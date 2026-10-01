package primitives;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Unit tests for {@link Ray}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Ray#Ray(Point, Vector)}</li>
 * <li>{@link Ray#Ray(Point, Vector, Vector)}</li>
 * <li>{@link Ray#getPoint(double)}</li>
 * <li>{@link Ray#findClosestPoint(List)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author ityy35-bit
 */
class RayTest {

    /** Default constructor to satisfy JavaDoc generator */
    RayTest() { /* to satisfy JavaDoc generator */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** A ray from the origin along the X axis */
    private static final Ray RAY = new Ray(Point.ZERO, Vector.AXIS_X);
    /** The point (1,0,0), closest to the ray origin among the test points */
    private static final Point NEAR = new Point(1, 0, 0);
    /** The point (2,0,0) */
    private static final Point MIDDLE = new Point(2, 0, 0);
    /** The point (3,0,0), farthest from the ray origin among the test points */
    private static final Point FAR = new Point(3, 0, 0);

    /**
     * Test method for {@link Ray#Ray(Point, Vector)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The direction of a ray is normalized
        Ray ray = new Ray(new Point(-1, 1, 1), new Vector(2, 0, 1));
        assertEquals(1, ray.direction().length(), DELTA, "The ray direction is not normalized");
    }

    /**
     * Test method for {@link Ray#Ray(Point, Vector, Vector)}.
     */
    @Test
    void testOffsetConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A ray leaving the front of a surface is pushed off to the front
        Ray front = new Ray(Point.ZERO, new Vector(1, 1, 0), Vector.AXIS_Y);
        assertEquals(0.1, front.origin().getY(), DELTA, "The ray was not pushed towards its direction");

        // TC02: A ray leaving the back of a surface is pushed off to the back
        Ray back = new Ray(Point.ZERO, new Vector(1, -1, 0), Vector.AXIS_Y);
        assertEquals(-0.1, back.origin().getY(), DELTA, "The ray was not pushed towards its direction");
    }

    /**
     * Test method for {@link Ray#getPoint(double)}.
     */
    @Test
    void testGetPoint() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Positive distance
        assertEquals(MIDDLE, RAY.getPoint(2), "getPoint() returned a wrong point for a positive distance");

        // TC02: Negative distance
        assertEquals(new Point(-2, 0, 0), RAY.getPoint(-2), "getPoint() returned a wrong point for a negative distance");

        // =============== Boundary Values Tests ==================

        // TC11: Zero distance
        assertEquals(Point.ZERO, RAY.getPoint(0), "getPoint() returned a wrong point for zero distance");
    }

    /**
     * Test method for {@link Ray#findClosestPoint(List)}.
     */
    @Test
    void testFindClosestPoint() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The closest point is in the middle of the list
        assertEquals(NEAR, RAY.findClosestPoint(List.of(MIDDLE, NEAR, FAR)), "Wrong closest point");

        // =============== Boundary Values Tests ==================

        // TC11: An empty list
        assertNull(RAY.findClosestPoint(List.of()), "findClosestPoint() of an empty list is not null");

        // TC12: A null list
        assertNull(RAY.findClosestPoint(null), "findClosestPoint() of a null list is not null");

        // TC13: The closest point is the first in the list
        assertEquals(NEAR, RAY.findClosestPoint(List.of(NEAR, MIDDLE, FAR)), "Wrong closest point");

        // TC14: The closest point is the last in the list
        assertEquals(NEAR, RAY.findClosestPoint(List.of(FAR, MIDDLE, NEAR)), "Wrong closest point");
    }
}
