package com.aporia.knowledge;

import java.util.List;

/**
 * An immutable result wrapping a retrieved concept, its neighborhood, and their relationships.
 */
public record KnowledgeResult(
    KnowledgeConcept primaryConcept,
    List<KnowledgeConcept> relatedConcepts,
    List<KnowledgeRelation> relations
) {
    public KnowledgeResult {
        if (primaryConcept == null) {
            throw new IllegalArgumentException("Primary concept cannot be null");
        }
        if (relatedConcepts == null) {
            relatedConcepts = List.of();
        }
        if (relations == null) {
            relations = List.of();
        }
        
        // Ensure immutability for the lists
        relatedConcepts = List.copyOf(relatedConcepts);
        relations = List.copyOf(relations);
    }
}
