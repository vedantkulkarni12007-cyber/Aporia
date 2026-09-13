package com.aporia.ai;

import com.aporia.knowledge.KnowledgeResult;

/**
 * Represents a provider-neutral request to a local AI model.
 */
public class AIRequest {
    private final String query;
    private final KnowledgeResult context;
    private final String instruction;
    
    public AIRequest(String query, KnowledgeResult context, String instruction) {
        this.query = query;
        this.context = context;
        this.instruction = instruction;
    }
    
    public String query() { return query; }
    public KnowledgeResult context() { return context; }
    public String instruction() { return instruction; }
}
