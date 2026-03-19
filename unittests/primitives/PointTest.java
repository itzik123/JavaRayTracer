package primitives;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;



/**
 * Unit tests for class {@link Point}.
 * The tests verify:
 * <ul>
 * <li>{@link Point#subtract(Point)}</li>
 * <li>{@link Point#add(Vector)}</li>
 * <li>{@link Point#distance(Point)}</li>
 * <li>{@link Point#distanceSquared(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author ityy35-bit
 */
class PointTest {

    /** Default constructor to satisfy JavaDoc generator */
    PointTest() { /* to satisfy JavaDoc generator */ }

    /** first point for point tests */
    private static final Point  POINT1        = new Point(-1, 1, 1);
    /** Point not in the polygon plane */
    private static final Point  POINT2        = new Point(0, 2, 2);
    /** Vector that take POINT1 to ZERO */
    private static final Vector  VECTOR1        = new Vector(1, -1, -1);
    /** Vector from POINT2 to POINT1 */
    private static final Vector  VECTOR2        = new Vector(-1,-1,-1);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA         = 1e-6;

    /**
     * Test method for {@link Point#subtract(Point)}.
     * Verifies that the returned vector from the subtraction define correctly
     * including when it should not create it
     */
    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: regular substruction
        assertEquals(POINT1.subtract(POINT2),VECTOR2,
                "ERROR : Create the wrong vector");

        // =============== Boundary Values Tests ==================

        // TC11: substruct the point by itself
        assertThrows(IllegalArgumentException.class, () -> POINT1.subtract(POINT1),
                "ERROR : The program should throw an exception");

    }

    /**
     * Test method for {@link Point#add(Vector)}.
     * Verifies that the returned point from the addition define correctly
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: regular addition
        assertEquals(POINT2.add(VECTOR2),POINT1,
                "ERROR : Create the wrong point");

        // =============== Boundary Values Tests ==================

        // TC11: addition to ZERO
        assertEquals(POINT1.add(VECTOR1), Point.ZERO,
                "ERROR : Create the wrong point");

    }

    /**
     * Test method for {@link Point#distanceSquared(Point)}.
     * Verifies that the returned squared distance from the two points define correctly
     */
    @Test
    void testDistanceSquared() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: regular squared distance
        assertEquals(POINT2.distanceSquared(POINT1),3, DELTA,
                "ERROR : Calculate the wrong squared distance");

        // =============== Boundary Values Tests ==================

        // TC11: squared distance from point to itself
        assertEquals(POINT1.distanceSquared(POINT1), 0, DELTA,
                "ERROR : Squared distance should be zero");

        // TC12: squared distance from 2 ways
        assertEquals(POINT1.distanceSquared(POINT2), POINT2.distanceSquared(POINT1), DELTA,
                "ERROR : Squared distance should be commutative");

    }
    /**
     * Test method for {@link Point#distance(Point)}.
     * Verifies that the returned distance from the two points define correctly
     */
    @Test
    void testDistance() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: regular squared distance
        assertEquals(POINT2.distance(POINT1),Math.sqrt(3), DELTA,
                "ERROR : Calculate the wrong distance");

        // =============== Boundary Values Tests ==================

        // TC11: distance from point to itself
        assertEquals(POINT1.distance(POINT1), 0, DELTA,
                "ERROR : Distance should be zero");

        // TC12: distance from 2 ways
        assertEquals(POINT1.distance(POINT2), POINT2.distance(POINT1), DELTA,
                "ERROR : Distance should be commutative");

    }
}