package com.aporia.knowledge;

import com.aporia.knowledge.http.HttpTransport;
import com.aporia.knowledge.json.MiniJson;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Retrieves structured semantic knowledge from the public Wikidata API.
 */
public class WikidataKnowledgeSource implements KnowledgeSource {
    
    // Strict bound on requests to prevent graph explosion
    private static final int MAX_RELATIONS = 8;
    private final HttpTransport http;
    
    private record PropertyMeta(String label, KnowledgeRelation.RelationCategory category) {}
    
    // Whitelist of supported properties mapped to neutral Aporia relationship labels.
    // We use LinkedHashMap to maintain a deterministic order of preference.
    private static final Map<String, PropertyMeta> SUPPORTED_PROPERTIES = new LinkedHashMap<>();
    static {
        SUPPORTED_PROPERTIES.put("P31", new PropertyMeta("INSTANCE_OF", KnowledgeRelation.RelationCategory.CONCEPTUAL));
        SUPPORTED_PROPERTIES.put("P279", new PropertyMeta("SUBCLASS_OF", KnowledgeRelation.RelationCategory.CONCEPTUAL));
        SUPPORTED_PROPERTIES.put("P361", new PropertyMeta("PART_OF", KnowledgeRelation.RelationCategory.CONCEPTUAL));
        SUPPORTED_PROPERTIES.put("P101", new PropertyMeta("FIELD_OF_WORK", KnowledgeRelation.RelationCategory.CONCEPTUAL));
        
        SUPPORTED_PROPERTIES.put("P17", new PropertyMeta("COUNTRY", KnowledgeRelation.RelationCategory.CONTEXTUAL));
        SUPPORTED_PROPERTIES.put("P27", new PropertyMeta("CITIZENSHIP", KnowledgeRelation.RelationCategory.CONTEXTUAL));
        SUPPORTED_PROPERTIES.put("P106", new PropertyMeta("OCCUPATION", KnowledgeRelation.RelationCategory.CONTEXTUAL));
        SUPPORTED_PROPERTIES.put("P136", new PropertyMeta("GENRE", KnowledgeRelation.RelationCategory.CONTEXTUAL));
        SUPPORTED_PROPERTIES.put("P170", new PropertyMeta("CREATOR", KnowledgeRelation.RelationCategory.CONTEXTUAL));
        SUPPORTED_PROPERTIES.put("P50", new PropertyMeta("AUTHOR", KnowledgeRelation.RelationCategory.CONTEXTUAL));
    }

    public WikidataKnowledgeSource(HttpTransport http) {
        this.http = http;
    }

