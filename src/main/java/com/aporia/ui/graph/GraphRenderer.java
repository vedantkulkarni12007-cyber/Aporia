package com.aporia.ui.graph;

import com.aporia.graph.layout.Point;
import com.aporia.ui.camera.Camera;
import com.aporia.ui.state.VisualEdge;
import com.aporia.ui.state.VisualGraph;
import com.aporia.ui.state.VisualNode;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

/**
 * Responsible for rendering the VisualGraph using JavaFX Canvas.
 * Implements the "Archival Observatory" visual identity.
 */
public class GraphRenderer {
    private final Canvas canvas;
    private final VisualGraph visualGraph;
    private final Camera camera;

    // Archival Observatory Palette
    private static final Color BG_COLOR = Color.web("#1A1A18");
    private static final Color EDGE_COLOR = Color.rgb(168, 149, 122, 0.25);
    private static final Color ROOT_COLOR = Color.web("#B59E80");
    private static final Color NODE_COLOR = Color.web("#A69F91");
    private static final Color NODE_SELECTED_COLOR = Color.web("#F0EAD6");
    private static final Color TEXT_COLOR = Color.web("#D4C8B8");
    
    // System font fallback for sans-serif
    private static final String FONT_FAMILY = "SansSerif";
    
    private static final double BASE_RADIUS = 24.0;
    private static final double BASE_FONT_SIZE = 14.0;

    public GraphRenderer(Canvas canvas, VisualGraph visualGraph, Camera camera) {
        this.canvas = canvas;
        this.visualGraph = visualGraph;
        this.camera = camera;
    }

    public void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        
        camera.updateViewport(width, height);

        // Clear background
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, width, height);

        double zoom = camera.getZoom();

        // Draw edges
        gc.setStroke(EDGE_COLOR);
        gc.setLineWidth(1.5 * zoom);
        for (VisualEdge edge : visualGraph.getEdges()) {
            Point p1 = camera.worldToScreen(edge.getSource().getX(), edge.getSource().getY());
            Point p2 = camera.worldToScreen(edge.getTarget().getX(), edge.getTarget().getY());
            gc.strokeLine(p1.x(), p1.y(), p2.x(), p2.y());
        }

        // Draw nodes
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        
        for (VisualNode node : visualGraph.getNodes()) {
            Point p = camera.worldToScreen(node.getX(), node.getY());
            int depth = node.getDepth();
            
            // Depth Hierarchy Math
            // Decrease size, opacity, and text size based on depth
            double scale = Math.max(0.4, 1.0 - (depth * 0.15));
            double radius = BASE_RADIUS * scale * zoom;
            double opacity = Math.max(0.3, 1.0 - (depth * 0.2));

            gc.setGlobalAlpha(opacity);
            
            // Determine Color
            if (node.isSelected()) {
                gc.setFill(NODE_SELECTED_COLOR);
            } else if (depth == 0) {
                gc.setFill(ROOT_COLOR);
            } else {
                gc.setFill(NODE_COLOR);
            }
            
            // Draw circle
            gc.fillOval(p.x() - radius, p.y() - radius, radius * 2, radius * 2);

            // Draw label
            double fontSize = BASE_FONT_SIZE * scale * zoom;
            if (fontSize > 4.0) { // Culling tiny unreadable text
                gc.setFill(TEXT_COLOR);
                gc.setFont(Font.font(FONT_FAMILY, fontSize));
                gc.fillText(node.getDomainNode().getLabel(), p.x(), p.y() + radius + (10 * zoom));
            }
            
            gc.setGlobalAlpha(1.0); // Reset alpha
        }
    }
}
