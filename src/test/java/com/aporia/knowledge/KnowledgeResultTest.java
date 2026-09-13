package com.aporia.knowledge;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeResultTest {

    @Test
    public void testValidResult() {
        KnowledgeConcept p = new KnowledgeConcept("p", "P", "Desc");
        KnowledgeResult result = new KnowledgeResult(p, null, null);

        assertEquals(p, result.primaryConcept());
        assertNotNull(result.relatedConcepts());
        assertNotNull(result.relations());
        assertTrue(result.relatedConcepts().isEmpty());
    }

    @Test
    public void testNullPrimaryConcept() {
        assertThrows(IllegalArgumentException.class, () -> new KnowledgeResult(null, List.of(), List.of()));
    }

    @Test
    public void testImmutability() {
        KnowledgeConcept p = new KnowledgeConcept("p", "P", "Desc");
        List<KnowledgeConcept> related = new java.util.ArrayList<>();
        
        KnowledgeResult result = new KnowledgeResult(p, related, null);
        
        // Modifying the original list shouldn't modify the record's list
        related.add(new KnowledgeConcept("new", "New", "Desc"));
        
        assertTrue(result.relatedConcepts().isEmpty());
    }
}
