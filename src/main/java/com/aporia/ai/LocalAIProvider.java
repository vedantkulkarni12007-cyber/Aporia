package com.aporia.ai;

/**
 * A provider-neutral abstraction for local AI capabilities.
 * 
 * THREADING CONTRACT:
 * Implementations of this interface may perform slow, blocking network I/O or 
 * heavy local computation. The `generate` method is strictly synchronous.
 * Callers MUST execute this method on a background thread and NEVER on the 
 * JavaFX Application Thread.
 */
public interface LocalAIProvider {
    
    /**
     * Generates an AI response based on the given request.
     * 
     * @param request the contextual request containing the query, knowledge grounding, and instructions
     * @return the generated response from the local AI
     * @throws AIException if generation fails or the provider is unavailable
     */
    AIResponse generate(AIRequest request) throws AIException;
}
