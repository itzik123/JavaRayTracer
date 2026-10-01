package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Unit tests for {@link Tube}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Tube#getNormal(Point)}</li>
 * <li>{@link Tube#findIntersections(Ray)}</li>
 * <li>{@link Tube#getBoundingBox()}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 * <p>
 * The intersection cases mirror those of the sphere, replacing its center with the tube axis.
 * Rays orthogonal to the axis are tested twice: in the plane of the axis origin (z = 0) and in a
 * parallel plane above it (z = 1), since the axial offset of the ray origin is a separate term of
 * the calculation. Rays slanted to or parallel with the axis are specific to the tube.
 *
 * @author ityy35-bit
 */
class TubeTest {

    /** Default constructor to satisfy JavaDoc generator */
    TubeTest() { /* to satisfy JavaDoc generator */ }

    /** The tube of radius 1 around the Z axis */
    private static final Tube TUBE = new Tube(1, new Ray(Point.ZERO, Vector.AXIS_Z));
    /** Message for wrong intersection results */
    private static final String ERROR_INTERSECTION = "Wrong tube intersection result";
    /** The X coordinate of the tube points where y = 0.5 */
    private static final double X_AT_HALF = Math.sqrt(3) / 2;
    /** The heights of the two planes orthogonal rays are tested in */
    private static final double[] HEIGHTS = {0, 1};

    /**
     * Test method for {@link Tube#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A point above the axis origin
        assertEquals(Vector.AXIS_Y, TUBE.getNormal(new Point(0, 1, 1)), "Wrong normal above the axis origin");

        // TC02: A point below the axis origin
        assertEquals(Vector.AXIS_Y, TUBE.getNormal(new Point(0, 1, -1)), "Wrong normal below the axis origin");

        // =============== Boundary Values Tests ==================

        // TC11: A point level with the axis origin
        assertEquals(Vector.AXIS_Y, TUBE.getNormal(new Point(0, 1, 0)), "Wrong normal level with the axis origin");
    }

    /**
     * Test method for {@link Tube#findIntersections(Ray)} with rays orthogonal to the axis.
     */
    @Test
    void testFindIntersectionsOrthogonalRays() {
        for (double z : HEIGHTS) {
            // ============ Equivalence Partitions Tests ==============

            // TC01: Ray's line is outside the tube (0 points)
            assertNull(TUBE.findIntersections(ray(2, 0, z, 1, 1)), ERROR_INTERSECTION);

            // TC02: Ray starts before and crosses the tube (2 points)
            assertEquals(List.of(new Point(0, -1, z), new Point(1, 0, z)),
                    TUBE.findIntersections(ray(-2, -3, z, 1, 1)), ERROR_INTERSECTION);

            // TC03: Ray starts inside the tube (1 point)
            assertEquals(List.of(new Point(X_AT_HALF, 0.5, z)), TUBE.findIntersections(ray(0.5, 0.5, z, 1, 0)),
                    ERROR_INTERSECTION);

            // TC04: Ray starts after the tube (0 points)
            assertNull(TUBE.findIntersections(ray(1.5, 0.5, z, 1, 0)), ERROR_INTERSECTION);

            // =============== Boundary Values Tests ==================

            // **** Group 1: Ray's line crosses the tube, but not the axis
            // TC11: Ray starts at the tube and goes inside (1 point)
            assertEquals(List.of(new Point(0, -1, z)), TUBE.findIntersections(ray(1, 0, z, -1, -1)), ERROR_INTERSECTION);

            // TC12: Ray starts at the tube and goes outside (0 points)
            assertNull(TUBE.findIntersections(ray(1, 0, z, 1, 1)), ERROR_INTERSECTION);

            // **** Group 2: Ray's line crosses the axis
            // TC21: Ray starts before the tube (2 points)
            assertEquals(List.of(new Point(-1, 0, z), new Point(1, 0, z)), TUBE.findIntersections(ray(-2, 0, z, 1, 0)),
                    ERROR_INTERSECTION);

            // TC22: Ray starts at the tube and goes inside (1 point)
            assertEquals(List.of(new Point(1, 0, z)), TUBE.findIntersections(ray(-1, 0, z, 1, 0)), ERROR_INTERSECTION);

            // TC23: Ray starts inside (1 point)
            assertEquals(List.of(new Point(1, 0, z)), TUBE.findIntersections(ray(0.5, 0, z, 1, 0)), ERROR_INTERSECTION);

            // TC24: Ray starts on the axis (1 point)
            assertEquals(List.of(new Point(1, 0, z)), TUBE.findIntersections(ray(0, 0, z, 1, 0)), ERROR_INTERSECTION);

            // TC25: Ray starts at the tube and goes outside (0 points)
            assertNull(TUBE.findIntersections(ray(1, 0, z, 1, 0)), ERROR_INTERSECTION);

            // TC26: Ray starts after the tube (0 points)
            assertNull(TUBE.findIntersections(ray(2, 0, z, 1, 0)), ERROR_INTERSECTION);

            // **** Group 3: Ray's line is tangent to the tube (all tests 0 points)
            // TC31: Ray starts before the tangent point
            assertNull(TUBE.findIntersections(ray(-1, 1, z, 1, 0)), ERROR_INTERSECTION);

            // TC32: Ray starts at the tangent point
            assertNull(TUBE.findIntersections(ray(0, 1, z, 1, 0)), ERROR_INTERSECTION);

            // TC33: Ray starts after the tangent point
            assertNull(TUBE.findIntersections(ray(1, 1, z, 1, 0)), ERROR_INTERSECTION);

            // **** Group 4: Ray is orthogonal to the line from its start to the axis
            // TC41: Ray's line is outside the tube (0 points)
            assertNull(TUBE.findIntersections(ray(0, 2, z, 1, 0)), ERROR_INTERSECTION);

            // TC42: Ray starts inside the tube (1 point)
            assertEquals(List.of(new Point(X_AT_HALF, 0.5, z)), TUBE.findIntersections(ray(0, 0.5, z, 1, 0)),
                    ERROR_INTERSECTION);
        }
    }

    /**
     * Test method for {@link Tube#findIntersections(Ray)} with rays slanted to or parallel with the
     * axis.
     */
    @Test
    void testFindIntersectionsNonOrthogonalRays() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Slanted ray starts before and crosses the tube (2 points)
        assertEquals(List.of(new Point(-1, 0, -1), new Point(1, 0, 1)),
                TUBE.findIntersections(new Ray(new Point(-2, 0, -2), new Vector(1, 0, 1))), ERROR_INTERSECTION);

        // TC02: Slanted ray in the opposite direction (2 points, in reverse order)
        assertEquals(List.of(new Point(1, 0, 1), new Point(-1, 0, -1)),
                TUBE.findIntersections(new Ray(new Point(2, 0, 2), new Vector(-1, 0, -1))), ERROR_INTERSECTION);

        // TC03: Ray parallel to the axis, inside the tube (0 points)
        assertNull(TUBE.findIntersections(new Ray(new Point(0.5, 0, 0), Vector.AXIS_Z)), ERROR_INTERSECTION);

        // TC04: Ray parallel to the axis, outside the tube (0 points)
        assertNull(TUBE.findIntersections(new Ray(new Point(1.5, 0, 0), Vector.AXIS_Z)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray is parallel to the axis (all tests 0 points)
        // TC11: Ray on the tube surface
        assertNull(TUBE.findIntersections(new Ray(new Point(1, 0, 0), Vector.AXIS_Z)), ERROR_INTERSECTION);

        // TC12: Ray on the axis
        assertNull(TUBE.findIntersections(new Ray(Point.ZERO, Vector.AXIS_Z)), ERROR_INTERSECTION);

        // **** Group 2: Slanted ray starts on the tube and goes inside (1 point)
        // TC21: Going up the axis
        assertEquals(List.of(new Point(1, 0, 1)),
                TUBE.findIntersections(new Ray(new Point(-1, 0, -1), new Vector(1, 0, 1))), ERROR_INTERSECTION);

        // TC22: Going down the axis
        assertEquals(List.of(new Point(-1, 0, -1)),
                TUBE.findIntersections(new Ray(new Point(1, 0, 1), new Vector(-1, 0, -1))), ERROR_INTERSECTION);
    }

    /**
     * Test method for {@link Tube#getBoundingBox()}.
     */
    @Test
    void testGetBoundingBox() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: An infinite tube has no bounding box
        assertNull(TUBE.getBoundingBox(), "An infinite tube has a bounding box");
    }

    /**
     * Creates a ray orthogonal to the tube axis.
     *
     * @param x  the X coordinate of the origin
     * @param y  the Y coordinate of the origin
     * @param z  the Z coordinate (height) of the origin
     * @param dx the X component of the direction
     * @param dy the Y component of the direction
     * @return the ray
     */
    private static Ray ray(double x, double y, double z, double dx, double dy) {
        return new Ray(new Point(x, y, z), new Vector(dx, dy, 0));
    }
}
