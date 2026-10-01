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
 * Unit tests for {@link Cylinder}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Cylinder#Cylinder(double, Ray, double)}</li>
 * <li>{@link Cylinder#getNormal(Point)}</li>
 * <li>{@link Cylinder#findIntersections(Ray)}</li>
 * <li>{@link Cylinder#getBoundingBox()}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class CylinderTest {

    /** Default constructor to satisfy JavaDoc generator */
    CylinderTest() { /* to satisfy JavaDoc generator */ }

    /** The Z axis, used as the axis of the test cylinders */
    private static final Ray Z_AXIS = new Ray(Point.ZERO, Vector.AXIS_Z);
    /** A cylinder of radius 1 around the Z axis, from z = 0 to z = 2 */
    private static final Cylinder CYLINDER = new Cylinder(1, Z_AXIS, 2);
    /** Message for wrong intersection results */
    private static final String ERROR_INTERSECTION = "Wrong cylinder intersection result";
    /** The X coordinate of the side points where y = 0.5 */
    private static final double X_AT_HALF = Math.sqrt(3) / 2;

    /**
     * Test method for {@link Cylinder#Cylinder(double, Ray, double)}.
     */
    @Test
    void testConstructor() {
        // =============== Boundary Values Tests ==================

        // TC11: Zero height
        assertThrows(IllegalArgumentException.class, () -> new Cylinder(1, Z_AXIS, 0),
                "Constructed a cylinder with zero height");
    }

    /**
     * Test method for {@link Cylinder#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        Cylinder cylinder = new Cylinder(1, Z_AXIS, 3);
        Vector down = new Vector(0, 0, -1);

        // ============ Equivalence Partitions Tests ==============

        // TC01: A point on the side
        assertEquals(Vector.AXIS_X, cylinder.getNormal(new Point(1, 0, 1)), "Wrong normal on the side");

        // TC02: A point on the bottom base
        assertEquals(down, cylinder.getNormal(new Point(0.5, 0, 0)), "Wrong normal on the bottom base");

        // TC03: A point on the top base
        assertEquals(Vector.AXIS_Z, cylinder.getNormal(new Point(0.5, 0, 3)), "Wrong normal on the top base");

        // =============== Boundary Values Tests ==================

        // TC11: A point on the rim of the bottom base
        assertEquals(down, cylinder.getNormal(new Point(1, 0, 0)), "Wrong normal on the bottom rim");

        // TC12: A point on the rim of the top base
        assertEquals(Vector.AXIS_Z, cylinder.getNormal(new Point(1, 0, 3)), "Wrong normal on the top rim");

        // TC13: The center of the bottom base
        assertEquals(down, cylinder.getNormal(Point.ZERO), "Wrong normal at the bottom center");

        // TC14: The center of the top base
        assertEquals(Vector.AXIS_Z, cylinder.getNormal(new Point(0, 0, 3)), "Wrong normal at the top center");
    }

    /**
     * Test method for {@link Cylinder#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray orthogonal to the axis crosses the side twice (2 points)
        assertEquals(List.of(new Point(0, -1, 1), new Point(1, 0, 1)),
                CYLINDER.findIntersections(ray(-2, -3, 1, 1, 1, 0)), ERROR_INTERSECTION);

        // TC02: Ray starts inside and exits through the side (1 point)
        assertEquals(List.of(new Point(X_AT_HALF, 0.5, 1)),
                CYLINDER.findIntersections(ray(0.5, 0.5, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // TC03: Ray's line is outside the cylinder (0 points)
        assertNull(CYLINDER.findIntersections(ray(2, 0, 1, 1, 1, 0)), ERROR_INTERSECTION);

        // TC04: Ray starts after the cylinder (0 points)
        assertNull(CYLINDER.findIntersections(ray(1.5, 0.5, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // TC05: Ray crosses both bases (2 points)
        assertEquals(List.of(Point.ZERO, new Point(0, 0, 2)),
                CYLINDER.findIntersections(ray(0, 0, -1, 0, 0, 1)), ERROR_INTERSECTION);

        // TC06: Ray enters through the bottom base and exits through the side (2 points)
        assertEquals(List.of(new Point(0.5, 0, 0), new Point(1, 0, 1)),
                CYLINDER.findIntersections(ray(0, 0, -1, 1, 0, 2)), ERROR_INTERSECTION);

        // TC07: Ray enters through the side and exits through the top base (2 points)
        assertEquals(List.of(new Point(1, 0, 1), new Point(0, 0, 2)),
                CYLINDER.findIntersections(ray(2, 0, 0, -1, 0, 1)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray lies in the plane of the bottom base (all tests 0 points)
        // TC11: Ray crosses the rim, not through the center
        assertAllNull(ray(2, 0, 0, 1, 1, 0), ray(-2, -3, 0, 1, 1, 0), ray(1, 0, 0, -1, -1, 0), ray(1, 0, 0, 1, 1, 0));

        // TC12: Ray passes through the center
        assertAllNull(ray(-2, 0, 0, 1, 0, 0), ray(-1, 0, 0, 1, 0, 0), ray(0.5, 0, 0, 1, 0, 0),
                ray(0, 0, 0, 1, 0, 0), ray(1, 0, 0, 1, 0, 0), ray(2, 0, 0, 1, 0, 0));

        // TC13: Ray is tangent to the rim
        assertAllNull(ray(1, 1, 0, 1, 0, 0), ray(0, 1, 0, 1, 0, 0), ray(-1, 1, 0, 1, 0, 0));

        // TC14: Ray starts inside or outside the base, orthogonal to the line to the center
        assertAllNull(ray(0, 2, 0, 1, 0, 0), ray(0, 0.5, 0, 1, 0, 0), ray(0.5, 0.5, 0, 1, 0, 0), ray(1.5, 0.5, 0, 1, 0, 0));

        // **** Group 2: Ray orthogonal to the axis, between the bases
        // TC21: Ray starts at the side and goes inside (1 point)
        assertEquals(List.of(new Point(0, -1, 1)), CYLINDER.findIntersections(ray(1, 0, 1, -1, -1, 0)), ERROR_INTERSECTION);

        // TC22: Ray starts at the side and goes outside (0 points)
        assertNull(CYLINDER.findIntersections(ray(1, 0, 1, 1, 1, 0)), ERROR_INTERSECTION);

        // TC23: Ray crosses the axis, starting before the cylinder (2 points)
        assertEquals(List.of(new Point(-1, 0, 1), new Point(1, 0, 1)),
                CYLINDER.findIntersections(ray(-2, 0, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // TC24: Ray crosses the axis, starting at the side (1 point)
        assertEquals(List.of(new Point(1, 0, 1)), CYLINDER.findIntersections(ray(-1, 0, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // TC25: Ray crosses the axis, starting inside (1 point)
        assertEquals(List.of(new Point(1, 0, 1)), CYLINDER.findIntersections(ray(0.5, 0, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // TC26: Ray starts on the axis (1 point)
        assertEquals(List.of(new Point(1, 0, 1)), CYLINDER.findIntersections(ray(0, 0, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // TC27: Ray on the axis line starts at the side and goes outside (0 points)
        assertNull(CYLINDER.findIntersections(ray(1, 0, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // TC28: Ray on the axis line starts after the cylinder (0 points)
        assertNull(CYLINDER.findIntersections(ray(2, 0, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // **** Group 3: Ray is tangent to the side (all tests 0 points)
        // TC31: Ray starts before the tangent point
        assertNull(CYLINDER.findIntersections(ray(-1, 1, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // TC32: Ray starts at the tangent point
        assertNull(CYLINDER.findIntersections(ray(0, 1, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // TC33: Ray starts after the tangent point
        assertNull(CYLINDER.findIntersections(ray(1, 1, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // **** Group 4: Ray is orthogonal to the line from its start to the axis
        // TC41: Ray's line is outside the cylinder (0 points)
        assertNull(CYLINDER.findIntersections(ray(0, 2, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // TC42: Ray starts inside the cylinder (1 point)
        assertEquals(List.of(new Point(X_AT_HALF, 0.5, 1)),
                CYLINDER.findIntersections(ray(0, 0.5, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // **** Group 5: Ray starts on the bottom base and goes inside (1 point)
        // TC51: From the center, straight to the top base
        assertEquals(List.of(new Point(0, 0, 2)), CYLINDER.findIntersections(ray(0, 0, 0, 0, 0, 1)), ERROR_INTERSECTION);

        // TC52: From the center, to the side
        assertEquals(List.of(new Point(1, 0, 1)), CYLINDER.findIntersections(ray(0, 0, 0, 1, 0, 1)), ERROR_INTERSECTION);

        // TC53: From the center, exactly to the top rim
        assertEquals(List.of(new Point(1, 0, 2)), CYLINDER.findIntersections(ray(0, 0, 0, 1, 0, 2)), ERROR_INTERSECTION);

        // TC54: From a general point of the base, straight up
        assertEquals(List.of(new Point(0.5, 0, 2)), CYLINDER.findIntersections(ray(0.5, 0, 0, 0, 0, 1)),
                ERROR_INTERSECTION);

        // TC55: From the bottom rim, diagonally to the opposite top rim
        assertEquals(List.of(new Point(-1, 0, 2)), CYLINDER.findIntersections(ray(1, 0, 0, -1, 0, 1)),
                ERROR_INTERSECTION);

        // **** Group 6: Ray starts on or above the top base and goes away (all tests 0 points)
        // TC61: From the top center, up
        assertNull(CYLINDER.findIntersections(ray(0, 0, 2, 0, 0, 1)), ERROR_INTERSECTION);

        // TC62: From the top rim, outwards
        assertNull(CYLINDER.findIntersections(ray(1, 0, 2, 1, 0, 1)), ERROR_INTERSECTION);

        // TC63: Above the cylinder, parallel to the top base
        assertNull(CYLINDER.findIntersections(ray(0, 0, 3, 1, 0, 0)), ERROR_INTERSECTION);

        // TC64: In the plane of the top base, tangent to its rim
        assertNull(CYLINDER.findIntersections(ray(2, 1, 2, -1, 0, 0)), ERROR_INTERSECTION);

        // **** Group 7: Ray starts strictly inside the cylinder (1 point)
        // TC71: Straight up to the top base
        assertEquals(List.of(new Point(0, 0, 2)), CYLINDER.findIntersections(ray(0, 0, 1, 0, 0, 1)), ERROR_INTERSECTION);

        // TC72: Straight to the side
        assertEquals(List.of(new Point(1, 0, 1)), CYLINDER.findIntersections(ray(0, 0, 1, 1, 0, 0)), ERROR_INTERSECTION);

        // TC73: Exactly to the top rim
        assertEquals(List.of(new Point(1, 0, 2)), CYLINDER.findIntersections(ray(0, 0, 1, 1, 0, 1)), ERROR_INTERSECTION);
    }

    /**
     * Test method for {@link Cylinder#getBoundingBox()}.
     */
    @Test
    void testGetBoundingBox() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Axis-aligned cylinder - the box is exact along the axis
        Cylinder alongY = new Cylinder(0.5, new Ray(Point.ZERO, Vector.AXIS_Y), 2);
        assertEquals(new AABB(new Point(-0.5, 0, -0.5), new Point(0.5, 2, 0.5)), alongY.getBoundingBox(),
                "Wrong bounding box for an axis-aligned cylinder");

        // TC02: Tilted cylinder - each base disk extends r * sqrt(1 - d²) along every axis
        Cylinder tilted = new Cylinder(1, new Ray(Point.ZERO, new Vector(1, 1, 0)), Math.sqrt(2));
        double e = Math.sqrt(0.5);
        assertEquals(new AABB(new Point(-e, -e, -1), new Point(1 + e, 1 + e, 1)), tilted.getBoundingBox(),
                "Wrong bounding box for a tilted cylinder");
    }

    /**
     * Creates a ray.
     *
     * @param x  the X coordinate of the origin
     * @param y  the Y coordinate of the origin
     * @param z  the Z coordinate of the origin
     * @param dx the X component of the direction
     * @param dy the Y component of the direction
     * @param dz the Z component of the direction
     * @return the ray
     */
    private static Ray ray(double x, double y, double z, double dx, double dy, double dz) {
        return new Ray(new Point(x, y, z), new Vector(dx, dy, dz));
    }

    /**
     * Asserts that none of the rays intersects {@link #CYLINDER}.
     *
     * @param rays the rays
     */
    private static void assertAllNull(Ray... rays) {
        for (Ray ray : rays) assertNull(CYLINDER.findIntersections(ray), ERROR_INTERSECTION + " for " + ray);
    }
}
