package lighting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;

/**
 * Unit tests for {@link PointLight}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link PointLight#getL(Point)}</li>
 * <li>{@link PointLight#getIntensity(Point)}</li>
 * <li>{@link PointLight#getDistance(Point)}</li>
 * <li>{@link PointLight#setSoftShadows(double, int)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author ityy35-bit
 */
class PointLightTest {

    /** Default constructor to satisfy JavaDoc generator */
    PointLightTest() { /* to satisfy JavaDoc generator */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** The intensity of the light */
    private static final Color INTENSITY = new Color(200, 200, 200);
    /** The position of the light */
    private static final Point POSITION = new Point(0, 10, 0);
    /** The light under test, with all three attenuation factors */
    private static final PointLight LIGHT = new PointLight(INTENSITY, POSITION).setKc(1).setKl(0.1).setKq(0.01);

    /**
     * Test method for {@link PointLight#getL(Point)}.
     */
    @Test
    void testGetL() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The direction from the light to a point
        assertEquals(Point.ZERO.subtract(POSITION).normalize(), LIGHT.getL(Point.ZERO), "Wrong light direction");

        // =============== Boundary Values Tests ==================

        // TC11: The point coincides with the light
        assertThrows(IllegalArgumentException.class, () -> LIGHT.getL(POSITION),
                "getL() at the light position did not throw");
    }

    /**
     * Test method for {@link PointLight#getIntensity(Point)}.
     */
    @Test
    void testGetIntensity() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Attenuation at distance 10: 1 / (1 + 0.1*10 + 0.01*100) = 1/3
        assertEquals(INTENSITY.scale(1.0 / 3), LIGHT.getIntensity(Point.ZERO), "Wrong attenuated intensity");

        // =============== Boundary Values Tests ==================

        // TC11: At the light position only the constant factor applies
        assertEquals(INTENSITY, LIGHT.getIntensity(POSITION), "Wrong intensity at the light position");
    }

    /**
     * Test method for {@link PointLight#getDistance(Point)}.
     */
    @Test
    void testGetDistance() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The distance from the light to a point
        assertEquals(10, LIGHT.getDistance(Point.ZERO), DELTA, "Wrong light distance");
    }

    /**
     * Test method for {@link PointLight#setSoftShadows(double, int)}.
     */
    @Test
    void testSetSoftShadows() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A light with a radius, sampled by several shadow rays
        PointLight soft = new PointLight(INTENSITY, POSITION).setSoftShadows(5, 16);
        assertEquals(5, soft.getRadius(), DELTA, "Wrong light radius");
        assertEquals(16, soft.getShadowRays(), "Wrong number of shadow rays");

        // =============== Boundary Values Tests ==================

        // TC11: By default the light is an ideal point with hard shadows
        assertEquals(0, LIGHT.getRadius(), DELTA, "A default light has a radius");
        assertEquals(1, LIGHT.getShadowRays(), "A default light samples several shadow rays");

        // TC12: A zero radius is allowed - hard shadows
        assertEquals(0, new PointLight(INTENSITY, POSITION).setSoftShadows(0, 1).getRadius(), DELTA,
                "A zero radius was not accepted");

        // TC13: A negative radius
        assertThrows(IllegalArgumentException.class, () -> new PointLight(INTENSITY, POSITION).setSoftShadows(-1, 4),
                "A negative radius did not throw");

        // TC14: No shadow rays
        assertThrows(IllegalArgumentException.class, () -> new PointLight(INTENSITY, POSITION).setSoftShadows(5, 0),
                "Zero shadow rays did not throw");
    }
}
