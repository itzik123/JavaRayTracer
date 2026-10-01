package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Unit tests for {@link Triangle}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Triangle#getNormal(Point)}</li>
 * <li>{@link Triangle#findIntersections(Ray)}</li>
 * <li>{@link Triangle#calcIntersections(Ray, double)}</li>
 * <li>{@link Triangle#getBoundingBox()}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author ityy35-bit
 */
class TriangleTest {

    /** Default constructor to satisfy JavaDoc generator */
    TriangleTest() { /* to satisfy JavaDoc generator */ }

    /** The triangle (0,1,0), (1,-1,0), (-1,-1,0) on the XY plane */
    private static final Triangle TRIANGLE = new Triangle(new Point(0, 1, 0), new Point(1, -1, 0), new Point(-1, -1, 0));
    /** The downward direction, towards the triangle */
    private static final Vector DOWN = new Vector(0, 0, -1);
    /** Message for wrong intersection results */
    private static final String ERROR_INTERSECTION = "Wrong triangle intersection result";

    /**
     * Test method for {@link Triangle#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The normal of a triangle crossing the three axes
        Triangle triangle = new Triangle(new Point(1, 0, 0), new Point(0, 1, 0), new Point(0, 0, 1));
        assertEquals(new Vector(1, 1, 1).normalize(), triangle.getNormal(new Point(1, 1, 1)),
                "Wrong triangle normal");
    }

    /**
     * Test method for {@link Triangle#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray crosses the inside of the triangle (1 point)
        assertEquals(List.of(Point.ZERO), TRIANGLE.findIntersections(new Ray(new Point(0, 0, 1), DOWN)), ERROR_INTERSECTION);

        // TC02: Ray crosses the plane outside the triangle, against an edge (0 points)
        assertNull(TRIANGLE.findIntersections(new Ray(new Point(0, -2, 1), DOWN)), ERROR_INTERSECTION);

        // TC03: Ray crosses the plane outside the triangle, against a vertex (0 points)
        assertNull(TRIANGLE.findIntersections(new Ray(new Point(0, 2, 1), DOWN)), ERROR_INTERSECTION);

        // TC04: Ray does not cross the plane of the triangle (0 points)
        assertNull(TRIANGLE.findIntersections(new Ray(new Point(0, 0, 1), new Vector(1, 1, 1))), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray crosses the plane on the triangle boundary (all tests 0 points)
        // TC11: On an edge
        assertNull(TRIANGLE.findIntersections(new Ray(new Point(0, -1, 1), DOWN)), ERROR_INTERSECTION);

        // TC12: On a vertex
        assertNull(TRIANGLE.findIntersections(new Ray(new Point(0, 1, 1), DOWN)), ERROR_INTERSECTION);

        // TC13: On the continuation of an edge
        assertNull(TRIANGLE.findIntersections(new Ray(new Point(2, -1, 1), DOWN)), ERROR_INTERSECTION);

        // **** Group 2: Ray is parallel to the plane (all tests 0 points)
        // TC21: Ray is inside the plane
        assertNull(TRIANGLE.findIntersections(new Ray(Point.ZERO, Vector.AXIS_X)), ERROR_INTERSECTION);

        // TC22: Ray is outside the plane
        assertNull(TRIANGLE.findIntersections(new Ray(new Point(0, 0, 1), Vector.AXIS_X)), ERROR_INTERSECTION);

        // **** Group 3: Ray is orthogonal to the plane
        // TC31: Ray starts in the plane (0 points)
        assertNull(TRIANGLE.findIntersections(new Ray(Point.ZERO, DOWN)), ERROR_INTERSECTION);

        // TC32: Ray starts after the plane (0 points)
        assertNull(TRIANGLE.findIntersections(new Ray(new Point(0, 0, -1), DOWN)), ERROR_INTERSECTION);

        // **** Group 4: Ray is neither orthogonal nor parallel
        // TC41: Ray starts in the plane and points away (0 points)
        assertNull(TRIANGLE.findIntersections(new Ray(Point.ZERO, new Vector(1, 1, 1))), ERROR_INTERSECTION);

        // TC42: Ray starts at a vertex (0 points)
        assertNull(TRIANGLE.findIntersections(new Ray(new Point(0, 1, 0), DOWN)), ERROR_INTERSECTION);
    }

    /**
     * Test method for {@link Triangle#calcIntersections(Ray, double)}.
     */
    @Test
    void testCalcIntersectionsWithMaxDistance() {
        Ray ray = new Ray(new Point(0, 0, 2), DOWN);

        // ============ Equivalence Partitions Tests ==============

        // TC01: The triangle is closer than the maximum distance (1 point)
        assertEquals(1, TRIANGLE.calcIntersections(ray, 3).size(), ERROR_INTERSECTION);

        // TC02: The triangle is farther than the maximum distance (0 points)
        assertNull(TRIANGLE.calcIntersections(ray, 1), ERROR_INTERSECTION);
    }

    /**
     * Test method for {@link Triangle#getBoundingBox()}.
     */
    @Test
    void testGetBoundingBox() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The box encloses the three vertices tightly
        assertEquals(new AABB(new Point(-1, -1, 0), new Point(1, 1, 0)), TRIANGLE.getBoundingBox(),
                "Wrong bounding box for a triangle");
    }
}
