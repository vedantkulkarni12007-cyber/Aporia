package com.aporia.ui.camera;

import com.aporia.graph.layout.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CameraTest {

    private Camera camera;

    @BeforeEach
    public void setUp() {
        camera = new Camera();
        camera.updateViewport(800, 600);
    }

    @Test
    public void testInitialTransforms() {
        // At origin, (0,0) world is center of screen (400, 300)
        Point screen = camera.worldToScreen(0, 0);
        assertEquals(400.0, screen.x(), 0.001);
        assertEquals(300.0, screen.y(), 0.001);

        Point world = camera.screenToWorld(400, 300);
        assertEquals(0.0, world.x(), 0.001);
        assertEquals(0.0, world.y(), 0.001);
    }

    @Test
    public void testPan() {
        camera.pan(100, -50);
        
        // Panning 100 right means world (0,0) moves to screen (500, 250)
        Point screen = camera.worldToScreen(0, 0);
        assertEquals(500.0, screen.x(), 0.001);
        assertEquals(250.0, screen.y(), 0.001);
    }

    @Test
    public void testZoomAroundCenter() {
        camera.zoom(2.0, 400, 300); // Zoom in 2x around center
        
        assertEquals(2.0, camera.getZoom(), 0.001);
        
        // World (0,0) should still be at center
        Point screenCenter = camera.worldToScreen(0, 0);
        assertEquals(400.0, screenCenter.x(), 0.001);
        assertEquals(300.0, screenCenter.y(), 0.001);

        // World (100, 0) should be at (600, 300) instead of (500, 300) because zoom is 2x
        Point screenRight = camera.worldToScreen(100, 0);
        assertEquals(600.0, screenRight.x(), 0.001);
    }
}
