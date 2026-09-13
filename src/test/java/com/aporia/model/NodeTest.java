package com.aporia.model;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {

    @Test
    public void testValidCreation() {
        Node node = new Node("n1", "Concept", "A test concept");
        assertEquals("n1", node.getId());
        assertEquals("Concept", node.getLabel());
        assertEquals("A test concept", node.getDescription());

        Node nodeNoDesc = new Node("n2", "Label");
        assertEquals("n2", nodeNoDesc.getId());
        assertNull(nodeNoDesc.getDescription());
    }

    @Test
    public void testInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> new Node(null, "Label"));
        assertThrows(IllegalArgumentException.class, () -> new Node("", "Label"));
        assertThrows(IllegalArgumentException.class, () -> new Node("id", null));
        assertThrows(IllegalArgumentException.class, () -> new Node("id", "   "));
    }

    @Test
    void testIdentityBehavior() {
        Node node1 = new Node("id", "Label 1", "Desc 1");
        Node node2 = new Node("id", "Label 2", "Desc 2");
        Node node3 = new Node("id2", "Label 1", "Desc 1");

        assertEquals(node1, node2);
        assertNotEquals(node1, node3);
        assertEquals(node1.hashCode(), node2.hashCode());
    }

    @Test
    void testContextBehavior() {
        Node node = new Node("id", "Label");
        
        assertTrue(node.getContext().isEmpty());
        
        node.addContext("Type", "Concept");
        node.addContext("Type", "Abstract");
        node.addContext("Source", "Wikidata");
        
        Map<String, java.util.List<String>> ctx = node.getContext();
        assertEquals(2, ctx.size());
        assertEquals(java.util.List.of("Concept", "Abstract"), ctx.get("Type"));
        assertEquals(java.util.List.of("Wikidata"), ctx.get("Source"));
    }

    @Test
    void testCopyPreservesContext() {
        Node node = new Node("id", "Label", "Desc");
        node.addContext("Domain", "Science");
        
        Node clone = node.copy();
        assertEquals(node.getId(), clone.getId());
        assertEquals(node.getLabel(), clone.getLabel());
        assertEquals(node.getDescription(), clone.getDescription());
        
        Map<String, java.util.List<String>> ctx = clone.getContext();
        assertEquals(1, ctx.size());
        assertEquals(java.util.List.of("Science"), ctx.get("Domain"));
        
        // Ensure changes to original don't affect clone
        node.addContext("NewKey", "NewVal");
        assertFalse(clone.getContext().containsKey("NewKey"));
    }
}
