package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import geometries.api.Intersectable;
import org.junit.jupiter.api.Test;
import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Unit tests for {@link Geometries}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Geometries#findIntersections(Ray)}</li>
 * <li>{@link Geometries#flatten()}</li>
 * <li>{@link Geometries#getBoundingBox()}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class GeometriesTest {

    /** Default constructor to satisfy JavaDoc generator */
    GeometriesTest() { /* to satisfy JavaDoc generator */ }

    /** A triangle on the XY plane */
    private static final Triangle TRIANGLE = new Triangle(new Point(-1, -1, 0), new Point(-1, 1, 0), new Point(1, 0, 0));
    /** The unit sphere around the origin */
    private static final Sphere SPHERE = new Sphere(Point.ZERO, 1);
    /** An infinite tube along the Y axis */
    private static final Tube TUBE = new Tube(0.5, new Ray(Point.ZERO, Vector.AXIS_Y));
    /** A collection of the three geometries */
    private static final Geometries GEOMETRIES = new Geometries(TRIANGLE, SPHERE, TUBE);
    /** The downward direction */
    private static final Vector DOWN = new Vector(0, 0, -1);
    /** Message for wrong intersection counts */
    private static final String ERROR_COUNT = "Wrong number of intersections";

    /**
     * Test method for {@link Geometries#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Some of the geometries are intersected (sphere and triangle, 3 points)
        assertEquals(3, GEOMETRIES.findIntersections(new Ray(new Point(-0.9, 0, 2), DOWN)).size(), ERROR_COUNT);

        // =============== Boundary Values Tests ==================

        // TC11: No geometry is intersected
        assertNull(GEOMETRIES.findIntersections(new Ray(new Point(1, 1, 2), DOWN)), ERROR_COUNT);

        // TC12: Only one geometry is intersected (tube, 2 points)
        assertEquals(2, GEOMETRIES.findIntersections(new Ray(new Point(0, 2, 2), DOWN)).size(), ERROR_COUNT);

        // TC13: All the geometries are intersected (5 points)
        assertEquals(5, GEOMETRIES.findIntersections(new Ray(new Point(0, 0, 2), DOWN)).size(), ERROR_COUNT);

        // TC14: An empty collection
        assertNull(new Geometries().findIntersections(new Ray(new Point(0, 0, 2), DOWN)), ERROR_COUNT);
    }

    /**
     * Test method for {@link Geometries#flatten()}.
     */
    @Test
    void testFlatten() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Nested collections are flattened in insertion order
        Geometries nested = new Geometries(TRIANGLE, new Geometries(SPHERE, new Geometries(TUBE)));
        assertEquals(List.<Intersectable>of(TRIANGLE, SPHERE, TUBE), nested.flatten(), "flatten() returned wrong leaves");

        // =============== Boundary Values Tests ==================

        // TC11: An empty collection
        assertEquals(List.of(), new Geometries().flatten(), "flatten() of an empty collection is not empty");
    }

    /**
     * Test method for {@link Geometries#getBoundingBox()}.
     */
    @Test
    void testGetBoundingBox() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The box encloses the bounded members and ignores the infinite tube
        assertEquals(new AABB(new Point(-1, -1, -1), new Point(1, 1, 1)), GEOMETRIES.getBoundingBox(),
                "Wrong bounding box for a collection");

        // =============== Boundary Values Tests ==================

        // TC11: A collection of infinite geometries only
        assertNull(new Geometries(TUBE).getBoundingBox(), "A collection of infinite geometries has a bounding box");
    }
}
