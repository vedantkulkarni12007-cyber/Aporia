package com.aporia.ui.graph;

import com.aporia.graph.layout.Point;
import com.aporia.ui.camera.Camera;
import com.aporia.ui.state.VisualGraph;
import com.aporia.ui.state.VisualNode;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Handles mouse interaction (pan, zoom, selection) for the graph canvas.
 */
public class InteractionHandler {
    private final Camera camera;
    private final VisualGraph visualGraph;
    private double lastMouseX;
    private double lastMouseY;
    private static final double BASE_HIT_RADIUS = 24.0;

    public InteractionHandler(Canvas canvas, Camera camera, VisualGraph visualGraph) {
        this.camera = camera;
        this.visualGraph = visualGraph;

        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnScroll(this::handleScroll);
    }

    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getX();
        lastMouseY = event.getY();

        VisualNode clickedNode = hitTest(event.getX(), event.getY(), camera, visualGraph);
        visualGraph.selectNode(clickedNode);
    }

    private void handleMouseDragged(MouseEvent event) {
        double dx = event.getX() - lastMouseX;
        double dy = event.getY() - lastMouseY;
        camera.pan(dx, dy);
        lastMouseX = event.getX();
        lastMouseY = event.getY();
    }

    private void handleScroll(ScrollEvent event) {
        double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
        camera.zoom(zoomFactor, event.getX(), event.getY());
    }

    /**
     * Determines which node is clicked based on screen coordinates.
     * This logic is extracted to be independently testable.
     */
    public static VisualNode hitTest(double screenX, double screenY, Camera camera, VisualGraph visualGraph) {
        Point worldClick = camera.screenToWorld(screenX, screenY);
        for (VisualNode vNode : visualGraph.getNodes()) {
            double dx = vNode.getX() - worldClick.x();
            double dy = vNode.getY() - worldClick.y();
            
            // Calculate actual world radius based on depth
            double scale = Math.max(0.4, 1.0 - (vNode.getDepth() * 0.15));
            double nodeRadius = BASE_HIT_RADIUS * scale;
            
            if (Math.hypot(dx, dy) <= nodeRadius) {
                return vNode;
            }
        }
        return null;
    }
}
