package renderer;

import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link lighting.SpotLight}.
 */
class SpotLightTest {

    /**
     * Test method for {@link lighting.SpotLight#getL(Point)}
     * and {@link lighting.SpotLight#getIntensity(Point)}.
     */
    @Test
    void testSpotLight() {
        Point position = new Point(0, 10, 0);
        Vector dir = new Vector(0, -1, 0);
        Color color = new Color(200, 200, 200);
        SpotLight light = (SpotLight) new SpotLight(color, position, dir).setKc(1).setKl(0.1).setKq(0.01);

        Point pFront = new Point(0, 0, 0);
        Point pBehind = new Point(0, 20, 0);
        Point p90 = new Point(10, 10, 0);

        // ============ Equivalence Partitions Tests ==============
        // TC01: Point in front of the spotlight - Check direction
        Vector expectedDirFront = pFront.subtract(position).normalize();
        assertEquals(expectedDirFront, light.getL(pFront), "Wrong direction for SpotLight (front)");

        // TC02: Point in front of the spotlight - Check intensity attenuation + angle factor
        double distance = 10;
        double attenuation = 1.0 / (1 + 0.1 * distance + 0.01 * distance * distance);
        double dirDotL = dir.normalize().dotProduct(expectedDirFront);
        assertEquals(color.scale(attenuation * dirDotL), light.getIntensity(pFront),
                "Wrong intensity for SpotLight (front)");

        // TC03: Point behind the spotlight
        assertEquals(Color.BLACK, light.getIntensity(pBehind), "Intensity should be black behind spotlight");

        // =============== Boundary Values Tests ==================
        // TC11: Point at exactly 90 degrees to the spotlight direction
        assertEquals(Color.BLACK, light.getIntensity(p90), "Intensity should be black at exactly 90 degrees");

        // TC12: Point coincides with light position - Vector calculation should fail
        assertThrows(IllegalArgumentException.class, () -> light.getL(position),
                "Expected exception when point coincides with light position");
    }
}