    @Override
    @SuppressWarnings("unchecked")
    public KnowledgeResult searchConcept(String query) throws KnowledgeException {
        if (query == null || query.isBlank()) {
            throw new KnowledgeException("Query cannot be empty");
        }
        
        try {
            String primaryId;
            String primaryLabel;
            String primaryDesc;
            
            // 1. Resolve human query to Wikidata Q-ID via wbsearchentities (skip if already Q-ID)
            boolean isQID = query.matches("^Q\\d+$");
            if (isQID) {
                primaryId = query;
                primaryLabel = query; // Updated later via batch fetch
                primaryDesc = "No description available.";
            } else {
                String searchUrl = "https://www.wikidata.org/w/api.php?action=wbsearchentities&search=" 
                    + URLEncoder.encode(query, StandardCharsets.UTF_8) 
                    + "&language=en&format=json&limit=1";
                    
                String searchJson = http.get(searchUrl);
                Map<String, Object> searchRoot = (Map<String, Object>) MiniJson.parse(searchJson);
                List<Object> searchArray = (List<Object>) searchRoot.get("search");
                
                if (searchArray == null || searchArray.isEmpty()) {
                    throw new KnowledgeException("No Wikidata results found for: " + query);
                }
                
                Map<String, Object> firstResult = (Map<String, Object>) searchArray.get(0);
                primaryId = extractString(firstResult, "id", null);
                if (primaryId == null) {
                    throw new KnowledgeException("Wikidata search result missing ID for: " + query);
                }
                primaryLabel = extractString(firstResult, "label", primaryId);
                primaryDesc = extractString(firstResult, "description", "No description available.");
            }
            
            KnowledgeConcept primaryConcept = new KnowledgeConcept(primaryId, primaryLabel, primaryDesc);
            
            // 2. Fetch structured claims for the resolved Q-ID via wbgetclaims
            String claimsUrl = "https://www.wikidata.org/w/api.php?action=wbgetclaims&entity=" 
                + URLEncoder.encode(primaryId, StandardCharsets.UTF_8) + "&format=json";
            
            String claimsJson = http.get(claimsUrl);
            Map<String, Object> claimsRoot = (Map<String, Object>) MiniJson.parse(claimsJson);
            Map<String, Object> claims = (Map<String, Object>) claimsRoot.get("claims");
            
            List<KnowledgeRelation> relations = new ArrayList<>();
            List<String> targetIds = new ArrayList<>();
            
            int conceptualCount = 0;
            
            if (claims != null) {
                for (Map.Entry<String, PropertyMeta> prop : SUPPORTED_PROPERTIES.entrySet()) {
                    String propId = prop.getKey();
                    String relType = prop.getValue().label();
                    KnowledgeRelation.RelationCategory category = prop.getValue().category();
                    
                    if (claims.containsKey(propId)) {
                        List<Object> snaks = (List<Object>) claims.get(propId);
                        int contextualPerProp = 0;
                        for (Object snakObj : snaks) {
                            if (category == KnowledgeRelation.RelationCategory.CONCEPTUAL && conceptualCount >= MAX_RELATIONS) {
                                break; // Max conceptual limit reached
                            }
                            if (category == KnowledgeRelation.RelationCategory.CONTEXTUAL && contextualPerProp >= 3) {
                                break; // Cap contextual items per property so graph isn't flooded with 50 authors
                            }
                            
                            Map<String, Object> snakMap = (Map<String, Object>) snakObj;
                            Map<String, Object> mainsnak = (Map<String, Object>) snakMap.get("mainsnak");
                            if (mainsnak == null) continue;
                            
                            Map<String, Object> datavalue = (Map<String, Object>) mainsnak.get("datavalue");
                            if (datavalue == null) continue;
                            
                            Map<String, Object> value = (Map<String, Object>) datavalue.get("value");
                            if (value == null) continue;
                            
                            String targetId = extractString(value, "id", null);
                            if (targetId != null && !targetIds.contains(targetId)) {
                                targetIds.add(targetId);
                                relations.add(new KnowledgeRelation(primaryId, targetId, relType, category));
                                if (category == KnowledgeRelation.RelationCategory.CONCEPTUAL) {
                                    conceptualCount++;
                                } else {
                                    contextualPerProp++;
                                }
                            }
                        }
                    }
                }
            }
            
            List<KnowledgeConcept> relatedConcepts = new ArrayList<>();
            List<String> fetchIds = new ArrayList<>(targetIds);
            if (isQID) {
                fetchIds.add(primaryId);
            }
            
            // 3. Batch fetch labels/descriptions for all target Q-IDs efficiently
            if (!fetchIds.isEmpty()) {
                String idsParam = String.join("|", fetchIds);
                String batchUrl = "https://www.wikidata.org/w/api.php?action=wbgetentities&ids=" 
                    + URLEncoder.encode(idsParam, StandardCharsets.UTF_8) 
                    + "&languages=en&props=" + URLEncoder.encode("labels|descriptions", StandardCharsets.UTF_8) + "&format=json";
                
                String batchJson = http.get(batchUrl);
                Map<String, Object> batchRoot = (Map<String, Object>) MiniJson.parse(batchJson);
                Map<String, Object> entities = (Map<String, Object>) batchRoot.get("entities");
                
                if (entities != null) {
                    if (isQID) {
                        Map<String, Object> primaryData = (Map<String, Object>) entities.get(primaryId);
                        if (primaryData != null) {
                            String fetchedLabel = extractLocalisedString(primaryData, "labels", primaryId);
                            String fetchedDesc = extractLocalisedString(primaryData, "descriptions", "No description available.");
                            primaryConcept = new KnowledgeConcept(primaryId, fetchedLabel, fetchedDesc);
                        }
                    }
                    
                    for (String tId : targetIds) {
                        Map<String, Object> entityData = (Map<String, Object>) entities.get(tId);
                        if (entityData != null) {
                            String tLabel = extractLocalisedString(entityData, "labels", tId);
                            String tDesc = extractLocalisedString(entityData, "descriptions", "No description available.");
                            relatedConcepts.add(new KnowledgeConcept(tId, tLabel, tDesc));
                        } else {
                            relatedConcepts.add(new KnowledgeConcept(tId, tId, "No description available."));
                        }
                    }
                }
            }
            
            return new KnowledgeResult(primaryConcept, relatedConcepts, relations);
            
        } catch (KnowledgeException e) {
            throw e;
        } catch (Exception e) {
            throw new KnowledgeException("Failed to retrieve knowledge from Wikidata: " + e.getMessage(), e);
        }
    }
    
    private String extractString(Map<String, Object> map, String key, String defaultVal) {
        if (map == null) return defaultVal;
        Object val = map.get(key);
        return val instanceof String ? (String) val : defaultVal;
    }
    
    @SuppressWarnings("unchecked")
    private String extractLocalisedString(Map<String, Object> entityData, String mapKey, String defaultVal) {
        if (entityData == null) return defaultVal;
        Map<String, Object> container = (Map<String, Object>) entityData.get(mapKey);
        if (container == null) return defaultVal;
        
        Map<String, Object> enBlock = (Map<String, Object>) container.get("en");
        if (enBlock == null) return defaultVal;
        
        return extractString(enBlock, "value", defaultVal);
    }
}
