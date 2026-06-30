package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;
import primitives.Ray;

import java.util.List;

/**
 * Unit tests for class {@link Cylinder}.
 * The tests verify:
 * <ul>
 * <li>{@link Cylinder#getNormal(Point)}</li>
 * <li>{@link Cylinder#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author itzik123
 */
class CylinderTest {
    /** Default constructor to satisfy JavaDoc generator */
    CylinderTest() { /* to satisfy JavaDoc generator */ }

    /** Vertex (0,0,0) used in Cylinder tests */
    private static final Point  POINT_ORIGIN    = new Point(0, 0, 0);
    /** direction vector used in tests */
    private static final Vector VECTOR          = new Vector(0, 0, 1);

    /** Vertex (1,0,1) used in Cylinder tests (Point on the side surface) */
    private static final Point  POINT_EP1       = new Point(1, 0, 1);
    /** Vertex (0.5,0,0) used in Cylinder tests (Point on the bottom base) */
    private static final Point  POINT_EP2       = new Point(0.5, 0, 0);
    /** Vertex (0.5,0,3) used in Cylinder tests (Point on the top base) */
    private static final Point  POINT_EP3       = new Point(0.5, 0, 3);

    /** Vertex (1,0,0) used in Cylinder tests (Point on the edge of the bottom base) */
    private static final Point  POINT_BVA1      = new Point(1, 0, 0);
    /** Vertex (1,0,3) used in Cylinder tests (Point on the edge of the top base) */
    private static final Point  POINT_BVA2      = new Point(1, 0, 3);
    /** Vertex (0,0,0) used in Cylinder tests (Point at the center of the bottom base) */
    private static final Point  POINT_BVA3      = new Point(0, 0, 0);
    /** Vertex (0,0,3) used in Cylinder tests (Point at the center of the top base) */
    private static final Point  POINT_BVA4      = new Point(0, 0, 3);

    /** normal vector for the side surface */
    private static final Vector VECTOR_NORMAL1  = new Vector(1, 0, 0);
    /** normal vector for the bottom base */
    private static final Vector VECTOR_NORMAL2  = new Vector(0, 0, -1);
    /** normal vector for the top base */
    private static final Vector VECTOR_NORMAL3  = new Vector(0, 0, 1);

    /**
     * test method for {@link Cylinder#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        Cylinder cylinder = new Cylinder(1, new Ray(POINT_ORIGIN, VECTOR), 3);

        // ============ Equivalence Partitions Tests ==============

        // TC01: test a normal on the side surface of the cylinder
        assertEquals(VECTOR_NORMAL1, cylinder.getNormal(POINT_EP1),
                "ERROR: Failed returning the correct normal on the side surface of the cylinder");

        // TC02: test a normal on the bottom base of the cylinder
        assertEquals(VECTOR_NORMAL2, cylinder.getNormal(POINT_EP2),
                "ERROR: Failed returning the correct normal on the bottom base of the cylinder");

        // TC03: test a normal on the top base of the cylinder
        assertEquals(VECTOR_NORMAL3, cylinder.getNormal(POINT_EP3),
                "ERROR: Failed returning the correct normal on the top base of the cylinder");

        // =============== Boundary Values Tests ==================

        // TC11: test a normal on the edge of the bottom base
        assertEquals(VECTOR_NORMAL2, cylinder.getNormal(POINT_BVA1),
                "ERROR: Failed returning the correct normal on the edge of the bottom base");

        // TC12: test a normal on the edge of the top base
        assertEquals(VECTOR_NORMAL3, cylinder.getNormal(POINT_BVA2),
                "ERROR: Failed returning the correct normal on the edge of the top base");

        // TC13: test a normal exactly at the center of the bottom base (origin)
        assertEquals(VECTOR_NORMAL2, cylinder.getNormal(POINT_BVA3),
                "ERROR: Failed returning the correct normal exactly at the center of the bottom base");

        // TC14: test a normal exactly at the center of the top base
        assertEquals(VECTOR_NORMAL3, cylinder.getNormal(POINT_BVA4),
                "ERROR: Failed returning the correct normal exactly at the center of the top base");
    }

    /** Cylinder used in tests (radius 1, origin (0,0,0), direction (0,0,1), height 2) */
    private static final Cylinder CYLINDER = new Cylinder(1d, new Ray(POINT_ORIGIN, VECTOR), 2d);

    /** Error message for cylinder intersection failures */
    private static final String ERROR_CYLINDER_INTERSECTION = "Wrong cylinder intersection result";

