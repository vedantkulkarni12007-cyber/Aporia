package com.aporia.graph;

import com.aporia.model.Edge;
import com.aporia.model.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GraphTest {

    private Graph graph;

    @BeforeEach
    public void setUp() {
        graph = new Graph();
    }

    @Test
    public void testAddAndLookupNode() {
        Node node = new Node("1", "Concept");
        graph.addNode(node);

        assertTrue(graph.containsNode("1"));
        assertEquals(node, graph.getNode("1"));
        assertEquals(1, graph.getNodeCount());
    }

    @Test
    public void testDuplicateNode() {
        Node node1 = new Node("1", "Concept A");
        Node node2 = new Node("1", "Concept B");
        graph.addNode(node1);

        assertThrows(IllegalArgumentException.class, () -> graph.addNode(node2));
    }

    @Test
    public void testAddEdge() {
        Node source = new Node("1", "Source");
        Node target = new Node("2", "Target");
        graph.addNode(source);
        graph.addNode(target);

        Edge edge = new Edge(source, target, "REL");
        graph.addEdge(edge);

        assertEquals(1, graph.getEdgeCount());
        List<Edge> outgoing = graph.getOutgoingEdges(source);
        assertEquals(1, outgoing.size());
        assertEquals(edge, outgoing.get(0));
    }

    @Test
    public void testInvalidEdge() {
        Node source = new Node("1", "Source");
        Node target = new Node("2", "Target");
        Node notInGraph = new Node("3", "Missing");
        
        graph.addNode(source);

        Edge edgeWithMissingTarget = new Edge(source, notInGraph, "REL");
        Edge edgeWithMissingSource = new Edge(notInGraph, source, "REL");

        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(edgeWithMissingTarget));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(edgeWithMissingSource));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(null));
    }

    @Test
    public void testSelfLoopBehavior() {
        Node node = new Node("1", "Node");
        graph.addNode(node);
        Edge selfLoop = new Edge(node, node, "SELF");

        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(selfLoop));
    }

    @Test
    public void testDuplicateEdgeBehavior() {
        Node source = new Node("1", "Source");
        Node target = new Node("2", "Target");
        graph.addNode(source);
        graph.addNode(target);

        Edge edge = new Edge(source, target, "REL");
        graph.addEdge(edge);
        
        // Cannot add the exact same edge twice
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(edge));
    }

    @Test
    public void testClear() {
        Node node = new Node("1", "Node");
        graph.addNode(node);
        graph.clear();

        assertEquals(0, graph.getNodeCount());
        assertFalse(graph.containsNode("1"));
    }

    @Test
    public void testCopy() {
        Node n1 = new Node("1", "Node1");
        Node n2 = new Node("2", "Node2");
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(new Edge(n1, n2, "REL"));

        Graph clone = graph.copy();
        
        // Assert structure is identical
        assertEquals(2, clone.getNodeCount());
        assertEquals(1, clone.getEdgeCount());
        assertTrue(clone.containsNode("1"));
        
        // Assert independence
        clone.addNode(new Node("3", "Node3"));
        assertEquals(3, clone.getNodeCount());
        assertEquals(2, graph.getNodeCount()); // Original unaffected
    }
}
