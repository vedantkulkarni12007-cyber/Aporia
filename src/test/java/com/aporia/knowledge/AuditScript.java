package com.aporia.knowledge;

import com.aporia.knowledge.http.DefaultHttpTransport;
import com.aporia.knowledge.http.HttpTransport;
import com.aporia.knowledge.json.MiniJson;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class AuditScript {
    private final HttpTransport http = new DefaultHttpTransport();

    private record PropertyMeta(String label, KnowledgeRelation.RelationCategory category) {}
    private static final Map<String, PropertyMeta> SUPPORTED = new LinkedHashMap<>();
    static {
        SUPPORTED.put("P31", new PropertyMeta("INSTANCE_OF", KnowledgeRelation.RelationCategory.CONCEPTUAL));
        SUPPORTED.put("P279", new PropertyMeta("SUBCLASS_OF", KnowledgeRelation.RelationCategory.CONCEPTUAL));
        SUPPORTED.put("P361", new PropertyMeta("PART_OF", KnowledgeRelation.RelationCategory.CONCEPTUAL));
        SUPPORTED.put("P101", new PropertyMeta("FIELD_OF_WORK", KnowledgeRelation.RelationCategory.CONCEPTUAL));
        
        SUPPORTED.put("P17", new PropertyMeta("COUNTRY", KnowledgeRelation.RelationCategory.CONTEXTUAL));
        SUPPORTED.put("P27", new PropertyMeta("CITIZENSHIP", KnowledgeRelation.RelationCategory.CONTEXTUAL));
        SUPPORTED.put("P106", new PropertyMeta("OCCUPATION", KnowledgeRelation.RelationCategory.CONTEXTUAL));
        SUPPORTED.put("P136", new PropertyMeta("GENRE", KnowledgeRelation.RelationCategory.CONTEXTUAL));
        SUPPORTED.put("P170", new PropertyMeta("CREATOR", KnowledgeRelation.RelationCategory.CONTEXTUAL));
        SUPPORTED.put("P50", new PropertyMeta("AUTHOR", KnowledgeRelation.RelationCategory.CONTEXTUAL));
    }

    @Test
    public void runAudit() throws Exception {
        auditQuery("Astronomy", "Q333");
        auditQuery("Science", "Q336");
        auditQuery("India", "Q668");
        
        System.out.println("====== WIKIPEDIA LINK AUDIT ======");
        String wikiUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=links%7Ccategories&titles=Astronomy&pllimit=50&format=json";
        System.out.println("Wikipedia links for Astronomy:");
        String res = http.get(wikiUrl);
        System.out.println(res.substring(0, Math.min(res.length(), 1000)));
    }

    private void auditQuery(String query, String qid) throws Exception {
        System.out.println("====== " + query.toUpperCase() + " (" + qid + ") ======");
        
        String claimsUrl = "https://www.wikidata.org/w/api.php?action=wbgetclaims&entity=" 
                + URLEncoder.encode(qid, StandardCharsets.UTF_8) + "&format=json";
        String claimsJson = http.get(claimsUrl);
        Map<String, Object> claimsRoot = (Map<String, Object>) MiniJson.parse(claimsJson);
        Map<String, Object> claims = (Map<String, Object>) claimsRoot.get("claims");
        
        for (Map.Entry<String, PropertyMeta> prop : SUPPORTED.entrySet()) {
            if (claims.containsKey(prop.getKey())) {
                List<Object> snaks = (List<Object>) claims.get(prop.getKey());
                for (Object snakObj : snaks) {
                    Map<String, Object> snakMap = (Map<String, Object>) snakObj;
                    Map<String, Object> mainsnak = (Map<String, Object>) snakMap.get("mainsnak");
                    if (mainsnak == null) continue;
                    Map<String, Object> datavalue = (Map<String, Object>) mainsnak.get("datavalue");
                    if (datavalue == null) continue;
                    Map<String, Object> value = (Map<String, Object>) datavalue.get("value");
                    if (value == null) continue;
                    
                    Object idObj = value.get("id");
                    if (idObj instanceof String) {
                        String targetId = (String) idObj;
                        String tLabel = fetchLabel(targetId);
                        System.out.println(qid + " (" + query + ") -> " + targetId + " (" + tLabel + ") : " + prop.getValue().label() + " [" + prop.getValue().category() + "] (Wikidata)");
                    }
                }
            }
        }
    }

    private String fetchLabel(String id) throws Exception {
        String batchUrl = "https://www.wikidata.org/w/api.php?action=wbgetentities&ids=" 
            + URLEncoder.encode(id, StandardCharsets.UTF_8) 
            + "&languages=en&props=labels&format=json";
        String json = http.get(batchUrl);
        Map<String, Object> batchRoot = (Map<String, Object>) MiniJson.parse(json);
        Map<String, Object> entities = (Map<String, Object>) batchRoot.get("entities");
        if (entities != null && entities.containsKey(id)) {
            Map<String, Object> entityData = (Map<String, Object>) entities.get(id);
            Map<String, Object> labels = (Map<String, Object>) entityData.get("labels");
            if (labels != null && labels.containsKey("en")) {
                Map<String, Object> en = (Map<String, Object>) labels.get("en");
                return (String) en.get("value");
            }
        }
        return "Unknown";
    }
}