    /** Vector (1,0,0) used for ray directions in tests */
    private static final Vector DIR_X = new Vector(1, 0, 0);

    // ================== Rays for Plane Z=0 (Bottom Base) ==================
    private static final Ray EP01 = new Ray(new Point(2, 0, 0), new Vector(1, 1, 0));
    private static final Ray EP02 = new Ray(new Point(-2, -3, 0), new Vector(1, 1, 0));
    private static final Ray EP03 = new Ray(new Point(0.5, 0.5, 0), DIR_X);
    private static final Ray EP04 = new Ray(new Point(1.5, 0.5, 0), DIR_X);

    private static final Ray BV11 = new Ray(new Point(1, 0, 0), new Vector(-1, -1, 0));
    private static final Ray BV12 = new Ray(new Point(1, 0, 0), new Vector(1, 1, 0));

    private static final Ray BV21 = new Ray(new Point(-2, 0, 0), DIR_X);
    private static final Ray BV22 = new Ray(new Point(-1, 0, 0), DIR_X);
    private static final Ray BV23 = new Ray(new Point(0.5, 0, 0), DIR_X);
    private static final Ray BV24 = new Ray(new Point(0, 0, 0), DIR_X);
    private static final Ray BV25 = new Ray(new Point(1, 0, 0), DIR_X);
    private static final Ray BV26 = new Ray(new Point(2, 0, 0), DIR_X);

    private static final Ray BV31 = new Ray(new Point(1, 1, 0), DIR_X);
    private static final Ray BV32 = new Ray(new Point(0, 1, 0), DIR_X);
    private static final Ray BV33 = new Ray(new Point(-1, 1, 0), DIR_X);

    private static final Ray BV41 = new Ray(new Point(0, 2, 0), DIR_X);
    private static final Ray BV42 = new Ray(new Point(0, 0.5, 0), DIR_X);

    // ================== Rays & Expected for Plane Z=1 (Middle of Cylinder) ==================
    private static final Ray EP05 = new Ray(new Point(2, 0, 1), new Vector(1, 1, 0));
    private static final Ray EP06 = new Ray(new Point(-2, -3, 1), new Vector(1, 1, 0));
    private static final Ray EP07 = new Ray(new Point(0.5, 0.5, 1), DIR_X);
    private static final Ray EP08 = new Ray(new Point(1.5, 0.5, 1), DIR_X);

    private static final Ray BV13 = new Ray(new Point(1, 0, 1), new Vector(-1, -1, 0));
    private static final Ray BV14 = new Ray(new Point(1, 0, 1), new Vector(1, 1, 0));

    private static final Ray BV27 = new Ray(new Point(-2, 0, 1), DIR_X);
    private static final Ray BV28 = new Ray(new Point(-1, 0, 1), DIR_X);
    private static final Ray BV29 = new Ray(new Point(0.5, 0, 1), DIR_X);
    private static final Ray BV2_10 = new Ray(new Point(0, 0, 1), DIR_X);
    private static final Ray BV2_11 = new Ray(new Point(1, 0, 1), DIR_X);
    private static final Ray BV2_12 = new Ray(new Point(2, 0, 1), DIR_X);

    private static final Ray BV34 = new Ray(new Point(1, 1, 1), DIR_X);
    private static final Ray BV35 = new Ray(new Point(0, 1, 1), DIR_X);
    private static final Ray BV36 = new Ray(new Point(-1, 1, 1), DIR_X);

    private static final Ray BV43 = new Ray(new Point(0, 2, 1), DIR_X);
    private static final Ray BV44 = new Ray(new Point(0, 0.5, 1), DIR_X);

    // Expected Intersections for Z=1 (Sorted from closest to farthest)
    private static final List<Point> INTERSECTION_EP06 = List.of(new Point(0, -1, 1), new Point(1, 0, 1));
    private static final List<Point> INTERSECTION_EP07 = List.of(new Point(0.8660254037844386, 0.5, 1));
    private static final List<Point> INTERSECTION_BV13 = List.of(new Point(0, -1, 1));
    private static final List<Point> INTERSECTION_BV27 = List.of(new Point(-1, 0, 1), new Point(1, 0, 1));
    private static final List<Point> INTERSECTION_BV28 = List.of(new Point(1, 0, 1));
    private static final List<Point> INTERSECTION_BV29 = List.of(new Point(1, 0, 1));
    private static final List<Point> INTERSECTION_BV2_10 = List.of(new Point(1, 0, 1));
    private static final List<Point> INTERSECTION_BV44 = List.of(new Point(0.8660254037844386, 0.5, 1));

