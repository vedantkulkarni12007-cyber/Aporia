package com.aporia.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EdgeTest {

    @Test
    public void testValidCreation() {
        Node source = new Node("1", "Source");
        Node target = new Node("2", "Target");
        Edge edge = new Edge(source, target, "RELATES_TO");

        assertEquals(source, edge.source());
        assertEquals(target, edge.target());
        assertEquals("RELATES_TO", edge.relationship());
    }

    @Test
    public void testInvalidInput() {
        Node validNode = new Node("1", "Valid");

        assertThrows(IllegalArgumentException.class, () -> new Edge(null, validNode, "REL"));
        assertThrows(IllegalArgumentException.class, () -> new Edge(validNode, null, "REL"));
        assertThrows(IllegalArgumentException.class, () -> new Edge(validNode, validNode, null));
        assertThrows(IllegalArgumentException.class, () -> new Edge(validNode, validNode, "   "));
    }

    @Test
    public void testRelationshipBehavior() {
        Node source = new Node("1", "Source");
        Node target = new Node("2", "Target");
        Edge edge1 = new Edge(source, target, "REL");
        Edge edge2 = new Edge(source, target, "REL");
        Edge edge3 = new Edge(source, target, "DIFFERENT");

        assertEquals(edge1, edge2);
        assertNotEquals(edge1, edge3);
    }
}
