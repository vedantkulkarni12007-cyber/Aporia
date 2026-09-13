package com.aporia.knowledge.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Standard Java 25 HttpClient implementation of the HttpTransport.
 * Reuses a single HttpClient instance as recommended.
 */
public class DefaultHttpTransport implements HttpTransport {
    
    private final HttpClient client;

    public DefaultHttpTransport() {
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    @Override
    public String get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            // Wikipedia requires a descriptive User-Agent
            .header("User-Agent", "Aporia/1.0 (https://github.com/vedantkulkarni12007-cyber/Aporia)")
            .header("Accept", "application/json")
            .GET()
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400) {
            throw new Exception("HTTP request failed with status: " + response.statusCode() + " for URL: " + url);
        }
        
        return response.body();
    }

    @Override
    public String post(String url, String jsonBody, int timeoutSeconds) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400) {
            throw new Exception("HTTP POST request failed with status: " + response.statusCode() + " for URL: " + url);
        }
        
        return response.body();
    }
}
