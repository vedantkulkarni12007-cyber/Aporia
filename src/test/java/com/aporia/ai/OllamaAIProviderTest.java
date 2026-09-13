package com.aporia.ai;

import com.aporia.knowledge.KnowledgeConcept;
import com.aporia.knowledge.KnowledgeRelation;
import com.aporia.knowledge.KnowledgeResult;
import com.aporia.knowledge.http.HttpTransport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OllamaAIProviderTest {

    private static class MockHttpTransport implements HttpTransport {
        String lastUrl;
        String lastBody;
        int lastTimeout;
        String mockResponse = "{\"response\": \"Generated text\"}";
        Exception mockException;

        @Override
        public String get(String url) throws Exception { 
            throw new UnsupportedOperationException("Not used by Ollama."); 
        }

        @Override
        public String post(String url, String jsonBody, int timeoutSeconds) throws Exception {
            this.lastUrl = url;
            this.lastBody = jsonBody;
            this.lastTimeout = timeoutSeconds;
            if (mockException != null) throw mockException;
            return mockResponse;
        }
    }

    @Test
    public void testSuccessfulGeneration() throws AIException {
        MockHttpTransport http = new MockHttpTransport();
        OllamaAIProvider provider = new OllamaAIProvider(http, "http://test-ollama", "llama-test", 45);
        
        AIRequest request = new AIRequest("Explain testing", null, "Be concise.");
        AIResponse response = provider.generate(request);
        
        assertNotNull(response);
        assertEquals("Generated text", response.text());
        
        // Assert correct endpoint and timeout
        assertEquals("http://test-ollama/api/generate", http.lastUrl);
        assertEquals(45, http.lastTimeout);
        
        // Assert JSON body contains correct configuration and prompt
        assertTrue(http.lastBody.contains("\"model\": \"llama-test\""));
        assertTrue(http.lastBody.contains("\"stream\": false"));
        assertTrue(http.lastBody.contains("Explain testing")); // Query
        assertTrue(http.lastBody.contains("Be concise.")); // Instruction
    }

    @Test
    public void testGroundedContextSerialization() throws AIException {
        MockHttpTransport http = new MockHttpTransport();
        OllamaAIProvider provider = new OllamaAIProvider(http, null, null, 0); // test defaults
        
        KnowledgeResult ctx = new KnowledgeResult(
            new KnowledgeConcept("id1", "Astronomy", "Study of stars"),
            List.of(new KnowledgeConcept("id2", "Physics", "Study of matter")),
            List.of(new KnowledgeRelation("id1", "id2", "RELATED_TO"))
        );
        
        AIRequest request = new AIRequest("Query", ctx, null);
        provider.generate(request);
        
        // Ensure defaults were populated
        assertEquals("http://localhost:11434/api/generate", http.lastUrl);
        assertEquals(60, http.lastTimeout);
        
        // Check serialization
        assertTrue(http.lastBody.contains("Astronomy"));
        assertTrue(http.lastBody.contains("Study of stars"));
        assertTrue(http.lastBody.contains("Astronomy is RELATED_TO Physics"));
    }
    
    @Test
    public void testEmptyResponseRejected() {
        MockHttpTransport http = new MockHttpTransport();
        http.mockResponse = "{\"response\": \"   \"}";
        OllamaAIProvider provider = new OllamaAIProvider(http, null, null, 0);
        
        AIException ex = assertThrows(AIException.class, () -> provider.generate(new AIRequest("q", null, null)));
        assertTrue(ex.getMessage().contains("empty response"));
    }
    
    @Test
    public void testMalformedJsonRejected() {
        MockHttpTransport http = new MockHttpTransport();
        http.mockResponse = "not valid json";
        OllamaAIProvider provider = new OllamaAIProvider(http, null, null, 0);
        
        assertThrows(AIException.class, () -> provider.generate(new AIRequest("q", null, null)));
    }
    
    @Test
    public void testMissingResponseField() {
        MockHttpTransport http = new MockHttpTransport();
        http.mockResponse = "{\"other\": \"field\"}";
        OllamaAIProvider provider = new OllamaAIProvider(http, null, null, 0);
        
        AIException ex = assertThrows(AIException.class, () -> provider.generate(new AIRequest("q", null, null)));
        assertTrue(ex.getMessage().contains("Missing 'response' field"));
    }
    
    @Test
    public void testHttpExceptionWrapped() {
        MockHttpTransport http = new MockHttpTransport();
        http.mockException = new RuntimeException("Network down");
        OllamaAIProvider provider = new OllamaAIProvider(http, null, null, 0);
        
        AIException ex = assertThrows(AIException.class, () -> provider.generate(new AIRequest("q", null, null)));
        assertTrue(ex.getMessage().contains("Network down"));
    }
    
    @Test
    public void testInterruptedExceptionHandled() {
        MockHttpTransport http = new MockHttpTransport();
        http.mockException = new InterruptedException("Sleep interrupted");
        OllamaAIProvider provider = new OllamaAIProvider(http, null, null, 0);
        
        AIException ex = assertThrows(AIException.class, () -> provider.generate(new AIRequest("q", null, null)));
        assertTrue(ex.getMessage().contains("interrupted"));
        assertTrue(Thread.currentThread().isInterrupted()); // Flag should be restored
        Thread.interrupted(); // Clear flag for the rest of the test suite
    }
    
    @Test
    public void testInvalidRequestRejected() {
        OllamaAIProvider provider = new OllamaAIProvider(new MockHttpTransport(), null, null, 0);
        assertThrows(AIException.class, () -> provider.generate(new AIRequest("", null, null)));
        assertThrows(AIException.class, () -> provider.generate(null));
    }
}
