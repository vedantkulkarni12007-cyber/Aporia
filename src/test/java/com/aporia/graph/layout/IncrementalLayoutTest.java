package com.aporia.graph.layout;

import com.aporia.model.Node;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class IncrementalLayoutTest {

    @Test
    public void testExpandPreservesExistingPositions() {
        Node origin = new Node("1", "Origin");
        Map<Node, NodeLayout> layoutMap = new HashMap<>();
        layoutMap.put(origin, new NodeLayout(new Point(100, 100), 1));
        
        List<Node> newNodes = new ArrayList<>();
        Node n2 = new Node("2", "N2");
        newNodes.add(n2);
        
        IncrementalLayout.expand(layoutMap, origin, newNodes, 50.0);
        
        // Origin must be unchanged
        NodeLayout originLayout = layoutMap.get(origin);
        assertEquals(100.0, originLayout.point().x(), 0.001);
        assertEquals(100.0, originLayout.point().y(), 0.001);
        assertEquals(1, originLayout.depth());
        
        // New node must be added
        NodeLayout newLayout = layoutMap.get(n2);
        assertNotNull(newLayout);
        assertEquals(2, newLayout.depth());
    }

    @Test
    public void testDeterministicPlacement() {
        Node origin = new Node("1", "Origin");
        
        Map<Node, NodeLayout> map1 = new HashMap<>();
        map1.put(origin, new NodeLayout(new Point(0, 50), 1));
        
        Map<Node, NodeLayout> map2 = new HashMap<>();
        map2.put(origin, new NodeLayout(new Point(0, 50), 1));
        
        List<Node> newNodes = new ArrayList<>();
        newNodes.add(new Node("2", "N2"));
        newNodes.add(new Node("3", "N3"));
        
        IncrementalLayout.expand(map1, origin, newNodes, 50.0);
        IncrementalLayout.expand(map2, origin, newNodes, 50.0);
        
        assertEquals(map1.get(newNodes.get(0)).point().x(), map2.get(newNodes.get(0)).point().x(), 0.001);
        assertEquals(map1.get(newNodes.get(0)).point().y(), map2.get(newNodes.get(0)).point().y(), 0.001);
    }
    
    @Test
    public void testNodesDoNotOverlap() {
        Node origin = new Node("1", "Origin");
        Map<Node, NodeLayout> layoutMap = new HashMap<>();
        layoutMap.put(origin, new NodeLayout(new Point(50, 0), 1));
        
        List<Node> newNodes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            newNodes.add(new Node("N" + i, "Node " + i));
        }
        
        IncrementalLayout.expand(layoutMap, origin, newNodes, 50.0);
        
        // Check that none of the new nodes have identical coordinates
        for (int i = 0; i < newNodes.size(); i++) {
            for (int j = i + 1; j < newNodes.size(); j++) {
                Point p1 = layoutMap.get(newNodes.get(i)).point();
                Point p2 = layoutMap.get(newNodes.get(j)).point();
                
                double dx = p1.x() - p2.x();
                double dy = p1.y() - p2.y();
                double dist = Math.hypot(dx, dy);
                
                assertTrue(dist > 5.0, "Nodes should not be placed on top of each other");
            }
        }
    }
}
