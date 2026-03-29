package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for class {@link Geometries}.
 * The tests verify:
 * <ul>
 * <li>{@link Geometries#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author itzik123
 */
class GeometriesTest {

    private static final Triangle TRIANGLE = new Triangle(new Point(-1,-1,0), new Point(-1,1,0), new Point(1,0,0));
    private static final Sphere SPHERE = new Sphere(new Point(0,0,0), 1);
    private static final Tube TUBE = new Tube(0.5, new Ray(new Point(0,0,0), new Vector(0,1,0)));

    private static final Geometries GEOMETRIES = new Geometries(TRIANGLE, SPHERE, TUBE);

    /** EP01: Ray intersects some of the geometries (Sphere and Triangle, 3 points) */
    private static final Ray EP01 = new Ray(new Point(-0.9, 0, 2), new Vector(0, 0, -1));

    /** BV11: Ray does not intersect any of the geometries (0 points) */
    private static final Ray BV11 = new Ray(new Point(1, 1, 2), new Vector(0, 0, -1));

    /** BV12: Ray intersects exactly one geometry (Tube, 2 points) */
    private static final Ray BV12 = new Ray(new Point(0, 2, 2), new Vector(0, 0, -1));

    /** BV13: Ray intersects all the geometries in the collection (5 points) */
    private static final Ray BV13 = new Ray(new Point(0, 0, 2), new Vector(0, 0, -1));

    /**
     * test method for {@link Geometries#findIntersections(Ray)}
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Some geometries are intersected
        List<Point> resultEP01 = GEOMETRIES.findIntersections(EP01);
        assertNotNull(resultEP01, "ERROR: Expected intersections but got null");
        assertEquals(3, resultEP01.size(), "ERROR: wrong intersection count for some geometries");

        // =============== Boundary Values Tests ==================

        // TC11: No geometries are intersected
        assertNull(GEOMETRIES.findIntersections(BV11), "ERROR: Expected null when no geometry is intersected");

        // TC12: Only one geometry is intersected
        List<Point> resultBV12 = GEOMETRIES.findIntersections(BV12);
        assertNotNull(resultBV12, "ERROR: Expected intersections but got null");
        assertEquals(2, resultBV12.size(), "ERROR: wrong intersection count for one geometry");

        // TC13: All geometries are intersected
        List<Point> resultBV13 = GEOMETRIES.findIntersections(BV13);
        assertNotNull(resultBV13, "ERROR: Expected intersections but got null");
        assertEquals(5, resultBV13.size(), "ERROR: wrong intersection count for all geometries");

        // TC14: Empty geometries collection
        Geometries emptyGeometries = new Geometries();
        assertNull(emptyGeometries.findIntersections(EP01), "ERROR: Expected null for empty geometries collection");
    }
}