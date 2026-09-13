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
 */
public class GraphRenderer {
    private final Canvas canvas;
    private final VisualGraph visualGraph;
    private final Camera camera;

    private static final double NODE_RADIUS = 20.0;
    
    // Neutral visual style for Phase 2
    private static final Color BG_COLOR = Color.web("#222222");
    private static final Color EDGE_COLOR = Color.web("#555555");
    private static final Color NODE_COLOR = Color.web("#888888");
    private static final Color NODE_SELECTED_COLOR = Color.web("#EEEEEE");
    private static final Color TEXT_COLOR = Color.web("#DDDDDD");

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

        // Draw edges
        gc.setStroke(EDGE_COLOR);
        gc.setLineWidth(1.5 * camera.getZoom());
        for (VisualEdge edge : visualGraph.getEdges()) {
            Point p1 = camera.worldToScreen(edge.getSource().getX(), edge.getSource().getY());
            Point p2 = camera.worldToScreen(edge.getTarget().getX(), edge.getTarget().getY());
            gc.strokeLine(p1.x(), p1.y(), p2.x(), p2.y());
        }

        // Draw nodes
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        
        double currentRadius = NODE_RADIUS * camera.getZoom();
        
        for (VisualNode node : visualGraph.getNodes()) {
            Point p = camera.worldToScreen(node.getX(), node.getY());
            
            // Draw circle
            if (node.isSelected()) {
                gc.setFill(NODE_SELECTED_COLOR);
            } else {
                gc.setFill(NODE_COLOR);
            }
            gc.fillOval(p.x() - currentRadius, p.y() - currentRadius, currentRadius * 2, currentRadius * 2);

            // Draw label
            gc.setFill(TEXT_COLOR);
            gc.setFont(new Font(12 * camera.getZoom()));
            // Position label below the node
            gc.fillText(node.getDomainNode().getLabel(), p.x(), p.y() + currentRadius + (10 * camera.getZoom()));
        }
    }
}
