package lighting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for {@link SpotLight}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link SpotLight#getL(Point)}</li>
 * <li>{@link SpotLight#getIntensity(Point)}</li>
 * <li>{@link SpotLight#setNarrowBeam(int)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author ityy35-bit
 */
class SpotLightTest {

    /** Default constructor to satisfy JavaDoc generator */
    SpotLightTest() { /* to satisfy JavaDoc generator */ }

    /** The intensity of the light */
    private static final Color INTENSITY = new Color(200, 200, 200);
    /** The position of the light */
    private static final Point POSITION = new Point(0, 10, 0);
    /** The beam direction - straight down */
    private static final Vector DIRECTION = new Vector(0, -1, 0);
    /** The attenuation at distance 10: 1 / (1 + 0.1*10 + 0.01*100) */
    private static final double ATTENUATION = 1.0 / 3;
    /** A point on the beam axis, 10 units in front of the light */
    private static final Point ON_AXIS = Point.ZERO;
    /** A point 45° off the beam axis */
    private static final Point OFF_AXIS = new Point(10, 0, 0);
    /** A point behind the light */
    private static final Point BEHIND = new Point(0, 20, 0);
    /** A point exactly sideways from the light (90° off axis) */
    private static final Point SIDEWAYS = new Point(10, 10, 0);

    /**
     * Creates the light under test.
     *
     * @return a spot light with attenuation factors kC=1, kL=0.1, kQ=0.01
     */
    private static SpotLight light() {
        return new SpotLight(INTENSITY, POSITION, DIRECTION).setKc(1).setKl(0.1).setKq(0.01);
    }

    /**
     * Test method for {@link SpotLight#getL(Point)}.
     */
    @Test
    void testGetL() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The direction from the light to a point
        assertEquals(DIRECTION, light().getL(ON_AXIS), "Wrong light direction");

        // =============== Boundary Values Tests ==================

        // TC11: The point coincides with the light
        assertThrows(IllegalArgumentException.class, () -> light().getL(POSITION),
                "getL() at the light position did not throw");
    }

    /**
     * Test method for {@link SpotLight#getIntensity(Point)}.
     */
    @Test
    void testGetIntensity() {
        SpotLight light = light();
        double cos45 = Math.sqrt(0.5);
        double attenuation45 = 1 / (1 + 0.1 * Math.sqrt(200) + 0.01 * 200);

        // ============ Equivalence Partitions Tests ==============

        // TC01: A point on the beam axis gets the full attenuated intensity
        assertEquals(INTENSITY.scale(ATTENUATION), light.getIntensity(ON_AXIS), "Wrong intensity on the axis");

        // TC02: A point off the axis is dimmed by the cosine of the angle
        assertEquals(INTENSITY.scale(attenuation45 * cos45), light.getIntensity(OFF_AXIS), "Wrong intensity off the axis");

        // TC03: A point behind the light gets nothing
        assertEquals(Color.BLACK, light.getIntensity(BEHIND), "A point behind the light is lit");

        // =============== Boundary Values Tests ==================

        // TC11: A point exactly sideways gets nothing
        assertEquals(Color.BLACK, light.getIntensity(SIDEWAYS), "A point sideways of the light is lit");
    }

    /**
     * Test method for {@link SpotLight#setNarrowBeam(int)}.
     */
    @Test
    void testSetNarrowBeam() {
        double cos45 = Math.sqrt(0.5);
        double attenuation45 = 1 / (1 + 0.1 * Math.sqrt(200) + 0.01 * 200);

        // ============ Equivalence Partitions Tests ==============

        // TC01: A narrow beam dims off-axis points by a power of the cosine
        assertEquals(INTENSITY.scale(attenuation45 * Math.pow(cos45, 3)), light().setNarrowBeam(3).getIntensity(OFF_AXIS),
                "Wrong intensity of a narrow beam");

        // TC02: An even exponent must not light the half-space behind the light
        assertEquals(Color.BLACK, light().setNarrowBeam(10).getIntensity(BEHIND),
                "An even beam exponent lit a point behind the light");

        // =============== Boundary Values Tests ==================

        // TC11: An exponent below 1
        assertThrows(IllegalArgumentException.class, () -> light().setNarrowBeam(0), "A zero exponent was accepted");
    }
}
