package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import geometries.api.Intersectable.Intersection;
import geometries.impl.Lathe.ProfilePoint;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link Lathe}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Lathe#Lathe(List)}</li>
 * <li>{@link Lathe#setSegments(int)}</li>
 * <li>{@link Lathe#revolve(Point, Vector)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class LatheTest {

    /** Default constructor to satisfy JavaDoc generator */
    LatheTest() { /* to satisfy JavaDoc generator */ }

    /** Delta value for comparing smooth normals with the exact surface normals */
    private static final double DELTA = 0.01;

    /** The profile of a closed cylinder of radius 10 and height 20: bottom, side and top */
    private static final List<ProfilePoint> CYLINDER = List.of(
            new ProfilePoint(0, 0), new ProfilePoint(10, 0), new ProfilePoint(10, 20), new ProfilePoint(0, 20));

    /**
     * Test method for {@link Lathe#Lathe(List)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A valid profile
        assertDoesNotThrow(() -> new Lathe(CYLINDER), "A valid profile threw an exception");

        // =============== Boundary Values Tests ==================

        // TC11: A single point
        assertThrows(IllegalArgumentException.class, () -> new Lathe(List.of(new ProfilePoint(1, 0))),
                "A single-point profile did not throw");

        // TC12: A negative radius
        assertThrows(IllegalArgumentException.class,
                () -> new Lathe(List.of(new ProfilePoint(1, 0), new ProfilePoint(-1, 1))), "A negative radius did not throw");

        // TC13: Two consecutive points coincide
        assertThrows(IllegalArgumentException.class,
                () -> new Lathe(List.of(new ProfilePoint(1, 0), new ProfilePoint(1, 0), new ProfilePoint(1, 1))),
                "Coinciding consecutive points did not throw");
    }

    /**
     * Test method for {@link Lathe#setSegments(int)}.
     */
    @Test
    void testSetSegments() {
        // =============== Boundary Values Tests ==================

        // TC11: Three segments - the minimum
        assertDoesNotThrow(() -> new Lathe(CYLINDER).setSegments(3), "Three segments threw an exception");

        // TC12: Two segments
        assertThrows(IllegalArgumentException.class, () -> new Lathe(CYLINDER).setSegments(2), "Two segments did not throw");
    }

    /**
     * Test method for {@link Lathe#revolve(Point, Vector)}.
     */
    @Test
    void testRevolve() {
        Geometries cylinder = new Lathe(CYLINDER).setSegments(32).revolve(Point.ZERO, Vector.AXIS_Y);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Profile points on the axis close the caps with one triangle per segment, and the side
        // takes two: 32 + 64 + 32
        assertEquals(128, cylinder.flatten().size(), "Wrong number of triangles");

        // TC02: The side is hit close to the true radius, with a horizontal normal pointing out
        Intersection side = firstHit(cylinder, new Ray(new Point(-50, 10, 1), Vector.AXIS_X));
        assertEquals(-10, side.point.getX(), 0.1, "The side was hit far from the true radius");
        Vector normal = side.geometry.getNormal(side.point);
        assertEquals(0, normal.getY(), DELTA, "The side normal is not horizontal");
        assertTrue(normal.getX() < 0, "The side normal points inwards");

        // TC03: A curved profile gets smooth normals that match the exact surface - a hemisphere
        List<ProfilePoint> arc = new ArrayList<>();
        for (int i = 0; i <= 16; i++) {
            double angle = Math.PI / 2 * i / 16;
            arc.add(new ProfilePoint(10 * Math.cos(angle), 10 * Math.sin(angle)));
        }
        Geometries dome = new Lathe(arc).setSegments(64).revolve(Point.ZERO, Vector.AXIS_Y);
        Intersection hit = firstHit(dome, new Ray(new Point(3, 50, 2), new Vector(0, -1, 0)));
        Vector exact = hit.point.subtract(Point.ZERO).normalize();
        assertEquals(1, hit.geometry.getNormal(hit.point).dotProduct(exact), DELTA,
                "The smooth normal differs from the exact surface normal");

        // =============== Boundary Values Tests ==================

        // TC11: A sharp corner stays a crease - the top cap keeps a vertical normal
        Intersection top = firstHit(cylinder, new Ray(new Point(1, 50, 2), new Vector(0, -1, 0)));
        assertEquals(Vector.AXIS_Y, top.geometry.getNormal(top.point), "The top cap normal was blended with the side");
    }

    /**
     * Finds the closest intersection of a ray with a mesh.
     *
     * @param mesh the mesh
     * @param ray  the ray
     * @return the closest intersection
     */
    private static Intersection firstHit(Geometries mesh, Ray ray) {
        return ray.findClosestIntersection(mesh.calcIntersections(ray));
    }
}
