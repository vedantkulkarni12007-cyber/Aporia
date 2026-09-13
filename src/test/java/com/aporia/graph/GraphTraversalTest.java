package com.aporia.graph;

import com.aporia.model.Edge;
import com.aporia.model.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GraphTraversalTest {

    private Graph graph;
    private Node nA, nB, nC, nD, nE;

    @BeforeEach
    public void setUp() {
        graph = new Graph();
        nA = new Node("A", "Node A");
        nB = new Node("B", "Node B");
        nC = new Node("C", "Node C");
        nD = new Node("D", "Node D");
        nE = new Node("E", "Node E");

        graph.addNode(nA);
        graph.addNode(nB);
        graph.addNode(nC);
        graph.addNode(nD);
        graph.addNode(nE);
    }

    @Test
    public void testBasicTraversal() {
        graph.addEdge(new Edge(nA, nB, "REL"));
        graph.addEdge(new Edge(nB, nC, "REL"));

        List<Node> path = GraphTraversal.breadthFirst(graph, nA, 10);
        assertEquals(3, path.size());
        assertEquals(nA, path.get(0));
        assertEquals(nB, path.get(1));
        assertEquals(nC, path.get(2));
    }

    @Test
    public void testBranching() {
        graph.addEdge(new Edge(nA, nB, "REL"));
        graph.addEdge(new Edge(nA, nC, "REL"));
        graph.addEdge(new Edge(nB, nD, "REL"));
        
        List<Node> path = GraphTraversal.breadthFirst(graph, nA, 10);
        assertEquals(4, path.size());
        assertEquals(nA, path.get(0));
        // A -> B, A -> C, B -> D
        assertTrue(path.contains(nB));
        assertTrue(path.contains(nC));
        assertTrue(path.contains(nD));
        assertEquals(nD, path.get(3)); // Depth 2 node visited last
    }

    @Test
    public void testCycles() {
        graph.addEdge(new Edge(nA, nB, "REL"));
        graph.addEdge(new Edge(nB, nC, "REL"));
        graph.addEdge(new Edge(nC, nA, "REL")); // cycle

        List<Node> path = GraphTraversal.breadthFirst(graph, nA, 10);
        assertEquals(3, path.size());
    }

    @Test
    public void testMissingStartNode() {
        Node missing = new Node("Z", "Z");
        assertThrows(IllegalArgumentException.class, () -> GraphTraversal.breadthFirst(graph, missing, 10));
    }

    @Test
    public void testMaximumDepth() {
        graph.addEdge(new Edge(nA, nB, "REL")); // Depth 1
        graph.addEdge(new Edge(nB, nC, "REL")); // Depth 2
        graph.addEdge(new Edge(nC, nD, "REL")); // Depth 3

        List<Node> depth0 = GraphTraversal.breadthFirst(graph, nA, 0);
        assertEquals(1, depth0.size());

        List<Node> depth1 = GraphTraversal.breadthFirst(graph, nA, 1);
        assertEquals(2, depth1.size());

        List<Node> depth2 = GraphTraversal.breadthFirst(graph, nA, 2);
        assertEquals(3, depth2.size());
    }
}
