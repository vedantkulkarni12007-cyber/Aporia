package com.aporia.ai;

/**
 * Thrown when the local AI provider fails to generate a response.
 * This can occur due to the provider being unavailable, invalid requests, or internal model errors.
 */
public class AIException extends Exception {
    public AIException(String message) {
        super(message);
    }
    
    public AIException(String message, Throwable cause) {
        super(message, cause);
    }
}
