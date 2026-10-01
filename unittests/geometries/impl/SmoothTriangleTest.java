package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Unit tests for {@link SmoothTriangle}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link SmoothTriangle#getNormal(Point)}</li>
 * <li>{@link SmoothTriangle#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class SmoothTriangleTest {

    /** Default constructor to satisfy JavaDoc generator */
    SmoothTriangleTest() { /* to satisfy JavaDoc generator */ }

    /** A triangle in the XY plane whose vertex normals lean away from its corners */
    private static final SmoothTriangle TRIANGLE = new SmoothTriangle(
            Point.ZERO, new Point(1, 0, 0), new Point(0, 1, 0),
            Vector.AXIS_Z, new Vector(1, 0, 1), new Vector(0, 1, 1));

    /**
     * Test method for {@link SmoothTriangle#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Inside the triangle the vertex normals are blended by the barycentric weights
        Vector n2 = new Vector(1, 0, 1).normalize(), n3 = new Vector(0, 1, 1).normalize();
        Vector blended = Vector.AXIS_Z.add(n2).add(n3).normalize();
        assertEquals(blended, TRIANGLE.getNormal(new Point(1.0 / 3, 1.0 / 3, 0)), "Wrong normal at the centroid");

        // =============== Boundary Values Tests ==================

        // TC11: At a vertex, the normal of that vertex
        assertEquals(n2, TRIANGLE.getNormal(new Point(1, 0, 0)), "Wrong normal at a vertex");

        // TC12: On an edge, the average of its two vertex normals
        assertEquals(Vector.AXIS_Z.add(n2).normalize(), TRIANGLE.getNormal(new Point(0.5, 0, 0)), "Wrong normal on an edge");
    }

    /**
     * Test method for {@link SmoothTriangle#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Smooth normals do not change the shape - a ray through the inside hits it
        assertEquals(List.of(new Point(0.25, 0.25, 0)),
                TRIANGLE.findIntersections(new Ray(new Point(0.25, 0.25, 1), new Vector(0, 0, -1))),
                "Wrong intersection with a smooth triangle");
    }
}
