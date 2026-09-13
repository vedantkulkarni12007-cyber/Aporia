package com.aporia.knowledge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates knowledge retrieval across Wikipedia and Wikidata.
 * Combines human-readable summaries (Wikipedia) with structured semantic relationships (Wikidata).
 */
public class KnowledgeAggregator {
    private final KnowledgeSource wikipedia;
    private final KnowledgeSource wikidata;

    public KnowledgeAggregator(KnowledgeSource wikipedia, KnowledgeSource wikidata) {
        this.wikipedia = wikipedia;
        this.wikidata = wikidata;
    }

    public KnowledgeResult search(String query) throws KnowledgeException {
        KnowledgeResult wikiResult = null;
        KnowledgeResult dataResult = null;
        KnowledgeException wikiError = null;
        KnowledgeException dataError = null;

        try {
            wikiResult = wikipedia.searchConcept(query);
        } catch (KnowledgeException e) {
            wikiError = e;
        }

        try {
            dataResult = wikidata.searchConcept(query);
        } catch (KnowledgeException e) {
            dataError = e;
        }

        // Total failure
        if (wikiResult == null && dataResult == null) {
            throw new KnowledgeException("Failed to retrieve knowledge. " +
                (wikiError != null ? "Wiki: " + wikiError.getMessage() : "") +
                (dataError != null ? " Data: " + dataError.getMessage() : ""));
        }

        // Partial failure (graceful degradation)
        if (wikiResult == null) return dataResult;
        if (dataResult == null) return wikiResult;

        // Combination Strategy:
        // 1. Wikipedia provides the primary identity, label, and description for the root node.
        KnowledgeConcept primary = wikiResult.primaryConcept();

        // 2. We collect related concepts from both sources.
        // We use a LinkedHashMap to preserve ordering and deduplicate.
        // DEDUPLICATION RULE: Since we lack a direct Wikipedia ID <-> Wikidata Q-ID mapping in these APIs, 
        // we merge related targets based on case-insensitive exact label matches. 
        // This is necessary to prevent duplicate nodes in the visual graph for overlapping concepts like "Physics".
        Map<String, KnowledgeConcept> combinedConcepts = new LinkedHashMap<>();
        
        // Add Wikipedia related concepts first
        for (KnowledgeConcept c : wikiResult.relatedConcepts()) {
            combinedConcepts.put(c.id(), c);
        }
        
        // Add Wikidata related concepts if they don't share a label with an existing Wikipedia concept
        for (KnowledgeConcept c : dataResult.relatedConcepts()) {
            boolean duplicate = false;
            for (KnowledgeConcept existing : combinedConcepts.values()) {
                if (existing.title().equalsIgnoreCase(c.title())) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                combinedConcepts.put(c.id(), c); // Use the Q-ID if no duplicate
            }
        }
        
        List<KnowledgeConcept> allRelated = new ArrayList<>(combinedConcepts.values());
        
        // 3. Combine relations
        List<KnowledgeRelation> allRelations = new ArrayList<>(wikiResult.relations());
        
        // Map Wikidata's relations to the chosen primary root ID and the deduplicated target IDs.
        for (KnowledgeRelation r : dataResult.relations()) {
            String targetId = r.targetId();
            
            // Check if the target was kept as a Q-ID
            boolean foundTarget = false;
            for (KnowledgeConcept c : allRelated) {
                if (c.id().equals(targetId)) {
                    foundTarget = true;
                    break;
                }
            }
            
            if (foundTarget) {
                allRelations.add(new KnowledgeRelation(primary.id(), targetId, r.type()));
            } else {
                // If the Q-ID was discarded as a duplicate, find the Wikipedia ID it was merged into
                String mappedTargetId = targetId;
                
                // Find the original Wikidata concept to get its label
                for (KnowledgeConcept c : dataResult.relatedConcepts()) {
                    if (c.id().equals(targetId)) {
                        // Find the matching Wikipedia concept by label
                        for (KnowledgeConcept existing : combinedConcepts.values()) {
                            if (existing.title().equalsIgnoreCase(c.title())) {
                                mappedTargetId = existing.id();
                                break;
                            }
                        }
                        break;
                    }
                }
                
                allRelations.add(new KnowledgeRelation(primary.id(), mappedTargetId, r.type()));
            }
        }
        
        return new KnowledgeResult(primary, allRelated, allRelations);
    }
}
