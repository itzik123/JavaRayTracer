package primitives;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for class {@link Ray}.
 * The tests verify:
 * <ul>
 * <li>Polygon constructor validity</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP).
 * @author ityy35-bit
 */
class RayTest {

    /** origin point for ray tests */
    private static final Point  POINT        = new Point(-1, 1, 1);

    /** direction vector for ray test (not normalized) */
    private static final Vector  VECTOR       = new Vector(2, 0, 1);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA         = 1e-6;

    /**
     * Test method for {@link Ray#Ray(Point,Vector)}.
     * Verifies correct and incorrect polygon constructions.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: regular addition
        final  Ray ray = new Ray(POINT,VECTOR);
        assertEquals(1,ray.getDirection().lengthSquared(), DELTA,
                "ERROR: direction vector not normalized");

    }

}