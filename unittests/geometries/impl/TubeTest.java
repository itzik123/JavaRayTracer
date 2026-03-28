package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;
import primitives.Ray;

import java.util.List;

/**
 * Unit tests for class {@link Tube}.
 * The tests verify:
 * <ul>
 * <li>{@link Tube#getNormal(Point)}</li>
 * <li>{@link Tube#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author ityy35-bit
 */
class TubeTest {

    /** Default constructor to satisfy JavaDoc generator */
    TubeTest() { /* to satisfy JavaDoc generator */ }

    /** Vertex (0,0,0) used in Tube tests */
    private static final Point  POINT_ORIGIN    = new Point(0, 0, 0);
    /** Vertex (0,1,0) used in Tube tests */
    private static final Point  POINT_BVA       = new Point(0, 1, 0);
    /** Vertex (0,1,-1) used in Tube tests */
    private static final Point  POINT_EP1       = new Point(0, 1, -1);
    /** Vertex (0,1,1) used in Tube tests */
    private static final Point  POINT_EP2       = new Point(0, 1, 1);
    /** direction vector used in tests */
    private static final Vector DIR_VECTOR          = new Vector(0, 0, 1);
    /** normal vector used in tests */
    private static final Vector VECTOR_NORMAL   = new Vector(0, 1, 0);

    private static final Tube TUBE = new Tube(1,new Ray(POINT_ORIGIN,DIR_VECTOR));

    /**
     * test method for {@link Tube#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: test a normal in front of the ray
        assertEquals(VECTOR_NORMAL, TUBE.getNormal(POINT_EP2),
                "ERROR: Failed returning the normal in front of the ray");

        // TC02: test a normal behind the ray
        assertEquals(VECTOR_NORMAL, TUBE.getNormal(POINT_EP1),
                "ERROR: Failed returning the normal behind the ray");

        // =============== Boundary Values Tests ==================

        // TC11: test a normal next to the origin
        assertEquals(VECTOR_NORMAL, TUBE.getNormal(POINT_BVA),
                "ERROR: Failed returning the normal behind the ray");

    }

    /**vector used in tests */
    private static final Vector VECTOR          = new Vector(1, 0, 0);
    /** Error message for tube intersection failures */
    private static final String ERROR_TUBE_INTERSECTION = "Wrong tube intersection result";

    /** Ray used in EP01 test */
    private static final Ray EP01 = new Ray(new Point(2, 0, 0), new Vector(1, 1, 0));
    /** Ray used in EP02 test */
    private static final Ray EP02 = new Ray(new Point(-2, -3, 0), new Vector(1, 1, 0));
    /** Ray used in EP03 test */
    private static final Ray EP03 = new Ray(new Point(0.5, 0.5, 0), new Vector(1, 0, 0));
    /** Ray used in EP04 test */
    private static final Ray EP04 = new Ray(new Point(1.5, 0.5, 0), new Vector(1, 0, 0));

    /** Ray used in BV11 test (edge)*/
    private static final Ray BV11 = new Ray(new Point(1, 0, 0), new Vector(-1, -1, 0));
    /** Ray used in BV12 test (edge)*/
    private static final Ray BV12 = new Ray(new Point(1, 0, 0), new Vector(1, 1, 0));

    /** Ray used in BV21 test (axis)*/
    private static final Ray BV21 = new Ray(new Point(-2, 0, 0), VECTOR);
    /** Ray used in BV22 test (axis)*/
    private static final Ray BV22 = new Ray(new Point(-1, 0, 0), VECTOR);
    /** Ray used in BV23 test (axis)*/
    private static final Ray BV23 = new Ray(new Point(0.5, 0, 0), VECTOR);
    /** Ray used in BV24 test (axis)*/
    private static final Ray BV24 = new Ray(new Point(0, 0, 0), VECTOR);
    /** Ray used in BV25 test (axis)*/
    private static final Ray BV25 = new Ray(new Point(1, 0, 0), VECTOR);
    /** Ray used in BV26 test (axis)*/
    private static final Ray BV26 = new Ray(new Point(2, 0, 0), VECTOR);

