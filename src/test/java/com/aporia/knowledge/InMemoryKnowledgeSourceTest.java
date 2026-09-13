package com.aporia.knowledge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryKnowledgeSourceTest {

    private KnowledgeSource source;

    @BeforeEach
    public void setUp() {
        source = new InMemoryKnowledgeSource();
    }

    @Test
    public void testSearchExistingConcept() throws KnowledgeException {
        KnowledgeResult result = source.searchConcept("astronomy");
        assertNotNull(result);
        assertEquals("astronomy", result.primaryConcept().id());
        assertEquals("Astronomy", result.primaryConcept().title());
        
        assertEquals(2, result.relatedConcepts().size());
        assertEquals(2, result.relations().size());
    }

    @Test
    public void testSearchCaseInsensitive() throws KnowledgeException {
        KnowledgeResult result = source.searchConcept("  ASTRONOMY  ");
        assertNotNull(result);
        assertEquals("astronomy", result.primaryConcept().id());
    }

    @Test
    public void testSearchUnknownConcept() {
        assertThrows(KnowledgeException.class, () -> source.searchConcept("unknown"));
    }

    @Test
    public void testSearchEmptyOrNull() {
        assertThrows(KnowledgeException.class, () -> source.searchConcept(null));
        assertThrows(KnowledgeException.class, () -> source.searchConcept("   "));
    }
}
