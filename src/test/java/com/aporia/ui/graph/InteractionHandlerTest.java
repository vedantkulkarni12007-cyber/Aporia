package com.aporia.ui.graph;

import com.aporia.graph.Graph;
import com.aporia.model.Node;
import com.aporia.ui.camera.Camera;
import com.aporia.ui.state.VisualGraph;
import com.aporia.ui.state.VisualNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InteractionHandlerTest {

    private Camera camera;
    private VisualGraph visualGraph;

    @BeforeEach
    public void setUp() {
        camera = new Camera();
        camera.updateViewport(800, 600); // screen center is 400,300

        Graph graph = new Graph();
        Node root = new Node("root", "Root");
        graph.addNode(root);

        visualGraph = new VisualGraph();
        visualGraph.initializeFromDomain(graph, root);
        // root is now at world (0,0) which maps to screen (400,300)
    }

    @Test
    public void testHitDetectionHit() {
        VisualNode hit = InteractionHandler.hitTest(400, 300, camera, visualGraph);
        assertNotNull(hit);
        assertEquals("root", hit.getDomainNode().getId());
    }

    @Test
    public void testHitDetectionMiss() {
        // Outside the 24.0 radius (world) which maps directly 1:1 when zoom is 1.0
        VisualNode miss = InteractionHandler.hitTest(425, 300, camera, visualGraph);
        assertNull(miss);
    }
    
    @Test
    public void testHitDetectionAfterCameraPan() {
        // Move camera so world (0,0) is at screen (500,300)
        camera.pan(100, 0); 
        
        // Clicking old center should miss
        VisualNode miss = InteractionHandler.hitTest(400, 300, camera, visualGraph);
        assertNull(miss);
        
        // Clicking new center should hit
        VisualNode hit = InteractionHandler.hitTest(500, 300, camera, visualGraph);
        assertNotNull(hit);
        assertEquals("root", hit.getDomainNode().getId());
    }
}
