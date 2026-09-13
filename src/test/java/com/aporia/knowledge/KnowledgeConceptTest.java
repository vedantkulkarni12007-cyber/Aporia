package com.aporia.knowledge;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeConceptTest {

    @Test
    public void testValidCreation() {
        KnowledgeConcept concept = new KnowledgeConcept("id-123", "Title", "A description");
        assertEquals("id-123", concept.id());
        assertEquals("Title", concept.title());
        assertEquals("A description", concept.description());
    }

    @Test
    public void testInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> new KnowledgeConcept(null, "Title", "Desc"));
        assertThrows(IllegalArgumentException.class, () -> new KnowledgeConcept("", "Title", "Desc"));
        assertThrows(IllegalArgumentException.class, () -> new KnowledgeConcept("   ", "Title", "Desc"));
    }

    @Test
    public void testInvalidTitle() {
        assertThrows(IllegalArgumentException.class, () -> new KnowledgeConcept("id", null, "Desc"));
        assertThrows(IllegalArgumentException.class, () -> new KnowledgeConcept("id", "", "Desc"));
    }

    @Test
    public void testEquality() {
        KnowledgeConcept c1 = new KnowledgeConcept("id", "Title", "Desc");
        KnowledgeConcept c2 = new KnowledgeConcept("id", "Title", "Desc");
        KnowledgeConcept c3 = new KnowledgeConcept("id", "Other Title", "Other Desc");

        assertEquals(c1, c2);
        assertNotEquals(c1, c3); // Records check all fields by default
    }
}
