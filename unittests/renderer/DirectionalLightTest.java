package renderer;

import lighting.DirectionalLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link lighting.DirectionalLight}.
 */
class DirectionalLightTest {

    /**
     * Test method for {@link lighting.DirectionalLight#getL(Point)}
     * and {@link lighting.DirectionalLight#getIntensity(Point)}.
     */
    @Test
    void testDirectionalLight() {
        Vector dir = new Vector(0, -1, 0);
        Color color = new Color(255, 0, 0); // Red light
        DirectionalLight light = new DirectionalLight(color, dir);
        Point p = new Point(0, 0, 0);

        // ============ Equivalence Partitions Tests ==============
        // TC01: Any point - Direction should be exactly the normalized direction of the light
        assertEquals(dir.normalize(), light.getL(p), "Wrong direction for DirectionalLight");

        // TC02: Any point - Intensity should be exactly the original intensity (no attenuation)
        assertEquals(color, light.getIntensity(p), "Wrong intensity for DirectionalLight");
    }
}