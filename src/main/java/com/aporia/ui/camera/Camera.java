package com.aporia.ui.camera;

import com.aporia.graph.layout.Point;

/**
 * Handles mapping between Screen (Canvas) and World (Layout) coordinates.
 */
public class Camera {
    private double xOffset = 0;
    private double yOffset = 0;
    private double zoom = 1.0;
    private double viewportWidth = 800;
    private double viewportHeight = 600;

    public void updateViewport(double width, double height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    public void pan(double dx, double dy) {
        this.xOffset += dx / zoom;
        this.yOffset += dy / zoom;
    }

    public void zoom(double factor, double mouseScreenX, double mouseScreenY) {
        Point worldBefore = screenToWorld(mouseScreenX, mouseScreenY);
        
        double newZoom = this.zoom * factor;
        // Restrict zoom limits
        if (newZoom < 0.1) newZoom = 0.1;
        if (newZoom > 10.0) newZoom = 10.0;
        
        this.zoom = newZoom;
        
        Point worldAfter = screenToWorld(mouseScreenX, mouseScreenY);
        
        // Adjust offset to zoom around mouse
        this.xOffset += worldAfter.x() - worldBefore.x();
        this.yOffset += worldAfter.y() - worldBefore.y();
    }

    public Point worldToScreen(double worldX, double worldY) {
        double screenX = (worldX + xOffset) * zoom + (viewportWidth / 2);
        double screenY = (worldY + yOffset) * zoom + (viewportHeight / 2);
        return new Point(screenX, screenY);
    }

    public Point screenToWorld(double screenX, double screenY) {
        double worldX = (screenX - (viewportWidth / 2)) / zoom - xOffset;
        double worldY = (screenY - (viewportHeight / 2)) / zoom - yOffset;
        return new Point(worldX, worldY);
    }

    public double getZoom() {
        return zoom;
    }
}
