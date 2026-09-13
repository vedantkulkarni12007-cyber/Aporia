package com.aporia.ai;

/**
 * A fake deterministic AI provider used purely for testing the architecture.
 * Performs zero network or external process calls.
 */
public class InMemoryAIProvider implements LocalAIProvider {
    
    private final String deterministicResponse;
    private final boolean simulateFailure;
    
    public InMemoryAIProvider(String deterministicResponse, boolean simulateFailure) {
        this.deterministicResponse = deterministicResponse;
        this.simulateFailure = simulateFailure;
    }

    @Override
    public AIResponse generate(AIRequest request) throws AIException {
        if (simulateFailure) {
            throw new AIException("Simulated AI failure for testing.");
        }
        
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new AIException("Invalid request: Query cannot be empty.");
        }
        
        return new AIResponse(deterministicResponse);
    }
}
