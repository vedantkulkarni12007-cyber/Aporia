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
    
    // Whitelist of supported properties mapped to neutral Aporia relationship labels.
    // We use LinkedHashMap to maintain a deterministic order of preference when trimming to MAX_RELATIONS.
    private static final Map<String, String> SUPPORTED_PROPERTIES = new LinkedHashMap<>();
    static {
        SUPPORTED_PROPERTIES.put("P31", "INSTANCE_OF");
        SUPPORTED_PROPERTIES.put("P279", "SUBCLASS_OF");
        SUPPORTED_PROPERTIES.put("P361", "PART_OF");
        SUPPORTED_PROPERTIES.put("P17", "COUNTRY");
        SUPPORTED_PROPERTIES.put("P27", "CITIZENSHIP");
        SUPPORTED_PROPERTIES.put("P106", "OCCUPATION");
        SUPPORTED_PROPERTIES.put("P101", "FIELD_OF_WORK");
        SUPPORTED_PROPERTIES.put("P136", "GENRE");
        SUPPORTED_PROPERTIES.put("P170", "CREATOR");
        SUPPORTED_PROPERTIES.put("P50", "AUTHOR");
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
            // 1. Resolve human query to Wikidata Q-ID via wbsearchentities
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
            String primaryId = extractString(firstResult, "id", null);
            if (primaryId == null) {
                throw new KnowledgeException("Wikidata search result missing ID for: " + query);
            }
            String primaryLabel = extractString(firstResult, "label", primaryId);
            String primaryDesc = extractString(firstResult, "description", "No description available.");
            
            KnowledgeConcept primaryConcept = new KnowledgeConcept(primaryId, primaryLabel, primaryDesc);
            
            // 2. Fetch structured claims for the resolved Q-ID via wbgetclaims
            String claimsUrl = "https://www.wikidata.org/w/api.php?action=wbgetclaims&entity=" 
                + URLEncoder.encode(primaryId, StandardCharsets.UTF_8) + "&format=json";
            
            String claimsJson = http.get(claimsUrl);
            Map<String, Object> claimsRoot = (Map<String, Object>) MiniJson.parse(claimsJson);
            Map<String, Object> claims = (Map<String, Object>) claimsRoot.get("claims");
            
            List<KnowledgeRelation> relations = new ArrayList<>();
            List<String> targetIds = new ArrayList<>();
            
            if (claims != null) {
                for (Map.Entry<String, String> prop : SUPPORTED_PROPERTIES.entrySet()) {
                    String propId = prop.getKey();
                    String relType = prop.getValue();
                    
                    if (claims.containsKey(propId)) {
                        List<Object> snaks = (List<Object>) claims.get(propId);
                        for (Object snakObj : snaks) {
                            if (targetIds.size() >= MAX_RELATIONS) break;
                            
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
                                relations.add(new KnowledgeRelation(primaryId, targetId, relType));
                            }
                        }
                    }
                    if (targetIds.size() >= MAX_RELATIONS) break;
                }
            }
            
            List<KnowledgeConcept> relatedConcepts = new ArrayList<>();
            
            // 3. Batch fetch labels/descriptions for all target Q-IDs efficiently
            if (!targetIds.isEmpty()) {
                String idsParam = String.join("|", targetIds);
                String batchUrl = "https://www.wikidata.org/w/api.php?action=wbgetentities&ids=" 
                    + URLEncoder.encode(idsParam, StandardCharsets.UTF_8) 
                    + "&languages=en&props=" + URLEncoder.encode("labels|descriptions", StandardCharsets.UTF_8) + "&format=json";
                
                String batchJson = http.get(batchUrl);
                Map<String, Object> batchRoot = (Map<String, Object>) MiniJson.parse(batchJson);
                Map<String, Object> entities = (Map<String, Object>) batchRoot.get("entities");
                
                if (entities != null) {
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
