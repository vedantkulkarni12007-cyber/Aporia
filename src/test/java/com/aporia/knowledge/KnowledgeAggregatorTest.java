package com.aporia.knowledge;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAggregatorTest {

    @Test
    public void testSuccessfulAggregation() throws KnowledgeException {
        KnowledgeSource wiki = query -> new KnowledgeResult(
            new KnowledgeConcept("wiki1", "Black Hole", "A region of spacetime..."),
            List.of(new KnowledgeConcept("wiki2", "Physics", "Natural science...")),
            List.of(new KnowledgeRelation("wiki1", "wiki2", "RELATED"))
        );
        
        KnowledgeSource data = query -> new KnowledgeResult(
            new KnowledgeConcept("Q1", "Black hole", "Spacetime region"),
            List.of(
                new KnowledgeConcept("Q2", "Physics", "Natural science"), // Duplicate label
                new KnowledgeConcept("Q3", "Gravity", "Fundamental interaction")
            ),
            List.of(
                new KnowledgeRelation("Q1", "Q2", "SUBCLASS_OF"),
                new KnowledgeRelation("Q1", "Q3", "PART_OF")
            )
        );
        
        KnowledgeAggregator aggregator = new KnowledgeAggregator(wiki, data);
        KnowledgeResult result = aggregator.search("black hole");
        
        // Primary concept should be Wikipedia's
        assertEquals("wiki1", result.primaryConcept().id());
        assertEquals("Black Hole", result.primaryConcept().title());
        
        // Related concepts should be deduplicated (Physics merged into wiki2, Gravity remains Q3)
        assertEquals(2, result.relatedConcepts().size());
        
        boolean hasPhysics = false;
        boolean hasGravity = false;
        
        for (KnowledgeConcept c : result.relatedConcepts()) {
            if (c.id().equals("wiki2")) hasPhysics = true;
            if (c.id().equals("Q3")) hasGravity = true;
        }
        
        assertTrue(hasPhysics);
        assertTrue(hasGravity);
        
        // Relations should map to the correct IDs
        assertEquals(3, result.relations().size());
        
        boolean hasWikiRelation = false;
        boolean hasDataSubclassRelation = false;
        boolean hasDataPartOfRelation = false;
        
        for (KnowledgeRelation r : result.relations()) {
            assertEquals("wiki1", r.sourceId()); // All should origin from primary
            
            if (r.targetId().equals("wiki2") && r.type().equals("RELATED")) hasWikiRelation = true;
            if (r.targetId().equals("wiki2") && r.type().equals("SUBCLASS_OF")) hasDataSubclassRelation = true;
            if (r.targetId().equals("Q3") && r.type().equals("PART_OF")) hasDataPartOfRelation = true;
        }
        
        assertTrue(hasWikiRelation);
        assertTrue(hasDataSubclassRelation);
        assertTrue(hasDataPartOfRelation);
    }

    @Test
    public void testPartialFailureWikipediaOnly() throws KnowledgeException {
        KnowledgeSource wiki = query -> new KnowledgeResult(
            new KnowledgeConcept("wiki1", "Test", "Test desc"),
            List.of(), List.of()
        );
        
        KnowledgeSource data = query -> { throw new KnowledgeException("Failed"); };
        
        KnowledgeAggregator aggregator = new KnowledgeAggregator(wiki, data);
        KnowledgeResult result = aggregator.search("test");
        
        assertEquals("wiki1", result.primaryConcept().id());
    }

    @Test
    public void testPartialFailureWikidataOnly() throws KnowledgeException {
        KnowledgeSource wiki = query -> { throw new KnowledgeException("Failed"); };
        
        KnowledgeSource data = query -> new KnowledgeResult(
            new KnowledgeConcept("Q1", "Test", "Test desc"),
            List.of(), List.of()
        );
        
        KnowledgeAggregator aggregator = new KnowledgeAggregator(wiki, data);
        KnowledgeResult result = aggregator.search("test");
        
        assertEquals("Q1", result.primaryConcept().id());
    }

    @Test
    public void testTotalFailure() {
        KnowledgeSource wiki = query -> { throw new KnowledgeException("Wiki failed"); };
        KnowledgeSource data = query -> { throw new KnowledgeException("Data failed"); };
        
        KnowledgeAggregator aggregator = new KnowledgeAggregator(wiki, data);
        
        KnowledgeException e = assertThrows(KnowledgeException.class, () -> aggregator.search("test"));
        assertTrue(e.getMessage().contains("Wiki failed"));
        assertTrue(e.getMessage().contains("Data failed"));
    }
}
