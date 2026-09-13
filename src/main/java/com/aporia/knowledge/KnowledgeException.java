package com.aporia.knowledge;

/**
 * Exception thrown when a knowledge source fails to retrieve or parse information.
 * Uses a checked exception to enforce robust error handling for network/API failures.
 */
public class KnowledgeException extends Exception {
    public KnowledgeException(String message) {
        super(message);
    }

    public KnowledgeException(String message, Throwable cause) {
        super(message, cause);
    }
}