    /** Ray used in BV31 test (tangent)*/
    private static final Ray BV31 = new Ray(new Point(1, 1, 0), VECTOR);
    /** Ray used in BV32 test (tangent)*/
    private static final Ray BV32 = new Ray(new Point(0, 1, 0), VECTOR);
    /** Ray used in BV33 test (tangent)*/
    private static final Ray BV33 = new Ray(new Point(-1, 1, 0), VECTOR);

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
    private static final List<Point> INTERSECTION_BV11 = List.of(new Point(0, -1,0));

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

    /** Ray used in EP05 test */
    private static final Ray EP05 = new Ray(new Point(2, 0, 1), new Vector(1, 1, 0));
    /** Ray used in EP06 test */
    private static final Ray EP06 = new Ray(new Point(-2, -3, 1), new Vector(1, 1, 0));
    /** Ray used in EP07 test */
    private static final Ray EP07 = new Ray(new Point(0.5, 0.5, 1), new Vector(1, 0, 0));
    /** Ray used in EP08 test */
    private static final Ray EP08 = new Ray(new Point(1.5, 0.5, 1), new Vector(1, 0, 0));

    /** Ray used in BV13 test (edge)*/
    private static final Ray BV13 = new Ray(new Point(1, 0, 1), new Vector(-1, -1, 0));
    /** Ray used in BV14 test (edge)*/
    private static final Ray BV14 = new Ray(new Point(1, 0, 1), new Vector(1, 1, 0));

    /** Ray used in BV27 test (axis)*/
    private static final Ray BV27 = new Ray(new Point(-2, 0, 1), VECTOR);
    /** Ray used in BV28 test (axis)*/
    private static final Ray BV28 = new Ray(new Point(-1, 0, 1), VECTOR);
    /** Ray used in BV29 test (axis)*/
    private static final Ray BV29 = new Ray(new Point(0.5, 0, 1), VECTOR);
    /** Ray used in BV2_10 test (axis)*/
    private static final Ray BV2_10 = new Ray(new Point(0, 0, 1), VECTOR);
    /** Ray used in BV2_11 test (axis)*/
    private static final Ray BV2_11 = new Ray(new Point(1, 0, 1), VECTOR);
    /** Ray used in BV2_12 test (axis)*/
    private static final Ray BV2_12 = new Ray(new Point(2, 0, 1), VECTOR);

    /** Ray used in BV34 test (tangent)*/
    private static final Ray BV34 = new Ray(new Point(1, 1, 1), VECTOR);
    /** Ray used in BV35 test (tangent)*/
    private static final Ray BV35 = new Ray(new Point(0, 1, 1), VECTOR);
    /** Ray used in BV36 test (tangent)*/
    private static final Ray BV36 = new Ray(new Point(-1, 1, 1), VECTOR);

    /** Ray used in BV43 test (orthogonal)*/
    private static final Ray BV43 = new Ray(new Point(0, 2, 1), VECTOR);
    /** Ray used in BV44 test (orthogonal)*/
    private static final Ray BV44 = new Ray(new Point(0, 0.5, 1), VECTOR);

// ================= Expected Intersections Lists =================

    /** Expected intersection points for EP06 test */
    private static final List<Point> INTERSECTION_EP06 = List.of(new Point(0, -1, 1), new Point(1, 0, 1));
    /** Expected intersection points for EP07 test */
    private static final List<Point> INTERSECTION_EP07 = List.of(new Point(0.8660254037844386, 0.5, 1));

    /** Expected intersection points for BV13 test */
    private static final List<Point> INTERSECTION_BV13 = List.of(new Point(0, -1, 1));

