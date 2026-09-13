package com.aporia.ui.animation;

/**
 * Pure mathematical utilities for smooth interpolation and easing.
 */
public class Interpolator {

    /**
     * Linearly interpolates between start and end by factor t [0.0, 1.0].
     */
    public static double lerp(double start, double end, double t) {
        // Clamp t to [0, 1] to ensure we don't overshoot unnecessarily in standard lerp
        t = Math.max(0.0, Math.min(1.0, t));
        return start + (end - start) * t;
    }

    /**
     * Smooth ease-out interpolation.
     * Starts fast and decelerates as it approaches the end.
     */
    public static double easeOut(double start, double end, double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        double easeT = t * (2.0 - t); // Quadratic ease out
        return start + (end - start) * easeT;
    }
}