    // ================== Special Cylinder Rays & Expected Results ==================

    // ---- Group EP (Cross-surface hits): Rays crossing multiple surface types ----
    /** Ray starts below, crosses bottom base and top base */
    private static final Ray EP09 = new Ray(new Point(0, 0, -1), new Vector(0, 0, 1));
    /** Ray starts below, crosses bottom base and exits through the side */
    private static final Ray EP10 = new Ray(new Point(0, 0, -1), new Vector(1, 0, 2));
    /** Ray starts outside on Z=0 plane, goes inside through side, exits through top base */
    private static final Ray EP11 = new Ray(new Point(2, 0, 0), new Vector(-1, 0, 1));

    private static final List<Point> INTERSECTION_EP09 = List.of(new Point(0, 0, 0), new Point(0, 0, 2));
    private static final List<Point> INTERSECTION_EP10 = List.of(new Point(0.5, 0, 0), new Point(1, 0, 1));
    private static final List<Point> INTERSECTION_EP11 = List.of(new Point(1, 0, 1), new Point(0, 0, 2));

    // ---- Group 5: Rays starting on the bottom base (Z=0) and going IN ----
    /** Starts at origin, goes straight up to top base center */
    private static final Ray BV51 = new Ray(POINT_ORIGIN, new Vector(0, 0, 1));
    /** Starts at origin, angled to hit the side */
    private static final Ray BV52 = new Ray(POINT_ORIGIN, new Vector(1, 0, 1));
    /** Starts at origin, angled to hit exactly the top edge */
    private static final Ray BV53 = new Ray(POINT_ORIGIN, new Vector(1, 0, 2));
    /** Starts on bottom base (not center), goes straight up */
    private static final Ray BV54 = new Ray(new Point(0.5, 0, 0), new Vector(0, 0, 1));
    /** Starts on bottom edge, goes diagonally to opposite top edge */
    private static final Ray BV55 = new Ray(new Point(1, 0, 0), new Vector(-1, 0, 1));

    private static final List<Point> INTERSECTION_BV51 = List.of(new Point(0, 0, 2));
    private static final List<Point> INTERSECTION_BV52 = List.of(new Point(1, 0, 1));
    private static final List<Point> INTERSECTION_BV53 = List.of(new Point(1, 0, 2));
    private static final List<Point> INTERSECTION_BV54 = List.of(new Point(0.5, 0, 2));
    private static final List<Point> INTERSECTION_BV55 = List.of(new Point(-1, 0, 2));

    // ---- Group 6: Rays starting on top base (Z=2) or above, going OUT/AWAY ----
    /** Starts at top base center, goes up */
    private static final Ray BV61 = new Ray(new Point(0, 0, 2), new Vector(0, 0, 1));
    /** Starts at top edge, goes outward */
    private static final Ray BV62 = new Ray(new Point(1, 0, 2), new Vector(1, 0, 1));
    /** Ray passes completely above the cylinder, parallel to top face */
    private static final Ray BV63 = new Ray(new Point(0, 0, 3), new Vector(1, 0, 0));
    /** Ray parallel to top face, passes exactly through the top edge (tangent) */
    private static final Ray BV64 = new Ray(new Point(2, 1, 2), new Vector(-1, 0, 0));

    // ---- Group 7: Rays starting strictly INSIDE the cylinder ----
    /** Starts inside, goes straight up to top base */
    private static final Ray BV71 = new Ray(new Point(0, 0, 1), new Vector(0, 0, 1));
    /** Starts inside, goes straight to the side */
    private static final Ray BV72 = new Ray(new Point(0, 0, 1), new Vector(1, 0, 0));
    /** Starts inside, angled to hit the top edge exactly */
    private static final Ray BV73 = new Ray(new Point(0, 0, 1), new Vector(1, 0, 1));

    private static final List<Point> INTERSECTION_BV71 = List.of(new Point(0, 0, 2));
    private static final List<Point> INTERSECTION_BV72 = List.of(new Point(1, 0, 1));
    private static final List<Point> INTERSECTION_BV73 = List.of(new Point(1, 0, 2));

