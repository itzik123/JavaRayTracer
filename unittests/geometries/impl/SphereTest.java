package geometries.impl;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for class {@link Sphere}.
 * The tests verify:
 * <ul>
 * <li>{@link Sphere#getNormal(Point)}</li>
 * <li>{@link Sphere#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author itzik123
 */
class SphereTest {

    /** Default constructor to satisfy JavaDoc generator */
    SphereTest() { /* to satisfy JavaDoc generator */ }

    /** Vertex (0,0,0) used in Sphere tests */
    private static final Point POINT_ORIGIN = new Point(0, 0, 0);
    /** Vertex (1,0,0) used in Sphere tests */
    private static final Point POINT_EP = new Point(1, 0, 0);
    /** normal vector used in tests */
    private static final Vector VECTOR = new Vector(1, 0, 0);

    /** Sphere used in all tests (center 0,0,0 radius 1) */
    private static final Sphere SPHERE = new Sphere(POINT_ORIGIN, 1d);

    /** Error message for sphere intersection failures */
    private static final String ERROR_SPHERE_INTERSECTION = "Wrong sphere intersection result";

    /** Ray used in EP01 test */
    private static final Ray EP01 = new Ray(new Point(2, 0, 0), new Vector(1, 1, 0));
    /** Ray used in EP02 test */
    private static final Ray EP02 = new Ray(new Point(-2, -3, 0), new Vector(1, 1, 0));
    /** Ray used in EP03 test */
    private static final Ray EP03 = new Ray(new Point(0.5, 0.5, 0), new Vector(1, 0, 0));
    /** Ray used in EP04 test */
    private static final Ray EP04 = new Ray(new Point(1.5, 0.5, 0), new Vector(1, 0, 0));

    /** Ray used in BV11 test (edge)*/
    private static final Ray BV11 = new Ray(new Point(0, 0, 1), new Vector(-1, -1, -1));
    /** Ray used in BV12 test (edge)*/
    private static final Ray BV12 = new Ray(new Point(0, 0, 1), new Vector(1, 1, 1));

    /** Ray used in BV21 test (center)*/
    private static final Ray BV21 = new Ray(new Point(-2, 0, 0), VECTOR);
    /** Ray used in BV22 test (center)*/
    private static final Ray BV22 = new Ray(new Point(-1, 0, 0), VECTOR);
    /** Ray used in BV23 test (center)*/
    private static final Ray BV23 = new Ray(new Point(0.5, 0, 0), VECTOR);
    /** Ray used in BV24 test (center)*/
    private static final Ray BV24 = new Ray(new Point(0, 0, 0), VECTOR);
    /** Ray used in BV25 test (center)*/
    private static final Ray BV25 = new Ray(new Point(1, 0, 0), VECTOR);
    /** Ray used in BV26 test (center)*/
    private static final Ray BV26 = new Ray(new Point(2, 0, 0), VECTOR);

    /** Ray used in BV31 test (tangent)*/
    private static final Ray BV31 = new Ray(new Point(-1, 0, 1), VECTOR);
    /** Ray used in BV32 test (tangent)*/
    private static final Ray BV32 = new Ray(new Point(0, 0, 1), VECTOR);
    /** Ray used in BV33 test (tangent)*/
    private static final Ray BV33 = new Ray(new Point(1, 0, 1), VECTOR);

    /** Ray used in BV41 test (orthogonal)*/
    private static final Ray BV41 = new Ray(new Point(0, 2, 0), VECTOR);
    /** Ray used in BV42 test (orthogonal)*/
    private static final Ray BV42 = new Ray(new Point(0, 0.5, 0), VECTOR);

    // ================= Expected Intersections Lists =================

    /** Expected intersection points for EP02 test */
    private static final List<Point> INTERSECTION_EP02 = List.of(new Point(0, -1, 0), new Point(1, 0, 0));
    /** Expected intersection points for EP03 test */
    private static final List<Point> INTERSECTION_EP03 = List.of(new Point(0.8660254037844386, 0.5, 0));

    /** Expected intersection points for BV11 test */
    private static final List<Point> INTERSECTION_BV11 = List.of(new Point(-0.6666666666666667, -0.6666666666666667, 0.3333333333333333));

    /** Expected intersection points for BV21 test */
    private static final List<Point> INTERSECTION_BV21 = List.of(new Point(-1, 0, 0), new Point(1, 0, 0));
    /** Expected intersection points for BV22 test */
    private static final List<Point> INTERSECTION_BV22 = List.of(new Point(1, 0, 0));
    /** Expected intersection points for BV23 test */
    private static final List<Point> INTERSECTION_BV23 = List.of(new Point(1, 0, 0));
    /** Expected intersection points for BV24 test */
    private static final List<Point> INTERSECTION_BV24 = List.of(new Point(1, 0, 0));

    /** Expected intersection points for BV42 test */
    private static final List<Point> INTERSECTION_BV42 = List.of(new Point(0.8660254037844386, 0.5, 0));


    /**
     * test method for {@link Sphere#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: test a normal on the sphere
        assertEquals(VECTOR.normalize(), SPHERE.getNormal(POINT_EP),
                "ERROR: Failed returning the right normal");
    }

    /**
     * Test method for {@link Sphere#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray's line is outside the sphere (0 points)
        assertNull(SPHERE.findIntersections(EP01), ERROR_SPHERE_INTERSECTION);

        // TC02: Ray starts before and crosses the sphere (2 points)
        assertEquals(INTERSECTION_EP02, SPHERE.findIntersections(EP02), ERROR_SPHERE_INTERSECTION);

        // TC03: Ray starts inside the sphere (1 point)
        assertEquals(INTERSECTION_EP03, SPHERE.findIntersections(EP03), ERROR_SPHERE_INTERSECTION);

        // TC04: Ray starts after the sphere (0 points)
        assertNull(SPHERE.findIntersections(EP04), ERROR_SPHERE_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray's line crosses the sphere (but not the center)
        // TC11: Ray starts at sphere and goes inside (1 point)
        assertEquals(INTERSECTION_BV11, SPHERE.findIntersections(BV11), ERROR_SPHERE_INTERSECTION);

        // TC12: Ray starts at sphere and goes outside (0 points)
        assertNull(SPHERE.findIntersections(BV12), ERROR_SPHERE_INTERSECTION);

        // **** Group 2: Ray's line goes through the center
        // TC21: Ray starts before the sphere (2 points)
        assertEquals(INTERSECTION_BV21, SPHERE.findIntersections(BV21), ERROR_SPHERE_INTERSECTION);

        // TC22: Ray starts at sphere and goes inside (1 point)
        assertEquals(INTERSECTION_BV22, SPHERE.findIntersections(BV22), ERROR_SPHERE_INTERSECTION);

        // TC23: Ray starts inside (1 point)
        assertEquals(INTERSECTION_BV23, SPHERE.findIntersections(BV23), ERROR_SPHERE_INTERSECTION);

        // TC24: Ray starts at the center (1 point)
        assertEquals(INTERSECTION_BV24, SPHERE.findIntersections(BV24), ERROR_SPHERE_INTERSECTION);

        // TC25: Ray starts at sphere and goes outside (0 points)
        assertNull(SPHERE.findIntersections(BV25), ERROR_SPHERE_INTERSECTION);

        // TC26: Ray starts after sphere (0 points)
        assertNull(SPHERE.findIntersections(BV26), ERROR_SPHERE_INTERSECTION);

        // **** Group 3: Ray's line is tangent to the sphere (all tests 0 points)
        // TC31: Ray starts before the tangent point
        assertNull(SPHERE.findIntersections(BV31), ERROR_SPHERE_INTERSECTION);

        // TC32: Ray starts at the tangent point
        assertNull(SPHERE.findIntersections(BV32), ERROR_SPHERE_INTERSECTION);

        // TC33: Ray starts after the tangent point
        assertNull(SPHERE.findIntersections(BV33), ERROR_SPHERE_INTERSECTION);

        // **** Group 4: Special cases
        // TC41: Ray's line is outside sphere, ray is orthogonal to ray start to sphere's center line
        assertNull(SPHERE.findIntersections(BV41), ERROR_SPHERE_INTERSECTION);

        // TC42: Ray starts inside, ray is orthogonal to ray start to sphere's center line
        assertEquals(INTERSECTION_BV42, SPHERE.findIntersections(BV42), ERROR_SPHERE_INTERSECTION);
    }
}