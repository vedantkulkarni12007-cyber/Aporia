package com.aporia.knowledge;

import com.aporia.knowledge.http.HttpTransport;
import com.aporia.knowledge.json.MiniJson;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Retrieves knowledge from the public Wikipedia APIs.
 */
public class WikipediaKnowledgeSource implements KnowledgeSource {
    
    private final HttpTransport http;

    public WikipediaKnowledgeSource(HttpTransport http) {
        this.http = http;
    }

    @Override
    @SuppressWarnings("unchecked")
    public KnowledgeResult searchConcept(String query) throws KnowledgeException {
        if (query == null || query.isBlank()) {
            throw new KnowledgeException("Query cannot be empty");
        }

        try {
            // 1. Search for the best matching article title
            String searchUrl = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" 
                + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&utf8=&format=json&srlimit=1";
            
            String searchJson = http.get(searchUrl);
            Map<String, Object> searchRoot = (Map<String, Object>) MiniJson.parse(searchJson);
            Map<String, Object> queryObj = (Map<String, Object>) searchRoot.get("query");
            List<Object> searchArray = (List<Object>) queryObj.get("search");
            
            if (searchArray.isEmpty()) {
                throw new KnowledgeException("No Wikipedia results found for: " + query);
            }
            
            Map<String, Object> firstResult = (Map<String, Object>) searchArray.get(0);
            String title = (String) firstResult.get("title");
            
            // 2. Get the primary article summary
            String summaryUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/" 
                + URLEncoder.encode(title.replace(" ", "_"), StandardCharsets.UTF_8);
            
            String summaryJson = http.get(summaryUrl);
            Map<String, Object> summaryRoot = (Map<String, Object>) MiniJson.parse(summaryJson);
            
            // Try to extract wikibase_item (Q-ID) first, fallback to pageid
            String pageId = extractString(summaryRoot, "wikibase_item", null);
            if (pageId == null) {
                pageId = extractId(summaryRoot);
            }
            
            String displayTitle = extractString(summaryRoot, "title", title);
            String extract = extractString(summaryRoot, "extract", "No description available.");
            
            KnowledgeConcept primaryConcept = new KnowledgeConcept(pageId, displayTitle, extract);
            
            // Wikipedia's /related endpoint was decommissioned (T376297).
            // We now rely on Wikidata entirely for relations and related concepts.
            List<KnowledgeConcept> relatedConcepts = new ArrayList<>();
            List<KnowledgeRelation> relations = new ArrayList<>();
            
            return new KnowledgeResult(primaryConcept, relatedConcepts, relations);
            
        } catch (KnowledgeException e) {
            throw e; // Rethrow expected domain exceptions
        } catch (Exception e) {
            // Wrap underlying network, IO, or JSON parsing errors
            throw new KnowledgeException("Failed to retrieve knowledge from Wikipedia: " + e.getMessage(), e);
        }
    }
    
    private String extractString(Map<String, Object> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val instanceof String ? (String) val : defaultVal;
    }
    
    private String extractId(Map<String, Object> map) {
        Object val = map.get("pageid");
        if (val instanceof Double) return String.valueOf(((Double) val).longValue());
        if (val instanceof String) return (String) val;
        return extractString(map, "title", "unknown_id");
    }
}
