package com.aporia.knowledge;

/**
 * Represents a relationship between two semantic concepts.
 */
public record KnowledgeRelation(String sourceId, String targetId, String type, RelationCategory category) {
    
    public enum RelationCategory {
        CONCEPTUAL,
        CONTEXTUAL
    }
    
    public KnowledgeRelation(String sourceId, String targetId, String type) {
        this(sourceId, targetId, type, RelationCategory.CONCEPTUAL);
    }
    
    public KnowledgeRelation {
        if (sourceId == null || sourceId.isBlank()) {
            throw new IllegalArgumentException("Source ID cannot be null or blank");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("Target ID cannot be null or blank");
        }
        if (category == null) {
            category = RelationCategory.CONCEPTUAL;
        }
    }
}
