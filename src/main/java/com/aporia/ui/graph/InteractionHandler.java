package com.aporia.ui.graph;

import com.aporia.graph.layout.Point;
import com.aporia.ui.camera.Camera;
import com.aporia.ui.state.VisualGraph;
import com.aporia.ui.state.VisualNode;
import java.util.function.Consumer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Handles mouse interaction (pan, zoom, selection, hover) for the graph canvas.
 */
public class InteractionHandler {
    private final Camera camera;
    private final VisualGraph visualGraph;
    private final Consumer<VisualNode> onNodeClicked;
    private double lastMouseX;
    private double lastMouseY;
    private double pressedX;
    private double pressedY;
    private static final double DRAG_THRESHOLD = 5.0;
    private static final double BASE_HIT_RADIUS = 24.0;

    public InteractionHandler(Canvas canvas, Camera camera, VisualGraph visualGraph, Consumer<VisualNode> onNodeClicked) {
        this.camera = camera;
        this.visualGraph = visualGraph;
        this.onNodeClicked = onNodeClicked;

        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnScroll(this::handleScroll);
        canvas.setOnMouseMoved(this::handleMouseMoved);
    }

    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getX();
        lastMouseY = event.getY();
        pressedX = event.getX();
        pressedY = event.getY();

        VisualNode clickedNode = hitTest(event.getX(), event.getY(), camera, visualGraph);
        visualGraph.selectNode(clickedNode);
    }

    private void handleMouseReleased(MouseEvent event) {
        double dx = event.getX() - pressedX;
        double dy = event.getY() - pressedY;
        if (Math.hypot(dx, dy) <= DRAG_THRESHOLD) {
            VisualNode clickedNode = hitTest(event.getX(), event.getY(), camera, visualGraph);
            if (onNodeClicked != null) {
                onNodeClicked.accept(clickedNode);
            }
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        double dx = event.getX() - lastMouseX;
        double dy = event.getY() - lastMouseY;
        camera.pan(dx, dy);
        lastMouseX = event.getX();
        lastMouseY = event.getY();
        
        // Disable hover effect while dragging
        visualGraph.hoverNode(null);
    }

    private void handleMouseMoved(MouseEvent event) {
        VisualNode hovered = hitTest(event.getX(), event.getY(), camera, visualGraph);
        visualGraph.hoverNode(hovered);
    }

    private void handleScroll(ScrollEvent event) {
        double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
        camera.zoom(zoomFactor, event.getX(), event.getY());
        
        // Re-calculate hover immediately after scrolling
        VisualNode hovered = hitTest(event.getX(), event.getY(), camera, visualGraph);
        visualGraph.hoverNode(hovered);
    }

    /**
     * Determines which node is clicked/hovered based on screen coordinates.
     * This logic is independently testable.
     */
    public static VisualNode hitTest(double screenX, double screenY, Camera camera, VisualGraph visualGraph) {
        Point worldClick = camera.screenToWorld(screenX, screenY);
        for (VisualNode vNode : visualGraph.getNodes()) {
            double dx = vNode.getX() - worldClick.x();
            double dy = vNode.getY() - worldClick.y();
            
            // Match the actual rendered radius formula from GraphRenderer
            double scale = Math.max(0.4, 1.0 - (vNode.getDepth() * 0.15));
            double nodeRadius = BASE_HIT_RADIUS * scale;
            
            if (Math.hypot(dx, dy) <= nodeRadius) {
                return vNode;
            }
        }
        return null;
    }
}
