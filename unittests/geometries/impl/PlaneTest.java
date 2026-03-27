package geometries.impl;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for class {@link Plane}.
 * The tests verify:
 * <ul>
 * <li>{@link Plane#Plane(Point, Point, Point)}</li>
 * <li>{@link Plane#Plane(Point, Vector)}</li>
 * <li>{@link Plane#getNormal(Point)}</li>
 * <li>{@link Plane#findIntersections(primitives.Ray)}</li>
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

    private static final String ERROR_PLANE_INTERSECTION = "Wrong plane intersection result";

    /** The plane used for all tests (The XY plane, z=0) */
    private static final Plane PLANE = new Plane(new Point(0, 0, 0), new Vector(0, 0, 1));

    // ================= Ray definitions for EP tests =================
    private static final Ray EP01 = new Ray(new Point(0, 0, 1), new Vector(-1, -1, -1));
    private static final Ray EP02 = new Ray(new Point(0, 0, 1), new Vector(1, 1, 1));

    // ================= Ray definitions for BVA tests =================
    // Group 1: Ray is parallel to the plane
    private static final Ray BV11 = new Ray(new Point(0, 1, 0), new Vector(1, 0, 0)); // Included in the plane
    private static final Ray BV12 = new Ray(new Point(0, 0, 1), new Vector(1, 0, 0)); // Not included in the plane

    // Group 2: Ray is orthogonal to the plane
    private static final Ray BV21 = new Ray(new Point(0, 0, -1), new Vector(0, 0, 1)); // Starts before the plane
    private static final Ray BV22 = new Ray(new Point(0, 0, 0), new Vector(0, 0, 1));  // Starts in the plane
    private static final Ray BV23 = new Ray(new Point(0, 0, 1), new Vector(0, 0, 1));  // Starts after the plane

    // Group 3: Ray is neither orthogonal nor parallel and begins on the plane
    private static final Ray BV31 = new Ray(new Point(1, 1, 0), new Vector(1, 1, 1)); // Begins on the plane (not reference point)
    private static final Ray BV32 = new Ray(new Point(0, 0, 0), new Vector(1, 1, 1)); // Begins at the reference point

    // ================= Expected Intersections Lists =================
    private static final List<Point> INTERSECTION_EP01 = List.of(new Point(-1, -1, 0));
    private static final List<Point> INTERSECTION_BV21 = List.of(new Point(0, 0, 0));

    /**
     * Test method for {@link Plane#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray intersects the plane at one point
        assertEquals(INTERSECTION_EP01, PLANE.findIntersections(EP01), ERROR_PLANE_INTERSECTION);

        // TC02: Ray does not intersect the plane
        assertNull(PLANE.findIntersections(EP02), ERROR_PLANE_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray is parallel to the plane
        // TC11: The ray is included in the plane (0 points)
        assertNull(PLANE.findIntersections(BV11), ERROR_PLANE_INTERSECTION);

        // TC12: The ray is not included in the plane (0 points)
        assertNull(PLANE.findIntersections(BV12), ERROR_PLANE_INTERSECTION);

        // **** Group 2: Ray is orthogonal to the plane
        // TC21: Ray starts before the plane (1 point)
        assertEquals(INTERSECTION_BV21, PLANE.findIntersections(BV21), ERROR_PLANE_INTERSECTION);

        // TC22: Ray starts in the plane (0 points, because intersection must be strictly t > 0)
        assertNull(PLANE.findIntersections(BV22), ERROR_PLANE_INTERSECTION);

        // TC23: Ray starts after the plane (0 points)
        assertNull(PLANE.findIntersections(BV23), ERROR_PLANE_INTERSECTION);

        // **** Group 3: Ray is neither orthogonal nor parallel and begins on the plane
        // TC31: Ray begins on the plane (0 points, t = 0)
        assertNull(PLANE.findIntersections(BV31), ERROR_PLANE_INTERSECTION);

        // TC32: Ray begins at the reference point of the plane (0 points, t = 0)
        assertNull(PLANE.findIntersections(BV32), ERROR_PLANE_INTERSECTION);
    }
}