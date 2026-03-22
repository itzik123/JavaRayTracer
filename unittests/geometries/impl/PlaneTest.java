package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for class {@link Plane}.
 * The tests verify:
 * <ul>
 * <li>{@link Plane#Plane(Point, Point, Point)}</li>
 * <li>{@link Plane#Plane(Point, Vector)}</li>
 * <li>{@link Plane#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author itzik123
 */
class PlaneTest {

    /** Default constructor to satisfy JavaDoc generator */
    PlaneTest() { /* to satisfy JavaDoc generator */ }

    /** Vertex (1,0,0) used in plane tests */
    private static final Point  POINT_X       = new Point(1, 0, 0);
    /** Vertex (2,0,0) used in plane tests */
    private static final Point  POINT_2X       = new Point(2, 0, 0);
    /** Vertex (3,0,0) used in plane tests */
    private static final Point  POINT_3X       = new Point(3, 0, 0);
    /** Vertex (0,1,0) used in plane tests */
    private static final Point  POINT_Y       = new Point(0, 1, 0);
    /** Vertex (0,0,1) used in plane tests */
    private static final Point  POINT_Z       = new Point(0, 0, 1);
    /** Vertex (1,0,1) used in plane tests */
    private static final Point  POINT_EP       = new Point(1, 0, 1);
    /** normal vector used in tests */
    private static final Vector VECTOR = new Vector(2, 0, 0);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * test method for {@link Plane#Plane(Point, Point, Point)}
     */
    @Test
    void test3PointConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: constructing a valid Plain
        assertDoesNotThrow(() -> new Plane(POINT_X, POINT_Y, POINT_Z),
                "ERROR: Failed constructing a correct plane");

        // =============== Boundary Values Tests ==================

        // TC11: constructing an invalid plane with 2 converging points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_X, POINT_Y),
                "ERROR: The program should throw an exception for constructing a plane with 2 converging points");

        // TC12: constructing an invalid plane with 3 converging points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_X, POINT_X),
                "ERROR: The program should throw an exception for constructing a plane with 3 converging points");

        // TC13: constructing an invalid plane with 3 points on the same line
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_2X, POINT_3X),
                "ERROR: The program should throw an exception for constructing a plane with 3 points on the same line");
    }

    /**
     * test method for {@link Plane#Plane(Point, Vector)}
     */
    @Test
    void testPointAndVectorConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: constructing a valid Plain and checking the normal
        Plane plane = new Plane(POINT_X, VECTOR);

        assertEquals(1, plane.getNormal(POINT_X).lengthSquared(), DELTA,
                "ERROR: Failed normalizing the vector");
    }

    /**
     * test method for {@link Plane#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: constructing a valid Plane and checking get normal
        Plane plane = new Plane(POINT_X, VECTOR);
        assertEquals(VECTOR.normalize(), plane.getNormal(POINT_EP),
                "ERROR: Failed giving the correct normal to the plane with some point on the plane");

        // =============== Boundary Values Tests ==================

        // TC11: checking get normal with the reference point of the plane
        assertEquals(VECTOR.normalize(), plane.getNormal(POINT_X),
                "ERROR: Failed giving the correct normal to the plane with the reference point of the plane");
    }
}