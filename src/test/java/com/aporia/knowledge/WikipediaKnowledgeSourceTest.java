package com.aporia.knowledge;

import com.aporia.knowledge.http.HttpTransport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WikipediaKnowledgeSourceTest {

    @Test
    public void testSuccessfulRetrieval() throws Exception {
        // A mock offline HTTP transport mimicking Wikipedia JSON responses
        HttpTransport mockHttp = url -> {
            if (url.contains("action=query")) {
                return "{\"query\":{\"search\":[{\"title\":\"Black hole\"}]}}";
            }
            if (url.contains("page/summary/")) {
                return "{\"wikibase_item\":\"Q589\", \"pageid\":123, \"title\":\"Black hole\", \"extract\":\"A black hole is a region of spacetime...\"}";
            }
            throw new Exception("Unknown url: " + url);
        };
        
        WikipediaKnowledgeSource source = new WikipediaKnowledgeSource(mockHttp);
        KnowledgeResult res = source.searchConcept("black hole");
        
        // Assert primary concept
        assertEquals("Q589", res.primaryConcept().id());
        assertEquals("Black hole", res.primaryConcept().title());
        assertEquals("A black hole is a region of spacetime...", res.primaryConcept().description());
        
        // Assert related concepts and relations are empty (relies on Wikidata now)
        assertEquals(0, res.relatedConcepts().size());
        assertEquals(0, res.relations().size());
    }

    @Test
    public void testNoSearchResults() {
        HttpTransport mockHttp = url -> "{\"query\":{\"search\":[]}}";
        WikipediaKnowledgeSource source = new WikipediaKnowledgeSource(mockHttp);
        
        KnowledgeException thrown = assertThrows(KnowledgeException.class, () -> source.searchConcept("unfindable concept"));
        assertTrue(thrown.getMessage().contains("No Wikipedia results found"));
    }

    @Test
    public void testHttpFailure() {
        HttpTransport mockHttp = url -> {
            throw new Exception("HTTP request failed with status: 404");
        };
        WikipediaKnowledgeSource source = new WikipediaKnowledgeSource(mockHttp);
        
        KnowledgeException thrown = assertThrows(KnowledgeException.class, () -> source.searchConcept("test"));
        assertTrue(thrown.getMessage().contains("Failed to retrieve knowledge from Wikipedia"));
    }

    @Test
    public void testMalformedJsonHandling() {
        HttpTransport mockHttp = url -> "{malformed_json_here";
        WikipediaKnowledgeSource source = new WikipediaKnowledgeSource(mockHttp);
        
        KnowledgeException thrown = assertThrows(KnowledgeException.class, () -> source.searchConcept("test"));
        assertTrue(thrown.getMessage().contains("Failed to retrieve knowledge from Wikipedia"));
    }
}
