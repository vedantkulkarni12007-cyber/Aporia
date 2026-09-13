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
        Point screen = camera.worldToScreen(0, 0);
        assertEquals(400.0, screen.x(), 0.001);
        assertEquals(300.0, screen.y(), 0.001);

        Point world = camera.screenToWorld(400, 300);
        assertEquals(0.0, world.x(), 0.001);
        assertEquals(0.0, world.y(), 0.001);
    }

    @Test
    public void testPanWithReducedMotion() {
        camera.setReducedMotion(true); // Disable inertia for instant test
        camera.pan(100, -50);
        
        Point screen = camera.worldToScreen(0, 0);
        assertEquals(500.0, screen.x(), 0.001);
        assertEquals(250.0, screen.y(), 0.001);
    }

    @Test
    public void testZoomAroundCenterWithReducedMotion() {
        camera.setReducedMotion(true);
        camera.zoom(2.0, 400, 300); 
        
        assertEquals(2.0, camera.getZoom(), 0.001);
        
        Point screenCenter = camera.worldToScreen(0, 0);
        assertEquals(400.0, screenCenter.x(), 0.001);
        assertEquals(300.0, screenCenter.y(), 0.001);

        Point screenRight = camera.worldToScreen(100, 0);
        assertEquals(600.0, screenRight.x(), 0.001);
    }

    @Test
    public void testInertiaVelocityDecayAndSettling() {
        // Reduced motion is FALSE by default
        assertEquals(0.0, camera.getCurrentXOffset(), 0.001);
        
        camera.pan(100, 0);
        assertEquals(100.0, camera.getTargetXOffset(), 0.001);
        assertEquals(0.0, camera.getCurrentXOffset(), 0.001); // Hasn't moved yet

        // Simulate a tiny time step (16ms)
        camera.update(0.016);
        double firstStepX = camera.getCurrentXOffset();
        assertTrue(firstStepX > 0.0 && firstStepX < 100.0, "Should have moved slightly towards target");

        // Simulate large time gap to settle
        camera.update(2.0);
        assertEquals(100.0, camera.getCurrentXOffset(), 0.001, "Should have settled exactly on target");
    }
}