    /** Expected intersection points for BV27 test */
    private static final List<Point> INTERSECTION_BV27 = List.of(new Point(-1, 0, 1), new Point(1, 0, 1));
    /** Expected intersection points for BV28 test */
    private static final List<Point> INTERSECTION_BV28 = List.of(new Point(1, 0, 1));
    /** Expected intersection points for BV29 test */
    private static final List<Point> INTERSECTION_BV29 = List.of(new Point(1, 0, 1));
    /** Expected intersection points for BV2_10 test */
    private static final List<Point> INTERSECTION_BV2_10 = List.of(new Point(1, 0, 1));

    /** Expected intersection points for BV44 test */
    private static final List<Point> INTERSECTION_BV44 = List.of(new Point(0.8660254037844386, 0.5, 1));

    /** Ray used in EP09 test */
    private static final Ray EP09 = new Ray(new Point(0.5, 0, 0), Vector.AXIS_Z);
    /** Ray used in EP0_10 test */
    private static final Ray EP0_10 = new Ray(new Point(1.5, 0, 0), Vector.AXIS_Z);
    /** Ray used in EP0_11 test */
    private static final Ray EP0_11 = new Ray(new Point(-2, 0, -2), new Vector(1, 0, 1));
    /** Ray used in EP0_12 test */
    private static final Ray EP0_12 = new Ray(new Point(2, 0, 2), new Vector(-1, 0, -1));

    /** Ray used in BV51 test (parallel on edge)*/
    private static final Ray BV51 = new Ray(new Point(1, 0, 0), Vector.AXIS_Z);
    /** Ray used in BV52 test (parallel)*/
    private static final Ray BV52 = new Ray(new Point(0, 0, 0), Vector.AXIS_Z);
    /** Ray used in BV61 test (edge slant)*/
    private static final Ray BV61 = new Ray(new Point(-1, 0, -1), new Vector(1, 0, 1));
    /** Ray used in BV62 test (edge slant)*/
    private static final Ray BV62 = new Ray(new Point(1, 0, 1), new Vector(-1, 0, -1));

    /** Expected intersection points for EP0_11 test */
    private static final List<Point> INTERSECTION_EP0_11 = List.of(new Point(-1, 0, -1), new Point(1, 0, 1));
    /** Expected intersection points for EP0_12 test */
    private static final List<Point> INTERSECTION_EP0_12 = List.of(new Point(1, 0, 1), new Point(-1, 0, -1));
    /** Expected intersection points for BV61 test */
    private static final List<Point> INTERSECTION_BV61 = List.of(new Point(1, 0, 1));
    /** Expected intersection points for BV62 test */
    private static final List<Point> INTERSECTION_BV62 = List.of(new Point(-1, 0, -1));

