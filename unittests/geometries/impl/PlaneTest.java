package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Unit tests for {@link Plane}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Plane#Plane(Point, Point, Point)}</li>
 * <li>{@link Plane#Plane(Point, Vector)}</li>
 * <li>{@link Plane#getNormal(Point)}</li>
 * <li>{@link Plane#findIntersections(Ray)}</li>
 * <li>{@link Plane#calcIntersections(Ray, double)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class PlaneTest {

    /** Default constructor to satisfy JavaDoc generator */
    PlaneTest() { /* to satisfy JavaDoc generator */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** The point (1,0,0) */
    private static final Point POINT_X = new Point(1, 0, 0);
    /** The point (2,0,0), on the line of {@link #POINT_X} and {@link #POINT_3X} */
    private static final Point POINT_2X = new Point(2, 0, 0);
    /** The point (3,0,0) */
    private static final Point POINT_3X = new Point(3, 0, 0);
    /** The point (0,1,0) */
    private static final Point POINT_Y = new Point(0, 1, 0);
    /** The point (0,0,1) */
    private static final Point POINT_Z = new Point(0, 0, 1);
    /** A non-unit normal (2,0,0) */
    private static final Vector NORMAL = new Vector(2, 0, 0);

    /** The XY plane, used for the intersection tests */
    private static final Plane XY_PLANE = new Plane(Point.ZERO, Vector.AXIS_Z);
    /** Message for wrong intersection results */
    private static final String ERROR_INTERSECTION = "Wrong plane intersection result";

    /**
     * Test method for {@link Plane#Plane(Point, Point, Point)}.
     */
    @Test
    void testThreePointsConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Three non-collinear points
        assertDoesNotThrow(() -> new Plane(POINT_X, POINT_Y, POINT_Z), "Failed constructing a correct plane");

        // TC02: The normal is a unit vector orthogonal to the plane
        Plane plane = new Plane(POINT_X, POINT_Y, POINT_Z);
        Vector normal = plane.getNormal(POINT_X);
        assertEquals(1, normal.length(), DELTA, "The plane normal is not a unit vector");
        assertEquals(0, normal.dotProduct(POINT_Y.subtract(POINT_X)), DELTA, "The normal is not orthogonal to the plane");
        assertEquals(0, normal.dotProduct(POINT_Z.subtract(POINT_X)), DELTA, "The normal is not orthogonal to the plane");

        // =============== Boundary Values Tests ==================

        // TC11: Two coincident points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_X, POINT_Y),
                "Constructed a plane from two coincident points");

        // TC12: Three coincident points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_X, POINT_X),
                "Constructed a plane from three coincident points");

        // TC13: Three collinear points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_2X, POINT_3X),
                "Constructed a plane from three collinear points");
    }

    /**
     * Test method for {@link Plane#Plane(Point, Vector)}.
     */
    @Test
    void testPointAndNormalConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The given normal is normalized
        assertEquals(1, new Plane(POINT_X, NORMAL).getNormal(POINT_X).length(), DELTA,
                "The plane normal is not a unit vector");
    }

    /**
     * Test method for {@link Plane#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        Plane plane = new Plane(POINT_X, NORMAL);

        // ============ Equivalence Partitions Tests ==============

        // TC01: The normal at a general point of the plane
        assertEquals(NORMAL.normalize(), plane.getNormal(new Point(1, 0, 1)), "Wrong normal at a point of the plane");

        // =============== Boundary Values Tests ==================

        // TC11: The normal at the reference point
        assertEquals(NORMAL.normalize(), plane.getNormal(POINT_X), "Wrong normal at the reference point");
    }

    /**
     * Test method for {@link Plane#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray crosses the plane (1 point)
        assertEquals(List.of(new Point(-1, -1, 0)),
                XY_PLANE.findIntersections(new Ray(POINT_Z, new Vector(-1, -1, -1))), ERROR_INTERSECTION);

        // TC02: Ray points away from the plane (0 points)
        assertNull(XY_PLANE.findIntersections(new Ray(POINT_Z, new Vector(1, 1, 1))), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray is parallel to the plane
        // TC11: Ray is inside the plane (0 points)
        assertNull(XY_PLANE.findIntersections(new Ray(POINT_Y, Vector.AXIS_X)), ERROR_INTERSECTION);

        // TC12: Ray is outside the plane (0 points)
        assertNull(XY_PLANE.findIntersections(new Ray(POINT_Z, Vector.AXIS_X)), ERROR_INTERSECTION);

        // **** Group 2: Ray is orthogonal to the plane
        // TC21: Ray starts before the plane (1 point)
        assertEquals(List.of(Point.ZERO), XY_PLANE.findIntersections(new Ray(new Point(0, 0, -1), Vector.AXIS_Z)),
                ERROR_INTERSECTION);

        // TC22: Ray starts in the plane (0 points)
        assertNull(XY_PLANE.findIntersections(new Ray(Point.ZERO, Vector.AXIS_Z)), ERROR_INTERSECTION);

        // TC23: Ray starts after the plane (0 points)
        assertNull(XY_PLANE.findIntersections(new Ray(POINT_Z, Vector.AXIS_Z)), ERROR_INTERSECTION);

        // **** Group 3: Ray is neither orthogonal nor parallel and starts on the plane
        // TC31: Ray starts at a general point of the plane (0 points)
        assertNull(XY_PLANE.findIntersections(new Ray(new Point(1, 1, 0), new Vector(1, 1, 1))), ERROR_INTERSECTION);

        // TC32: Ray starts at the reference point of the plane (0 points)
        assertNull(XY_PLANE.findIntersections(new Ray(Point.ZERO, new Vector(1, 1, 1))), ERROR_INTERSECTION);
    }

    /**
     * Test method for {@link Plane#calcIntersections(Ray, double)}.
     */
    @Test
    void testCalcIntersectionsWithMaxDistance() {
        Ray ray = new Ray(new Point(0, 0, 2), new Vector(0, 0, -1));

        // ============ Equivalence Partitions Tests ==============

        // TC01: The intersection is closer than the maximum distance
        assertEquals(1, XY_PLANE.calcIntersections(ray, 3).size(), ERROR_INTERSECTION);

        // TC02: The intersection is farther than the maximum distance
        assertNull(XY_PLANE.calcIntersections(ray, 1), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: The intersection is exactly at the maximum distance
        assertEquals(1, XY_PLANE.calcIntersections(ray, 2).size(), ERROR_INTERSECTION);
    }
}
