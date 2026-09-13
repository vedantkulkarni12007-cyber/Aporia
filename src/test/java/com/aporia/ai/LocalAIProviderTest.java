package com.aporia.ai;

import com.aporia.knowledge.KnowledgeConcept;
import com.aporia.knowledge.KnowledgeResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LocalAIProviderTest {

    @Test
    public void testValidAIRequest() throws AIException {
        LocalAIProvider provider = new InMemoryAIProvider("The black hole is a region of spacetime...", false);
        
        KnowledgeResult mockContext = new KnowledgeResult(
            new KnowledgeConcept("id", "Black Hole", "desc"),
            List.of(), List.of()
        );
        
        AIRequest request = new AIRequest("Explain black hole", mockContext, "Summarize this simply.");
        AIResponse response = provider.generate(request);
        
        assertNotNull(response);
        assertEquals("The black hole is a region of spacetime...", response.text());
        
        // Assert preservation
        assertEquals("Explain black hole", request.query());
        assertEquals("Summarize this simply.", request.instruction());
        assertEquals(mockContext, request.context());
    }

    @Test
    public void testInvalidEmptyRequest() {
        LocalAIProvider provider = new InMemoryAIProvider("Response", false);
        
        AIRequest request = new AIRequest("", null, "");
        
        AIException ex = assertThrows(AIException.class, () -> provider.generate(request));
        assertTrue(ex.getMessage().contains("Query cannot be empty"));
    }

    @Test
    public void testProviderFailure() {
        LocalAIProvider provider = new InMemoryAIProvider("Response", true);
        
        AIRequest request = new AIRequest("Test", null, "");
        
        AIException ex = assertThrows(AIException.class, () -> provider.generate(request));
        assertTrue(ex.getMessage().contains("Simulated AI failure"));
    }
}
