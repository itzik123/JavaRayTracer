package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
 * Unit tests for {@link Polygon}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Polygon#Polygon(Point...)}</li>
 * <li>{@link Polygon#getNormal(Point)}</li>
 * <li>{@link Polygon#findIntersections(Ray)}</li>
 * <li>{@link Polygon#calcIntersections(Ray, double)}</li>
 * <li>{@link Polygon#getBoundingBox()}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author Dan Zilberstein
 */
class PolygonTest {

    /** Default constructor to satisfy JavaDoc generator */
    PolygonTest() { /* to satisfy JavaDoc generator */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Vertex (1,0,0) */
    private static final Point POINT_X = new Point(1, 0, 0);
    /** Vertex (0,1,0) */
    private static final Point POINT_Y = new Point(0, 1, 0);
    /** Vertex (0,0,1) */
    private static final Point POINT_Z = new Point(0, 0, 1);
    /** A vertex completing a convex quadrilateral with the axis points */
    private static final Point POINT_CONVEX = new Point(-1, 1, 1);
    /** A point outside the plane of the axis points */
    private static final Point POINT_OFF_PLANE = new Point(0, 2, 2);
    /** A vertex that makes the quadrilateral concave */
    private static final Point POINT_CONCAVE = new Point(0.5, 0.25, 0.5);
    /** A point on the edge between {@link #POINT_Y} and {@link #POINT_Z} */
    private static final Point POINT_ON_EDGE = new Point(0, 0.5, 0.5);

    /** The square (±1,±1,0), used for the intersection tests */
    private static final Polygon SQUARE = new Polygon(
            new Point(1, 1, 0), new Point(1, -1, 0), new Point(-1, -1, 0), new Point(-1, 1, 0));
    /** The downward direction, towards the square */
    private static final Vector DOWN = new Vector(0, 0, -1);
    /** Message for wrong intersection results */
    private static final String ERROR_INTERSECTION = "Wrong polygon intersection result";

    /**
     * Test method for {@link Polygon#Polygon(Point...)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct convex quadrilateral with vertices in order
        assertDoesNotThrow(() -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_CONVEX),
                "Failed constructing a correct polygon");

        // TC02: Vertices out of order
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_Y, POINT_X, POINT_CONVEX),
                "Constructed a polygon with vertices out of order");

        // TC03: Vertices not in the same plane
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_OFF_PLANE),
                "Constructed a polygon with vertices not in the same plane");

        // TC04: Concave quadrilateral
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_CONCAVE),
                "Constructed a concave polygon");

        // =============== Boundary Values Tests ==================

        // TC11: A vertex on an edge
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_ON_EDGE),
                "Constructed a polygon with a vertex on an edge");

        // TC12: The last vertex equals the first one
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_Z),
                "Constructed a polygon with the last vertex equal to the first one");

        // TC13: Two consecutive vertices coincide
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_Y),
                "Constructed a polygon with coincident vertices");

        // TC14: Fewer than three vertices
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_X, POINT_Y),
                "Constructed a polygon with two vertices");
    }

    /**
     * Test method for {@link Polygon#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The normal is a unit vector orthogonal to all the edges
        Point[] vertices = {POINT_Z, POINT_X, POINT_Y, POINT_CONVEX};
        Polygon polygon = new Polygon(vertices);
        Vector normal = assertDoesNotThrow(() -> polygon.getNormal(POINT_Z), "getNormal() threw an exception");
        assertEquals(1, normal.length(), DELTA, "The polygon normal is not a unit vector");
        for (int i = 0; i < vertices.length; ++i)
            assertEquals(0, normal.dotProduct(vertices[i].subtract(vertices[i == 0 ? vertices.length - 1 : i - 1])),
                    DELTA, "The polygon normal is not orthogonal to an edge");
    }

    /**
     * Test method for {@link Polygon#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray crosses the inside of the polygon (1 point)
        assertEquals(List.of(Point.ZERO), SQUARE.findIntersections(new Ray(new Point(0, 0, 1), DOWN)), ERROR_INTERSECTION);

        // TC02: Ray crosses the plane outside the polygon, against an edge (0 points)
        assertNull(SQUARE.findIntersections(new Ray(new Point(2, 0, 1), DOWN)), ERROR_INTERSECTION);

        // TC03: Ray crosses the plane outside the polygon, against a vertex (0 points)
        assertNull(SQUARE.findIntersections(new Ray(new Point(2, 2, 1), DOWN)), ERROR_INTERSECTION);

        // TC04: Ray does not cross the plane of the polygon (0 points)
        assertNull(SQUARE.findIntersections(new Ray(new Point(0, 0, 1), new Vector(1, 1, 1))), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray crosses the plane on the polygon boundary (all tests 0 points)
        // TC11: On an edge
        assertNull(SQUARE.findIntersections(new Ray(new Point(1, 0, 1), DOWN)), ERROR_INTERSECTION);

        // TC12: On a vertex
        assertNull(SQUARE.findIntersections(new Ray(new Point(1, 1, 1), DOWN)), ERROR_INTERSECTION);

        // TC13: On the continuation of an edge
        assertNull(SQUARE.findIntersections(new Ray(new Point(1, 2, 1), DOWN)), ERROR_INTERSECTION);

        // **** Group 2: Ray is parallel to the plane (all tests 0 points)
        // TC21: Ray is inside the plane
        assertNull(SQUARE.findIntersections(new Ray(Point.ZERO, Vector.AXIS_X)), ERROR_INTERSECTION);

        // TC22: Ray is outside the plane
        assertNull(SQUARE.findIntersections(new Ray(new Point(0, 0, 1), Vector.AXIS_X)), ERROR_INTERSECTION);

        // **** Group 3: Ray is orthogonal to the plane
        // TC31: Ray starts in the plane (0 points)
        assertNull(SQUARE.findIntersections(new Ray(Point.ZERO, DOWN)), ERROR_INTERSECTION);

        // TC32: Ray starts after the plane (0 points)
        assertNull(SQUARE.findIntersections(new Ray(new Point(0, 0, -1), DOWN)), ERROR_INTERSECTION);

        // **** Group 4: Ray is neither orthogonal nor parallel
        // TC41: Ray starts in the plane and points away (0 points)
        assertNull(SQUARE.findIntersections(new Ray(Point.ZERO, new Vector(1, 1, 1))), ERROR_INTERSECTION);
    }

    /**
     * Test method for {@link Polygon#calcIntersections(Ray, double)}.
     */
    @Test
    void testCalcIntersectionsWithMaxDistance() {
        Ray ray = new Ray(new Point(0, 0, 2), DOWN);

        // ============ Equivalence Partitions Tests ==============

        // TC01: The polygon is closer than the maximum distance (1 point)
        assertEquals(1, SQUARE.calcIntersections(ray, 3).size(), ERROR_INTERSECTION);

        // TC02: The polygon is farther than the maximum distance (0 points)
        assertNull(SQUARE.calcIntersections(ray, 1), ERROR_INTERSECTION);
    }

    /**
     * Test method for {@link Polygon#getBoundingBox()}.
     */
    @Test
    void testGetBoundingBox() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The box encloses all the vertices tightly
        assertEquals(new AABB(new Point(-1, -1, 0), new Point(1, 1, 0)), SQUARE.getBoundingBox(),
                "Wrong bounding box for a polygon");
    }
}
