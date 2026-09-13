package com.aporia.graph.layout;

import com.aporia.graph.Graph;
import com.aporia.model.Edge;
import com.aporia.model.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RadialLayoutTest {

    private Graph graph;
    private Node root, child1, child2, grandchild1;

    @BeforeEach
    public void setUp() {
        graph = new Graph();
        root = new Node("root", "Root");
        child1 = new Node("c1", "Child 1");
        child2 = new Node("c2", "Child 2");
        grandchild1 = new Node("gc1", "Grandchild 1");

        graph.addNode(root);
        graph.addNode(child1);
        graph.addNode(child2);
        graph.addNode(grandchild1);
    }

    @Test
    public void testNullInputs() {
        assertThrows(IllegalArgumentException.class, () -> RadialLayout.calculate(null, root, 100));
        assertThrows(IllegalArgumentException.class, () -> RadialLayout.calculate(graph, null, 100));
    }

    @Test
    public void testMissingRoot() {
        Node missing = new Node("missing", "Missing");
        assertThrows(IllegalArgumentException.class, () -> RadialLayout.calculate(graph, missing, 100));
    }

    @Test
    public void testRootPositionAndDepth() {
        Map<Node, NodeLayout> layout = RadialLayout.calculate(graph, root, 100);
        NodeLayout nl = layout.get(root);
        assertEquals(0.0, nl.point().x(), 0.001);
        assertEquals(0.0, nl.point().y(), 0.001);
        assertEquals(0, nl.depth());
    }

    @Test
    public void testDepthBasedDistance() {
        graph.addEdge(new Edge(root, child1, "REL"));
        graph.addEdge(new Edge(child1, grandchild1, "REL"));

        Map<Node, NodeLayout> layout = RadialLayout.calculate(graph, root, 150);
        
        NodeLayout nlRoot = layout.get(root);
        NodeLayout nlChild1 = layout.get(child1);
        NodeLayout nlGrandchild = layout.get(grandchild1);

        double distC1 = Math.hypot(nlChild1.point().x() - nlRoot.point().x(), nlChild1.point().y() - nlRoot.point().y());
        double distGC1 = Math.hypot(nlGrandchild.point().x() - nlRoot.point().x(), nlGrandchild.point().y() - nlRoot.point().y());

        assertEquals(150.0, distC1, 0.001);
        assertEquals(300.0, distGC1, 0.001);
        
        assertEquals(1, nlChild1.depth());
        assertEquals(2, nlGrandchild.depth());
    }

    @Test
    public void testMultipleNodesAtSameDepth() {
        graph.addEdge(new Edge(root, child1, "REL"));
        graph.addEdge(new Edge(root, child2, "REL"));

        Map<Node, NodeLayout> layout = RadialLayout.calculate(graph, root, 100);
        
        NodeLayout nlC1 = layout.get(child1);
        NodeLayout nlC2 = layout.get(child2);

        // Should be on opposite sides (angle 0 and PI)
        assertEquals(100.0, nlC1.point().x(), 0.001);
        assertEquals(0.0, nlC1.point().y(), 0.001);

        assertEquals(-100.0, nlC2.point().x(), 0.001);
        assertEquals(0.0, nlC2.point().y(), 0.001);
    }
}
