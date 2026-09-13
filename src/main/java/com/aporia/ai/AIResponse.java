package com.aporia.ai;

/**
 * Represents a provider-neutral response from a local AI model.
 */
public class AIResponse {
    private final String text;
    
    public AIResponse(String text) {
        this.text = text;
    }
    
    public String text() { return text; }
}
