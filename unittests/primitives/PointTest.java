package primitives;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Point}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Point#subtract(Point)}</li>
 * <li>{@link Point#add(Vector)}</li>
 * <li>{@link Point#distanceSquared(Point)}</li>
 * <li>{@link Point#distance(Point)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author ityy35-bit
 */
class PointTest {

    /** Default constructor to satisfy JavaDoc generator */
    PointTest() { /* to satisfy JavaDoc generator */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** A general point (-1,1,1) */
    private static final Point P1 = new Point(-1, 1, 1);
    /** A general point (0,2,2) */
    private static final Point P2 = new Point(0, 2, 2);
    /** A point (0,2,1) at distance 1 from {@link #P2} */
    private static final Point P3 = new Point(0, 2, 1);
    /** The vector from {@link #P2} to {@link #P1} */
    private static final Vector P2_TO_P1 = new Vector(-1, -1, -1);
    /** The vector that moves {@link #P1} to the origin */
    private static final Vector P1_TO_ORIGIN = new Vector(1, -1, -1);

    /**
     * Test method for {@link Point#subtract(Point)}.
     */
    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Subtracting two different points
        assertEquals(P2_TO_P1, P1.subtract(P2), "subtract() returned a wrong vector");

        // TC02: Adding the difference back gives the original point
        assertEquals(P1, P2.add(P1.subtract(P2)), "p2 + (p1 - p2) is not p1");

        // =============== Boundary Values Tests ==================

        // TC11: Subtracting a point from itself creates the zero vector
        assertThrows(IllegalArgumentException.class, () -> P1.subtract(P1),
                "subtract() of a point from itself did not throw");
    }

    /**
     * Test method for {@link Point#add(Vector)}.
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Adding a vector to a point
        assertEquals(P1, P2.add(P2_TO_P1), "add() returned a wrong point");

        // =============== Boundary Values Tests ==================

        // TC11: Adding a vector that leads to the origin
        assertEquals(Point.ZERO, P1.add(P1_TO_ORIGIN), "add() did not reach the origin");
    }

    /**
     * Test method for {@link Point#distanceSquared(Point)}.
     */
    @Test
    void testDistanceSquared() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Squared distance between two different points
        assertEquals(3, P2.distanceSquared(P1), DELTA, "distanceSquared() returned a wrong value");

        // TC02: Squared distance is symmetric
        assertEquals(P1.distanceSquared(P2), P2.distanceSquared(P1), DELTA, "distanceSquared() is not symmetric");

        // =============== Boundary Values Tests ==================

        // TC11: Squared distance from a point to itself
        assertEquals(0, P1.distanceSquared(P1), DELTA, "distanceSquared() to itself is not zero");
    }

    /**
     * Test method for {@link Point#distance(Point)}.
     */
    @Test
    void testDistance() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Distance between two different points
        assertEquals(Math.sqrt(3), P2.distance(P1), DELTA, "distance() returned a wrong value");

        // TC02: Distance is symmetric
        assertEquals(P1.distance(P2), P2.distance(P1), DELTA, "distance() is not symmetric");

        // TC03: Distance obeys the triangle inequality
        assertTrue(P1.distance(P2) + P2.distance(P3) >= P1.distance(P3), "distance() violates the triangle inequality");

        // =============== Boundary Values Tests ==================

        // TC11: Distance from a point to itself
        assertEquals(0, P1.distance(P1), DELTA, "distance() to itself is not zero");
    }
}
