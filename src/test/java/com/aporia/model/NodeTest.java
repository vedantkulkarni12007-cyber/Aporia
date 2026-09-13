package com.aporia.model;

import org.junit.jupiter.api.Test;
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
    public void testIdentityBehavior() {
        Node n1 = new Node("1", "Label A", "Desc A");
        Node n2 = new Node("1", "Label B", "Desc B");
        Node n3 = new Node("2", "Label A", "Desc A");

        assertEquals(n1, n2, "Nodes with same ID should be equal");
        assertEquals(n1.hashCode(), n2.hashCode(), "Nodes with same ID should have same hashcode");
        assertNotEquals(n1, n3, "Nodes with different IDs should not be equal");
    }
}
