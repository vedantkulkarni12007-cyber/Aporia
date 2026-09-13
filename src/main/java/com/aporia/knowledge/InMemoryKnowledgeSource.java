package com.aporia.knowledge;

import java.util.List;

/**
 * A tiny deterministic offline implementation of KnowledgeSource for development and testing.
 * Does not make any HTTP requests.
 */
public class InMemoryKnowledgeSource implements KnowledgeSource {

    @Override
    public KnowledgeResult searchConcept(String query) throws KnowledgeException {
        if (query == null || query.isBlank()) {
            throw new KnowledgeException("Query cannot be empty or null");
        }

        String lowerQuery = query.toLowerCase().trim();

        // Hard-coded dataset mirroring the Phase 0-3 sample graph
        if (lowerQuery.equals("astronomy")) {
            KnowledgeConcept primary = new KnowledgeConcept("astronomy", "Astronomy", "The study of celestial objects and phenomena.");
            KnowledgeConcept physics = new KnowledgeConcept("physics", "Physics", "The natural science of matter and energy.");
            KnowledgeConcept stars = new KnowledgeConcept("stars", "Stars", "Luminous spheres of plasma.");

            List<KnowledgeConcept> related = List.of(physics, stars);
            List<KnowledgeRelation> relations = List.of(
                new KnowledgeRelation("astronomy", "physics", "RELATED"),
                new KnowledgeRelation("astronomy", "stars", "STUDIES")
            );

            return new KnowledgeResult(primary, related, relations);
        } else if (lowerQuery.equals("physics")) {
            KnowledgeConcept primary = new KnowledgeConcept("physics", "Physics", "The natural science of matter and energy.");
            KnowledgeConcept gravity = new KnowledgeConcept("gravity", "Gravity", "Fundamental interaction that attracts all mass.");
            KnowledgeConcept astronomy = new KnowledgeConcept("astronomy", "Astronomy", "The study of celestial objects and phenomena.");

            List<KnowledgeConcept> related = List.of(gravity, astronomy);
            List<KnowledgeRelation> relations = List.of(
                new KnowledgeRelation("physics", "gravity", "EXPLAINS"),
                new KnowledgeRelation("astronomy", "physics", "RELATED")
            );

            return new KnowledgeResult(primary, related, relations);
        }

        throw new KnowledgeException("Concept not found in offline knowledge base: " + query);
    }
}