    /**
     * Test method for {@link Cylinder#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        // All rays here are on the plane of the base, thus creating infinite intersections or none.
        // We expect null for all of them.

        // ==== Equivalence Partitions ====
        assertNull(CYLINDER.findIntersections(EP01), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(EP02), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(EP03), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(EP04), ERROR_CYLINDER_INTERSECTION);

        // ==== Boundary Values ====
        // Group 1: Crosses the base (but not center)
        assertNull(CYLINDER.findIntersections(BV11), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV12), ERROR_CYLINDER_INTERSECTION);

        // Group 2: Goes through the center
        assertNull(CYLINDER.findIntersections(BV21), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV22), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV23), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV24), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV25), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV26), ERROR_CYLINDER_INTERSECTION);

        // Group 3: Tangent to the base circle
        assertNull(CYLINDER.findIntersections(BV31), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV32), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV33), ERROR_CYLINDER_INTERSECTION);

        // Group 4: Special cases (orthogonal)
        assertNull(CYLINDER.findIntersections(BV41), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV42), ERROR_CYLINDER_INTERSECTION);

        // ==== Equivalence Partitions ====
        assertNull(CYLINDER.findIntersections(EP05), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_EP06, CYLINDER.findIntersections(EP06), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_EP07, CYLINDER.findIntersections(EP07), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(EP08), ERROR_CYLINDER_INTERSECTION);

        // ==== Boundary Values ====
        // Group 1: Crosses the cylinder (but not center)
        assertEquals(INTERSECTION_BV13, CYLINDER.findIntersections(BV13), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV14), ERROR_CYLINDER_INTERSECTION);

        // Group 2: Goes through the center axis
        assertEquals(INTERSECTION_BV27, CYLINDER.findIntersections(BV27), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_BV28, CYLINDER.findIntersections(BV28), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_BV29, CYLINDER.findIntersections(BV29), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_BV2_10, CYLINDER.findIntersections(BV2_10), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV2_11), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV2_12), ERROR_CYLINDER_INTERSECTION);

        // Group 3: Tangent to the cylinder
        assertNull(CYLINDER.findIntersections(BV34), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV35), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV36), ERROR_CYLINDER_INTERSECTION);

        // Group 4: Special cases (orthogonal)
        assertNull(CYLINDER.findIntersections(BV43), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_BV44, CYLINDER.findIntersections(BV44), ERROR_CYLINDER_INTERSECTION);

        // ==== Equivalence Partitions (Cross-surface hits) ====
        assertEquals(INTERSECTION_EP09, CYLINDER.findIntersections(EP09), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_EP10, CYLINDER.findIntersections(EP10), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_EP11, CYLINDER.findIntersections(EP11), ERROR_CYLINDER_INTERSECTION);

        // ==== Boundary Values ====

        // Group 5: Rays starting on the bottom base (Z=0) and going IN (1 point expected)
        assertEquals(INTERSECTION_BV51, CYLINDER.findIntersections(BV51), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_BV52, CYLINDER.findIntersections(BV52), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_BV53, CYLINDER.findIntersections(BV53), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_BV54, CYLINDER.findIntersections(BV54), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_BV55, CYLINDER.findIntersections(BV55), ERROR_CYLINDER_INTERSECTION);

        // Group 6: Rays starting on or above the top base (Z>=2), going OUT (0 points expected)
        assertNull(CYLINDER.findIntersections(BV61), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV62), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV63), ERROR_CYLINDER_INTERSECTION);
        assertNull(CYLINDER.findIntersections(BV64), ERROR_CYLINDER_INTERSECTION);

        // Group 7: Rays starting strictly INSIDE the cylinder (1 point expected)
        assertEquals(INTERSECTION_BV71, CYLINDER.findIntersections(BV71), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_BV72, CYLINDER.findIntersections(BV72), ERROR_CYLINDER_INTERSECTION);
        assertEquals(INTERSECTION_BV73, CYLINDER.findIntersections(BV73), ERROR_CYLINDER_INTERSECTION);
    }

    private static final Cylinder CYLINDER_AABB = new Cylinder(0.5, new Ray(new Point(0, 0, 0), new Vector(0, 1, 0)), 2);
    // מרכז תחתון (0,0,0), מרכז עליון (0,2,0). רדיוס 0.5.
    // לכן מינימום: X=-0.5, Y=-0.5, Z=-0.5.
    // מקסימום: X=0.5, Y=2.5, Z=0.5.
    private static final primitives.AABB EXPECTED_CYLINDER_AABB = new primitives.AABB(
            new Point(-0.5, -0.5, -0.5), new Point(0.5, 2.5, 0.5));

    /**
     * test method for bounding box creation.
     */
    @Test
    void testBoundingBox() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Correct AABB for a cylinder
        CYLINDER_AABB.setBoundingBox();
        assertEquals(EXPECTED_CYLINDER_AABB, CYLINDER_AABB.getBoundingBox(), "ERROR: Wrong AABB for Cylinder");
    }
}