package com.aporia.ui.animation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InterpolatorTest {

    @Test
    public void testLerpEndpoints() {
        assertEquals(0.0, Interpolator.lerp(0.0, 100.0, 0.0), 0.001);
        assertEquals(100.0, Interpolator.lerp(0.0, 100.0, 1.0), 0.001);
    }

    @Test
    public void testLerpMidpoint() {
        assertEquals(50.0, Interpolator.lerp(0.0, 100.0, 0.5), 0.001);
        assertEquals(25.0, Interpolator.lerp(0.0, 100.0, 0.25), 0.001);
    }

    @Test
    public void testLerpClamping() {
        // Should clamp t to 1.0
        assertEquals(100.0, Interpolator.lerp(0.0, 100.0, 1.5), 0.001);
        // Should clamp t to 0.0
        assertEquals(0.0, Interpolator.lerp(0.0, 100.0, -0.5), 0.001);
    }

    @Test
    public void testEaseOutEndpoints() {
        assertEquals(0.0, Interpolator.easeOut(0.0, 100.0, 0.0), 0.001);
        assertEquals(100.0, Interpolator.easeOut(0.0, 100.0, 1.0), 0.001);
    }

    @Test
    public void testEaseOutMonotonicity() {
        double prev = -1.0;
        for (double t = 0.0; t <= 1.0; t += 0.1) {
            double current = Interpolator.easeOut(0.0, 100.0, t);
            assertTrue(current >= prev, "EaseOut must be monotonically increasing");
            prev = current;
        }
    }
    
    @Test
    public void testEaseOutShape() {
        // At t=0.5, easeOut should be further along than linear
        double linear = Interpolator.lerp(0.0, 100.0, 0.5);
        double eased = Interpolator.easeOut(0.0, 100.0, 0.5);
        assertTrue(eased > linear, "EaseOut should decelerate (start fast)");
        assertEquals(75.0, eased, 0.001); // 0.5 * (2 - 0.5) = 0.75
    }
}
