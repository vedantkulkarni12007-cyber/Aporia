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
    
    /**
     * Performs an HTTP POST request and returns the response body as a string.
     *
     * @param url The target URL.
     * @param jsonBody The JSON payload.
     * @param timeoutSeconds The timeout for the request in seconds.
     * @return The response body.
     * @throws Exception If a network error, timeout, or non-200 status occurs.
     */
    default String post(String url, String jsonBody, int timeoutSeconds) throws Exception {
        throw new UnsupportedOperationException("POST not implemented by this transport.");
    }
}
