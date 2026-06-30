package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Unit tests for class {@link Triangle}.
 * The tests verify:
 * <ul>
 * <li>{@link Triangle#getNormal(Point)}</li>
 * <li>{@link Triangle#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values Tests (BVA)
 * @author ityy35-bit
 */
class TriangleTest {

    /** Default constructor to satisfy JavaDoc generator */
    TriangleTest() { /* to satisfy JavaDoc generator */ }

    /** Vertex (1,1,1) used in triangle tests */
    private static final Point  POINT_EP       = new Point(1, 1, 1);
    /** Vertex (1,0,0) used in triangle tests */
    private static final Point  POINT_X        = new Point(1, 0, 0);
    /** Vertex (0,1,0) used in triangle tests */
    private static final Point  POINT_Y        = new Point(0, 1, 0);
    /** Vertex (0,0,1) used in triangle tests */
    private static final Point  POINT_Z        = new Point(0, 0, 1);
    /** normal vector used in tests */
    private static final Vector VECTOR         = new Vector(1, 1, 1);

    /**
     * test method for {@link Triangle#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        Triangle triangle = new Triangle(POINT_X, POINT_Y, POINT_Z);

        // TC01: test a normal on the triangle
        assertEquals(VECTOR.normalize(), triangle.getNormal(POINT_EP) ,
                "ERROR: Failed returning the right normal");
    }

    /** Error message for triangle intersection failures */
    private static final String ERROR_TRIANGLE_INTERSECTION = "Wrong triangle intersection result";

    /** First vertex of the triangle on the XY plane (z=0) */
    private static final Point V1 = new Point(0, 1, 0);
    /** Second vertex of the triangle on the XY plane (z=0) */
    private static final Point V2 = new Point(1, -1, 0);
    /** Third vertex of the triangle on the XY plane (z=0) */
    private static final Point V3 = new Point(-1, -1, 0);

    /** The triangle used for tests */
    private static final Triangle TRIANGLE = new Triangle(V1, V2, V3);

    /** Ray intersecting strictly inside the triangle */
    private static final Ray EP01 = new Ray(new Point(0, 0, 1), new Vector(0, 0, -1));

    /** Ray intersecting the plane outside the triangle, against edge V2-V3 */
    private static final Ray EP02 = new Ray(new Point(0, -2, 1), new Vector(0, 0, -1));

    /** Ray intersecting the plane outside the triangle, against vertex V1 */
    private static final Ray EP03 = new Ray(new Point(0, 2, 1), new Vector(0, 0, -1));

    /** Ray whose line does not intersect the plane of the triangle at all */
    private static final Ray EP04 = new Ray(new Point(0, 0, 1), new Vector(1, 1, 1));

    /** Ray intersecting the plane strictly on the edge V2-V3 */
    private static final Ray BV11 = new Ray(new Point(0, -1, 1), new Vector(0, 0, -1));

    /** Ray intersecting the plane strictly on the vertex V1 */
    private static final Ray BV12 = new Ray(new Point(0, 1, 1), new Vector(0, 0, -1));

    /** Ray intersecting the plane on the continuation of edge V2-V3 */
    private static final Ray BV13 = new Ray(new Point(2, -1, 1), new Vector(0, 0, -1));

    /** Ray parallel to the plane and included in it */
    private static final Ray BV21 = new Ray(new Point(0, 0, 0), new Vector(1, 0, 0));

    /** Ray parallel to the plane but not included in it */
    private static final Ray BV22 = new Ray(new Point(0, 0, 1), new Vector(1, 0, 0));

    /** Ray orthogonal to the plane, starting IN the plane */
    private static final Ray BV31 = new Ray(new Point(0, 0, 0), new Vector(0, 0, -1));

    /** Ray orthogonal to the plane, starting AFTER the plane */
    private static final Ray BV32 = new Ray(new Point(0, 0, -1), new Vector(0, 0, -1));

    /** Ray orthogonal to the plane, starting BEFORE the plane */
    private static final Ray BV33 = new Ray(new Point(0, 0, 1), new Vector(0, 0, -1));

    /** Ray beginning on the plane and pointing away from it */
    private static final Ray BV41 = new Ray(new Point(0, 0, 0), new Vector(1, 1, 1));

    // ================= Expected Intersections Lists =================

    /** Expected intersection point for EP01 test */
    private static final List<Point> INTERSECTION_EP01 = List.of(new Point(0, 0, 0));

    /** Expected intersection point for BV33 test */
    private static final List<Point> INTERSECTION_BV33 = List.of(new Point(0, 0, 0));

    /**
     * Test method for {@link Triangle#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {

        // =========================================================================
        //                 PART 1: SPECIFIC TRIANGLE TESTS
        // All rays here go straight down (0,0,-1) from z=1 to intersect the plane z=0
        // =========================================================================

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray intersects strictly inside the triangle
        assertEquals(INTERSECTION_EP01, TRIANGLE.findIntersections(EP01), ERROR_TRIANGLE_INTERSECTION);

        // TC02: Ray intersects outside against an edge (against V2-V3)
        assertNull(TRIANGLE.findIntersections(EP02), ERROR_TRIANGLE_INTERSECTION);

        // TC03: Ray intersects outside against a vertex (against V1)
        assertNull(TRIANGLE.findIntersections(EP03), ERROR_TRIANGLE_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Ray intersects strictly on an edge (on V2-V3)
        assertNull(TRIANGLE.findIntersections(BV11), ERROR_TRIANGLE_INTERSECTION);

        // TC12: Ray intersects strictly on a vertex (on V1)
        assertNull(TRIANGLE.findIntersections(BV12), ERROR_TRIANGLE_INTERSECTION);

        // TC13: Ray intersects on an edge's continuation (continuation of V2-V3)
        assertNull(TRIANGLE.findIntersections(BV13), ERROR_TRIANGLE_INTERSECTION);


        // =========================================================================
        //                 PART 2: PLANE BASED TESTS (No Intersections)
        // Cases where the ray doesn't even intersect the plane the triangle is on
        // =========================================================================

        // ============ Equivalence Partitions Tests ==============

        // TC04: Ray's line does not intersect the plane at all
        assertNull(TRIANGLE.findIntersections(EP04), ERROR_TRIANGLE_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray is parallel to the plane
        // TC21: The ray is included in the plane
        assertNull(TRIANGLE.findIntersections(BV21), ERROR_TRIANGLE_INTERSECTION);

        // TC22: The ray is parallel but not included in the plane
        assertNull(TRIANGLE.findIntersections(BV22), ERROR_TRIANGLE_INTERSECTION);

        // **** Group 2: Ray is orthogonal to the plane
        // TC31: Ray starts IN the plane (orthogonal)
        assertNull(TRIANGLE.findIntersections(BV31), ERROR_TRIANGLE_INTERSECTION);

        // TC32: Ray starts AFTER the plane (orthogonal)
        assertNull(TRIANGLE.findIntersections(BV32), ERROR_TRIANGLE_INTERSECTION);

        // TC33: Ray starts BEFORE the plane (orthogonal)
        assertEquals(INTERSECTION_BV33,TRIANGLE.findIntersections(BV33), ERROR_TRIANGLE_INTERSECTION);

        // **** Group 3: Ray begins on the plane (neither orthogonal nor parallel)
        // TC41: Ray begins on the plane and points away
        assertNull(TRIANGLE.findIntersections(BV41), ERROR_TRIANGLE_INTERSECTION);
    }

    private static final Triangle TRIANGLE_AABB = new Triangle(new Point(-1, -1, 0), new Point(-1, 1, 0), new Point(1, 0, 0));
    private static final primitives.AABB EXPECTED_TRIANGLE_AABB = new primitives.AABB(new Point(-1, -1, 0), new Point(1, 1, 0));

    /**
     * test method for bounding box creation.
     */
    @Test
    void testBoundingBox() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Correct AABB for a simple triangle
        TRIANGLE_AABB.setBoundingBox();
        assertEquals(EXPECTED_TRIANGLE_AABB, TRIANGLE_AABB.getBoundingBox(), "ERROR: Wrong AABB for Triangle");
    }
}