    /**
     * Test method for {@link Tube#findIntersections(Ray)}.
     * To write the tube tests, we copied all the tests we used in the sphere tests with
     * minor changes where necessary and duplicated them to create more test cases.
     * Once tests when we are in the plane containing the point that defines the tube and once more above
     * this plane and in the direction of the Ray vector. In this way we found the simple test cases for the tube.
     * Finally, we added about ten more test cases that we think are especially important for the tube class.
     * We are aware that the sphere test cases can also be implemented against the direction of the Ray vector,
     * but we started to get alarmingly close to 70 tests.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // ******This part of the tests is in the plane that contains the point that defines the ray.*******

        // TC01: Ray's line is outside the tube (0 points)
        assertNull(TUBE.findIntersections(EP01), ERROR_TUBE_INTERSECTION);

        // TC02: Ray starts before and crosses the tube (2 points)
        assertEquals(INTERSECTION_EP02, TUBE.findIntersections(EP02), ERROR_TUBE_INTERSECTION);

        // TC03: Ray starts inside the tube (1 point)
        assertEquals(INTERSECTION_EP03, TUBE.findIntersections(EP03), ERROR_TUBE_INTERSECTION);

        // TC04: Ray starts after the tube (0 points)
        assertNull(TUBE.findIntersections(EP04), ERROR_TUBE_INTERSECTION);

        //This part of the tests is on the plane above the point that defines the ray in the direction of the ray.

        // TC05: Ray's line is outside the tube (0 points)
        assertNull(TUBE.findIntersections(EP05), ERROR_TUBE_INTERSECTION);

        // TC06: Ray starts before and crosses the tube (2 points)
        assertEquals(INTERSECTION_EP06, TUBE.findIntersections(EP06), ERROR_TUBE_INTERSECTION);

        // TC07: Ray starts inside the tube (1 point)
        assertEquals(INTERSECTION_EP07, TUBE.findIntersections(EP07), ERROR_TUBE_INTERSECTION);

        // TC08: Ray starts after the tube (0 points)
        assertNull(TUBE.findIntersections(EP08), ERROR_TUBE_INTERSECTION);

        //This part of the tests is specific to the tube

        // TC09: Ray inside, parallel to axis (0 points)
        assertNull(TUBE.findIntersections(EP09), ERROR_TUBE_INTERSECTION);

        // TC10: Ray outside, parallel to axis (0 points)
        assertNull(TUBE.findIntersections(EP0_10), ERROR_TUBE_INTERSECTION);

        // TC11: Slanted ray starts inside the tube (2 points)
        assertEquals(INTERSECTION_EP0_11, TUBE.findIntersections(EP0_11), ERROR_TUBE_INTERSECTION);

        // TC12: Slanted ray starts after the tube (2 points)
        assertEquals(INTERSECTION_EP0_12, TUBE.findIntersections(EP0_12), ERROR_TUBE_INTERSECTION);


        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray's line crosses the tube (but not the axis)

        // ******This part of the tests is in the plane that contains the point that defines the ray.*******

        // TC11: Ray starts at tube and goes inside (1 point)
        assertEquals(INTERSECTION_BV11, TUBE.findIntersections(BV11), ERROR_TUBE_INTERSECTION);

        // TC12: Ray starts at tube and goes outside (0 points)
        assertNull(TUBE.findIntersections(BV12), ERROR_TUBE_INTERSECTION);

        //This part of the tests is on the plane above the point that defines the ray in the direction of the ray.

        // TC13: Ray starts at tube and goes inside (1 point)
        assertEquals(INTERSECTION_BV13, TUBE.findIntersections(BV13), ERROR_TUBE_INTERSECTION);

        // TC14: Ray starts at tube and goes outside (0 points)
        assertNull(TUBE.findIntersections(BV14), ERROR_TUBE_INTERSECTION);

        // **** Group 2: Ray's line goes through the axis

        // ******This part of the tests is in the plane that contains the point that defines the ray.*******

        // TC21: Ray starts before the tube (2 points)
        assertEquals(INTERSECTION_BV21, TUBE.findIntersections(BV21), ERROR_TUBE_INTERSECTION);

        // TC22: Ray starts at tube and goes inside (1 point)
        assertEquals(INTERSECTION_BV22, TUBE.findIntersections(BV22), ERROR_TUBE_INTERSECTION);

        // TC23: Ray starts inside (1 point)
        assertEquals(INTERSECTION_BV23, TUBE.findIntersections(BV23), ERROR_TUBE_INTERSECTION);

        // TC24: Ray starts at the axis (1 point)
        assertEquals(INTERSECTION_BV24, TUBE.findIntersections(BV24), ERROR_TUBE_INTERSECTION);

        // TC25: Ray starts at tube and goes outside (0 points)
        assertNull(TUBE.findIntersections(BV25), ERROR_TUBE_INTERSECTION);

        // TC26: Ray starts after tube (0 points)
        assertNull(TUBE.findIntersections(BV26), ERROR_TUBE_INTERSECTION);

        //This part of the tests is on the plane above the point that defines the ray in the direction of the ray.

        // TC27: Ray starts before the tube (2 points)
        assertEquals(INTERSECTION_BV27, TUBE.findIntersections(BV27), ERROR_TUBE_INTERSECTION);

        // TC28: Ray starts at tube and goes inside (1 point)
        assertEquals(INTERSECTION_BV28, TUBE.findIntersections(BV28), ERROR_TUBE_INTERSECTION);

        // TC29: Ray starts inside (1 point)
        assertEquals(INTERSECTION_BV29, TUBE.findIntersections(BV29), ERROR_TUBE_INTERSECTION);

        // TC2_10: Ray starts at the axis (1 point)
        assertEquals(INTERSECTION_BV2_10, TUBE.findIntersections(BV2_10), ERROR_TUBE_INTERSECTION);

        // TC2_11: Ray starts at tube and goes outside (0 points)
        assertNull(TUBE.findIntersections(BV2_11), ERROR_TUBE_INTERSECTION);

        // TC2_12: Ray starts after tube (0 points)
        assertNull(TUBE.findIntersections(BV2_12), ERROR_TUBE_INTERSECTION);

        // **** Group 3: Ray's line is tangent to the tube (all tests 0 points)

        // ******This part of the tests is in the plane that contains the point that defines the ray.*******

        // TC31: Ray starts before the tangent point
        assertNull(TUBE.findIntersections(BV31), ERROR_TUBE_INTERSECTION);

        // TC32: Ray starts at the tangent point
        assertNull(TUBE.findIntersections(BV32), ERROR_TUBE_INTERSECTION);

        // TC33: Ray starts after the tangent point
        assertNull(TUBE.findIntersections(BV33), ERROR_TUBE_INTERSECTION);

        //This part of the tests is on the plane above the point that defines the ray in the direction of the ray.

        // TC34: Ray starts before the tangent point
        assertNull(TUBE.findIntersections(BV34), ERROR_TUBE_INTERSECTION);

        // TC35: Ray starts at the tangent point
        assertNull(TUBE.findIntersections(BV35), ERROR_TUBE_INTERSECTION);

        // TC36: Ray starts after the tangent point
        assertNull(TUBE.findIntersections(BV36), ERROR_TUBE_INTERSECTION);


        // **** Group 4: Special cases

        // ******This part of the tests is in the plane that contains the point that defines the ray.*******

        // TC41: Ray's line is outside tube, orthogonal to axis
        assertNull(TUBE.findIntersections(BV41), ERROR_TUBE_INTERSECTION);

        // TC42: Ray starts inside, orthogonal to axis
        assertEquals(INTERSECTION_BV42, TUBE.findIntersections(BV42), ERROR_TUBE_INTERSECTION);

        //This part of the tests is on the plane above the point that defines the ray in the direction of the ray.

        // TC43: Ray's line is outside tube, orthogonal to axis
        assertNull(TUBE.findIntersections(BV43), ERROR_TUBE_INTERSECTION);

        // TC44: Ray starts inside, orthogonal to axis
        assertEquals(INTERSECTION_BV44, TUBE.findIntersections(BV44), ERROR_TUBE_INTERSECTION);

        //This part of the tests is specific to the tube

        // TC51: Ray starts on tube, parallel to axis (0 points)
        assertNull(TUBE.findIntersections(BV51), ERROR_TUBE_INTERSECTION);

        // TC52: Ray matches the axis (0 points)
        assertNull(TUBE.findIntersections(BV52), ERROR_TUBE_INTERSECTION);


        // TC61: Slanted ray starts on tube, goes inside
        assertEquals(INTERSECTION_BV61,TUBE.findIntersections(BV61), ERROR_TUBE_INTERSECTION);

        // TC62: Slanted ray starts on tube, goes inside
        assertEquals(INTERSECTION_BV62,TUBE.findIntersections(BV62), ERROR_TUBE_INTERSECTION);
    }
}