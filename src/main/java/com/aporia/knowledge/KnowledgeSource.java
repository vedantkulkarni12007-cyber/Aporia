package com.aporia.knowledge;

/**
 * Abstraction for external knowledge providers (e.g., Wikipedia, Wikidata).
 * This keeps the application entirely decoupled from HTTP or specific APIs.
 */
public interface KnowledgeSource {
    
    /**
     * Searches for a concept by a string query and retrieves it along with its immediate neighborhood.
     *
     * @param query The user's search term.
     * @return A complete result containing the primary concept, related concepts, and edges.
     * @throws KnowledgeException If the concept cannot be found or if retrieval fails.
     */
    KnowledgeResult searchConcept(String query) throws KnowledgeException;
}
