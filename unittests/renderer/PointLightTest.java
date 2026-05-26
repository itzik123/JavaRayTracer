package renderer;

import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link lighting.PointLight}.
 */
class PointLightTest {

    /**
     * Test method for {@link lighting.PointLight#getL(Point)}
     * and {@link lighting.PointLight#getIntensity(Point)}.
     */
    @Test
    void testPointLight() {
        Point position = new Point(0, 10, 0);
        Color color = new Color(200, 200, 200);
        PointLight light = new PointLight(color, position).setKc(1).setKl(0.1).setKq(0.01);
        Point p1 = new Point(0, 0, 0);

        // ============ Equivalence Partitions Tests ==============
        // TC01: Normal distance - Check direction
        Vector expectedDir = p1.subtract(position).normalize();
        assertEquals(expectedDir, light.getL(p1), "Wrong direction for PointLight");

        // TC02: Normal distance - Check intensity attenuation
        double distance = 10;
        double attenuation = 1.0 / (1 + 0.1 * distance + 0.01 * distance * distance);
        assertEquals(color.scale(attenuation), light.getIntensity(p1), "Wrong intensity for PointLight");

        // =============== Boundary Values Tests ==================
        // TC11: Point coincides with light position - Vector calculation should fail
        assertThrows(IllegalArgumentException.class, () -> light.getL(position),
                "Expected exception when point coincides with light position");

        // TC12: Point coincides with light position - Intensity should be exactly the original intensity (d=0)
        assertEquals(color, light.getIntensity(position), "Intensity should be full at light position");
    }
}