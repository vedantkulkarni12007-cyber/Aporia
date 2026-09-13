package com.aporia.knowledge;

/**
 * Represents a semantic concept retrieved from a knowledge source.
 * This is distinct from com.aporia.model.Node to prevent the pure graph
 * engine from being coupled to rich knowledge-provider data.
 */
public record KnowledgeConcept(String id, String title, String description) {
    public KnowledgeConcept {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Concept ID cannot be null or blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Concept title cannot be null or blank");
        }
    }
}
