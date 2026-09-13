package com.aporia.ui.camera;

import com.aporia.graph.layout.Point;
import com.aporia.ui.animation.Interpolator;

/**
 * Handles mapping between Screen (Canvas) and World (Layout) coordinates,
 * with support for subtle inertia and smooth transitions.
 */
public class Camera {
    private double currentXOffset = 0;
    private double currentYOffset = 0;
    private double currentZoom = 1.0;

    private double targetXOffset = 0;
    private double targetYOffset = 0;
    private double targetZoom = 1.0;

    private double viewportWidth = 800;
    private double viewportHeight = 600;

    private boolean reducedMotion = false;

    public void setReducedMotion(boolean reducedMotion) {
        this.reducedMotion = reducedMotion;
        if (reducedMotion) {
            this.currentXOffset = targetXOffset;
            this.currentYOffset = targetYOffset;
            this.currentZoom = targetZoom;
        }
    }

    public void updateViewport(double width, double height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    public void pan(double dx, double dy) {
        // Adjust targets relative to the current zoom level for consistent panning feel
        this.targetXOffset += dx / currentZoom;
        this.targetYOffset += dy / currentZoom;
        
        if (reducedMotion) {
            this.currentXOffset = targetXOffset;
            this.currentYOffset = targetYOffset;
        }
    }

    public void reset() {
        this.targetXOffset = 0;
        this.targetYOffset = 0;
        this.targetZoom = 1.0;
        if (reducedMotion) {
            this.currentXOffset = 0;
            this.currentYOffset = 0;
            this.currentZoom = 1.0;
        }
    }

    public void zoom(double factor, double mouseScreenX, double mouseScreenY) {
        // Pivot around the mouse coordinate in world space
        Point worldBefore = screenToWorld(mouseScreenX, mouseScreenY);
        
        double newTargetZoom = this.targetZoom * factor;
        if (newTargetZoom < 0.1) newTargetZoom = 0.1;
        if (newTargetZoom > 10.0) newTargetZoom = 10.0;
        
        this.targetZoom = newTargetZoom;
        
        // Calculate where the mouse WOULD be if we applied target zoom and offset instantly
        double worldXAfter = (mouseScreenX - (viewportWidth / 2)) / targetZoom - targetXOffset;
        double worldYAfter = (mouseScreenY - (viewportHeight / 2)) / targetZoom - targetYOffset;
        
        // Adjust target offset to keep the mouse point stationary
        this.targetXOffset += worldXAfter - worldBefore.x();
        this.targetYOffset += worldYAfter - worldBefore.y();
        
        if (reducedMotion) {
            this.currentZoom = targetZoom;
            this.currentXOffset = targetXOffset;
            this.currentYOffset = targetYOffset;
        }
    }

    /**
     * Steps the inertia simulation.
     * @param dt delta time in seconds
     */
    public void update(double dt) {
        if (reducedMotion) {
            return;
        }

        // Use an exponential decay approximation for frame-rate independent lerp
        // A rate of ~12.0 gives a quick, heavy, physical settling without lingering
        double rate = 12.0;
        double factor = 1.0 - Math.exp(-rate * dt);

        currentXOffset = Interpolator.lerp(currentXOffset, targetXOffset, factor);
        currentYOffset = Interpolator.lerp(currentYOffset, targetYOffset, factor);
        currentZoom = Interpolator.lerp(currentZoom, targetZoom, factor);

        // Snap to target if very close to prevent micro-jitter calculations
        if (Math.abs(currentXOffset - targetXOffset) < 0.001) currentXOffset = targetXOffset;
        if (Math.abs(currentYOffset - targetYOffset) < 0.001) currentYOffset = targetYOffset;
        if (Math.abs(currentZoom - targetZoom) < 0.0001) currentZoom = targetZoom;
    }

    public Point worldToScreen(double worldX, double worldY) {
        double screenX = (worldX + currentXOffset) * currentZoom + (viewportWidth / 2);
        double screenY = (worldY + currentYOffset) * currentZoom + (viewportHeight / 2);
        return new Point(screenX, screenY);
    }

    public Point screenToWorld(double screenX, double screenY) {
        double worldX = (screenX - (viewportWidth / 2)) / currentZoom - currentXOffset;
        double worldY = (screenY - (viewportHeight / 2)) / currentZoom - currentYOffset;
        return new Point(worldX, worldY);
    }

    public double getZoom() {
        return currentZoom;
    }

    // Exposed for testing
    public double getTargetXOffset() { return targetXOffset; }
    public double getCurrentXOffset() { return currentXOffset; }
}
