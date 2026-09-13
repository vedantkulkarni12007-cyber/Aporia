package com.aporia.knowledge;

import com.aporia.knowledge.http.HttpTransport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WikidataKnowledgeSourceTest {

    @Test
    public void testSuccessfulRetrieval() throws Exception {
        // A mock offline HTTP transport mimicking Wikidata JSON responses deterministically
        HttpTransport mockHttp = url -> {
            if (url.contains("wbsearchentities")) {
                return "{\"search\":[{\"id\":\"Q42\",\"label\":\"Douglas Adams\",\"description\":\"English writer\"}]}";
            }
            if (url.contains("wbgetclaims")) {
                // Returns claims containing the P106 (occupation) relationship to Q36180 (writer)
                return "{\"claims\":{\"P106\":[{\"mainsnak\":{\"datavalue\":{\"value\":{\"id\":\"Q36180\"}}}}]}}";
            }
            if (url.contains("wbgetentities")) {
                return "{\"entities\":{\"Q36180\":{\"labels\":{\"en\":{\"value\":\"writer\"}},\"descriptions\":{\"en\":{\"value\":\"person who writes\"}}}}}";
            }
            throw new Exception("Unknown url: " + url);
        };
        
        WikidataKnowledgeSource source = new WikidataKnowledgeSource(mockHttp);
        KnowledgeResult res = source.searchConcept("Douglas Adams");
        
        // Assert primary concept uses Q-ID as identity, not label
        assertEquals("Q42", res.primaryConcept().id());
        assertEquals("Douglas Adams", res.primaryConcept().title());
        assertEquals("English writer", res.primaryConcept().description());
        
        // Assert related concepts extracted via batch request
        assertEquals(1, res.relatedConcepts().size());
        assertEquals("Q36180", res.relatedConcepts().get(0).id());
        assertEquals("writer", res.relatedConcepts().get(0).title());
        assertEquals("person who writes", res.relatedConcepts().get(0).description());
        
        // Assert relations preserve Q-IDs and map properties to generic types
        assertEquals(1, res.relations().size());
        assertEquals("Q42", res.relations().get(0).sourceId());
        assertEquals("Q36180", res.relations().get(0).targetId());
        assertEquals("OCCUPATION", res.relations().get(0).type());
    }

    @Test
    public void testNoSearchResults() {
        HttpTransport mockHttp = url -> "{\"search\":[]}";
        WikidataKnowledgeSource source = new WikidataKnowledgeSource(mockHttp);
        
        KnowledgeException thrown = assertThrows(KnowledgeException.class, () -> source.searchConcept("unfindable concept"));
        assertTrue(thrown.getMessage().contains("No Wikidata results found"));
    }

    @Test
    public void testHttpFailure() {
        HttpTransport mockHttp = url -> {
            throw new Exception("HTTP request failed with status: 503");
        };
        WikidataKnowledgeSource source = new WikidataKnowledgeSource(mockHttp);
        
        KnowledgeException thrown = assertThrows(KnowledgeException.class, () -> source.searchConcept("test"));
        assertTrue(thrown.getMessage().contains("Failed to retrieve knowledge from Wikidata"));
    }

    @Test
    public void testMalformedJsonHandling() {
        HttpTransport mockHttp = url -> "{malformed_json_here";
        WikidataKnowledgeSource source = new WikidataKnowledgeSource(mockHttp);
        
        KnowledgeException thrown = assertThrows(KnowledgeException.class, () -> source.searchConcept("test"));
        assertTrue(thrown.getMessage().contains("Failed to retrieve knowledge from Wikidata"));
    }
}
