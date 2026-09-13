package com.aporia.knowledge.http;

/**
 * An abstraction for HTTP requests to enable deterministic offline testing
 * without hitting real network endpoints.
 */
public interface HttpTransport {
    
    /**
     * Performs an HTTP GET request and returns the response body as a string.
     *
     * @param url The target URL.
     * @return The response body.
     * @throws Exception If a network error, timeout, or non-200 status occurs.
     */
    String get(String url) throws Exception;
}
