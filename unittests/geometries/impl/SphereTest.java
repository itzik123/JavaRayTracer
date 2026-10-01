package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Unit tests for {@link Sphere}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Sphere#Sphere(Point, double)}</li>
 * <li>{@link Sphere#getNormal(Point)}</li>
 * <li>{@link Sphere#findIntersections(Ray)}</li>
 * <li>{@link Sphere#calcIntersections(Ray, double)}</li>
 * <li>{@link Sphere#getBoundingBox()}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class SphereTest {

    /** Default constructor to satisfy JavaDoc generator */
    SphereTest() { /* to satisfy JavaDoc generator */ }

    /** The unit sphere around the origin */
    private static final Sphere SPHERE = new Sphere(Point.ZERO, 1);
    /** Message for wrong intersection results */
    private static final String ERROR_INTERSECTION = "Wrong sphere intersection result";
    /** The X coordinate of the sphere points where y = 0.5 on the XY plane */
    private static final double X_AT_HALF = Math.sqrt(3) / 2;

    /**
     * Test method for {@link Sphere#Sphere(Point, double)}.
     */
    @Test
    void testConstructor() {
        // =============== Boundary Values Tests ==================

        // TC11: Zero radius
        assertThrows(IllegalArgumentException.class, () -> new Sphere(Point.ZERO, 0),
                "Constructed a sphere with zero radius");
    }

    /**
     * Test method for {@link Sphere#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The normal at a point on the sphere
        assertEquals(Vector.AXIS_X, SPHERE.getNormal(new Point(1, 0, 0)), "Wrong sphere normal");
    }

    /**
     * Test method for {@link Sphere#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        Vector alongX = Vector.AXIS_X;

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray's line is outside the sphere (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(2, 0, 0), new Vector(1, 1, 0))), ERROR_INTERSECTION);

        // TC02: Ray starts before and crosses the sphere (2 points)
        assertEquals(List.of(new Point(0, -1, 0), new Point(1, 0, 0)),
                SPHERE.findIntersections(new Ray(new Point(-2, -3, 0), new Vector(1, 1, 0))), ERROR_INTERSECTION);

        // TC03: Ray starts inside the sphere (1 point)
        assertEquals(List.of(new Point(X_AT_HALF, 0.5, 0)),
                SPHERE.findIntersections(new Ray(new Point(0.5, 0.5, 0), alongX)), ERROR_INTERSECTION);

        // TC04: Ray starts after the sphere (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(1.5, 0.5, 0), alongX)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray's line crosses the sphere, but not the center
        // TC11: Ray starts at the sphere and goes inside (1 point)
        assertEquals(List.of(new Point(-2d / 3, -2d / 3, 1d / 3)),
                SPHERE.findIntersections(new Ray(new Point(0, 0, 1), new Vector(-1, -1, -1))), ERROR_INTERSECTION);

        // TC12: Ray starts at the sphere and goes outside (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(0, 0, 1), new Vector(1, 1, 1))), ERROR_INTERSECTION);

        // **** Group 2: Ray's line goes through the center
        // TC21: Ray starts before the sphere (2 points)
        assertEquals(List.of(new Point(-1, 0, 0), new Point(1, 0, 0)),
                SPHERE.findIntersections(new Ray(new Point(-2, 0, 0), alongX)), ERROR_INTERSECTION);

        // TC22: Ray starts at the sphere and goes inside (1 point)
        assertEquals(List.of(new Point(1, 0, 0)),
                SPHERE.findIntersections(new Ray(new Point(-1, 0, 0), alongX)), ERROR_INTERSECTION);

        // TC23: Ray starts inside (1 point)
        assertEquals(List.of(new Point(1, 0, 0)),
                SPHERE.findIntersections(new Ray(new Point(0.5, 0, 0), alongX)), ERROR_INTERSECTION);

        // TC24: Ray starts at the center (1 point)
        assertEquals(List.of(new Point(1, 0, 0)),
                SPHERE.findIntersections(new Ray(Point.ZERO, alongX)), ERROR_INTERSECTION);

        // TC25: Ray starts at the sphere and goes outside (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(1, 0, 0), alongX)), ERROR_INTERSECTION);

        // TC26: Ray starts after the sphere (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(2, 0, 0), alongX)), ERROR_INTERSECTION);

        // **** Group 3: Ray's line is tangent to the sphere (all tests 0 points)
        // TC31: Ray starts before the tangent point
        assertNull(SPHERE.findIntersections(new Ray(new Point(-1, 0, 1), alongX)), ERROR_INTERSECTION);

        // TC32: Ray starts at the tangent point
        assertNull(SPHERE.findIntersections(new Ray(new Point(0, 0, 1), alongX)), ERROR_INTERSECTION);

        // TC33: Ray starts after the tangent point
        assertNull(SPHERE.findIntersections(new Ray(new Point(1, 0, 1), alongX)), ERROR_INTERSECTION);

        // **** Group 4: Ray is orthogonal to the line from its start to the center
        // TC41: Ray's line is outside the sphere (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(0, 2, 0), alongX)), ERROR_INTERSECTION);

        // TC42: Ray starts inside the sphere (1 point)
        assertEquals(List.of(new Point(X_AT_HALF, 0.5, 0)),
                SPHERE.findIntersections(new Ray(new Point(0, 0.5, 0), alongX)), ERROR_INTERSECTION);
    }

    /**
     * Test method for {@link Sphere#calcIntersections(Ray, double)}.
     */
    @Test
    void testCalcIntersectionsWithMaxDistance() {
        Ray crossing = new Ray(new Point(-2, 0, 0), Vector.AXIS_X);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Both intersections are within the maximum distance (2 points)
        assertEquals(2, SPHERE.calcIntersections(crossing, 10).size(), ERROR_INTERSECTION);

        // TC02: Only the near intersection is within the maximum distance (1 point)
        assertEquals(1, SPHERE.calcIntersections(crossing, 2).size(), ERROR_INTERSECTION);

        // TC03: Both intersections are beyond the maximum distance (0 points)
        assertNull(SPHERE.calcIntersections(crossing, 0.5), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Ray from the center with a maximum distance shorter than the radius (0 points)
        assertNull(SPHERE.calcIntersections(new Ray(Point.ZERO, Vector.AXIS_X), 0.5), ERROR_INTERSECTION);
    }

    /**
     * Test method for {@link Sphere#getBoundingBox()}.
     */
    @Test
    void testGetBoundingBox() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The box of a sphere around the origin
        assertEquals(new AABB(new Point(-1, -1, -1), new Point(1, 1, 1)), SPHERE.getBoundingBox(),
                "Wrong bounding box for a sphere");
    }
}
