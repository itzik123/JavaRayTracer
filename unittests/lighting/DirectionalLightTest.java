package lighting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for {@link DirectionalLight}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link DirectionalLight#getL(Point)}</li>
 * <li>{@link DirectionalLight#getIntensity(Point)}</li>
 * <li>{@link DirectionalLight#getDistance(Point)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) methodology.
 *
 * @author ityy35-bit
 */
class DirectionalLightTest {

    /** Default constructor to satisfy JavaDoc generator */
    DirectionalLightTest() { /* to satisfy JavaDoc generator */ }

    /** The intensity of the light */
    private static final Color INTENSITY = new Color(255, 0, 0);
    /** The (non-unit) direction of the light */
    private static final Vector DIRECTION = new Vector(0, -2, 0);
    /** The light under test */
    private static final DirectionalLight LIGHT = new DirectionalLight(INTENSITY, DIRECTION);
    /** An illuminated point */
    private static final Point POINT = new Point(3, -4, 5);

    /**
     * Test method for {@link DirectionalLight#getL(Point)}.
     */
    @Test
    void testGetL() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The direction is the normalized light direction at any point
        assertEquals(DIRECTION.normalize(), LIGHT.getL(POINT), "Wrong light direction");
    }

    /**
     * Test method for {@link DirectionalLight#getIntensity(Point)}.
     */
    @Test
    void testGetIntensity() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The intensity is not attenuated at any point
        assertEquals(INTENSITY, LIGHT.getIntensity(POINT), "Wrong light intensity");
    }

    /**
     * Test method for {@link DirectionalLight#getDistance(Point)}.
     */
    @Test
    void testGetDistance() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The light is infinitely far from any point
        assertEquals(Double.POSITIVE_INFINITY, LIGHT.getDistance(POINT), "Wrong light distance");
    }
}
