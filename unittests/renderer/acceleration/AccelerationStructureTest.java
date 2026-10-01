package renderer.acceleration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import geometries.api.Intersectable;
import geometries.api.Intersectable.Intersection;
import geometries.impl.Cylinder;
import geometries.impl.Geometries;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Unit tests for the {@link AccelerationStructure} implementations: {@link LinearScan},
 * {@link RegularGrid} and {@link AdaptiveGrid}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link AccelerationStructure#findClosestIntersection(Ray)}</li>
 * <li>{@link AccelerationStructure#traverse(Ray, double, AccelerationStructure.IntersectionVisitor)}</li>
 * </ul>
 * The spatial structures must be exact accelerations: on a random scene they have to return the
 * very same intersections as the brute-force linear scan, for thousands of random rays.
 *
 * @author itzik123
 */
class AccelerationStructureTest {

    /** Default constructor to satisfy JavaDoc generator */
    AccelerationStructureTest() { /* to satisfy JavaDoc generator */ }

    /** The number of random rays per comparison */
    private static final int RAYS = 3000;
    /** The seed of the random scene and rays, for reproducible results */
    private static final long SEED = 5786;
    /** The random scene: many small bounded geometries plus two unbounded ones */
    private static final Geometries SCENE = randomScene(new Random(SEED));

    /**
     * Builds a scene of random spheres, triangles, polygons and cylinders in a 100-unit cube, with
     * a floor plane and a tube crossing it.
     *
     * @param random the random generator
     * @return the scene geometry
     */
    private static Geometries randomScene(Random random) {
        Geometries scene = new Geometries(
                new Plane(new Point(0, -60, 0), Vector.AXIS_Y),
                new Tube(3, new Ray(new Point(-50, 0, 0), new Vector(1, 0.3, 0.2))));
        for (int i = 0; i < 150; i++) {
            Point p = randomPoint(random, 50);
            scene.add(new Sphere(p, 1 + random.nextDouble() * 4));
            scene.add(new Triangle(p, p.add(randomVector(random, 8)), p.add(randomVector(random, 8))));
        }
        for (int i = 0; i < 30; i++) {
            Point p = randomPoint(random, 50);
            scene.add(new Cylinder(0.5 + random.nextDouble() * 2, new Ray(p, randomVector(random, 1)),
                    2 + random.nextDouble() * 10));
            scene.add(new Polygon(p, p.add(new Vector(6, 0, 0)), p.add(new Vector(6, 6, 1)), p.add(new Vector(0, 6, 1))));
        }
        return scene;
    }

    /**
     * Picks a random point in a cube around the origin.
     *
     * @param random the random generator
     * @param extent the half size of the cube
     * @return a random point in the cube {@code [-extent, extent]³}
     */
    private static Point randomPoint(Random random, double extent) {
        return new Point((random.nextDouble() * 2 - 1) * extent, (random.nextDouble() * 2 - 1) * extent,
                (random.nextDouble() * 2 - 1) * extent);
    }

    /**
     * Picks a random direction.
     *
     * @param random the random generator
     * @param extent the maximum absolute value of a component
     * @return a random non-zero vector
     */
    private static Vector randomVector(Random random, double extent) {
        return new Vector(randomPoint(random, extent).getX() + 1e-3, randomPoint(random, extent).getY(),
                randomPoint(random, extent).getZ());
    }

    /**
     * Creates the structures under test over the random scene.
     *
     * @return the linear scan followed by the spatial structures
     */
    private static List<AccelerationStructure> structures() {
        List<Intersectable> geometries = SCENE.flatten();
        return List.of(new LinearScan(SCENE), new RegularGrid(geometries, 8), new RegularGrid(geometries, 40),
                new AdaptiveGrid(geometries, 4, 8), new AdaptiveGrid(geometries, 16, 3));
    }

    /**
     * Collects all the intersections a structure passes to a visitor.
     *
     * @param structure   the structure
     * @param ray         the ray
     * @param maxDistance the maximum distance
     * @return the intersections, sorted by distance from the ray origin
     */
    private static List<Intersection> collect(AccelerationStructure structure, Ray ray, double maxDistance) {
        List<Intersection> result = new ArrayList<>();
        structure.traverse(ray, maxDistance, result::add);
        result.sort((a, b) -> Double.compare(a.point.distanceSquared(ray.origin()), b.point.distanceSquared(ray.origin())));
        return result;
    }

    /**
     * Test method for {@link AccelerationStructure#findClosestIntersection(Ray)}.
     */
    @Test
    void testFindClosestIntersection() {
        List<AccelerationStructure> structures = structures();
        Random random = new Random(SEED + 1);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Rays from outside the scene box towards it
        for (int i = 0; i < RAYS; i++) {
            Point origin = randomPoint(random, 120);
            Ray ray = new Ray(origin, randomPoint(random, 40).subtract(origin));
            Intersection expected = structures.getFirst().findClosestIntersection(ray);
            for (AccelerationStructure structure : structures.subList(1, structures.size())) {
                Intersection actual = structure.findClosestIntersection(ray);
                if (expected == null) assertNull(actual, structure.getClass().getSimpleName() + " found a phantom hit");
                else {
                    assertSame(expected.geometry, actual.geometry, structure.getClass().getSimpleName() + " hit another geometry");
                    assertEquals(expected.point, actual.point, structure.getClass().getSimpleName() + " hit another point");
                }
            }
        }

        // TC02: Rays starting inside the scene box, in random directions
        for (int i = 0; i < RAYS; i++) {
            Ray ray = new Ray(randomPoint(random, 50), randomVector(random, 1));
            Intersection expected = structures.getFirst().findClosestIntersection(ray);
            for (AccelerationStructure structure : structures.subList(1, structures.size())) {
                Intersection actual = structure.findClosestIntersection(ray);
                assertEquals(expected == null ? null : expected.point, actual == null ? null : actual.point,
                        structure.getClass().getSimpleName() + " disagrees with the linear scan");
            }
        }

        // =============== Boundary Values Tests ==================

        // TC11: Axis-aligned rays, which have zero direction components
        for (Vector axis : List.of(Vector.AXIS_X, Vector.AXIS_Y, Vector.AXIS_Z, new Vector(0, 0, -1))) {
            Ray ray = new Ray(new Point(-100, 0.5, 0.25).add(axis.scale(-1)), axis);
            Intersection expected = structures.getFirst().findClosestIntersection(ray);
            for (AccelerationStructure structure : structures.subList(1, structures.size())) {
                Intersection actual = structure.findClosestIntersection(ray);
                assertEquals(expected == null ? null : expected.point, actual == null ? null : actual.point,
                        structure.getClass().getSimpleName() + " disagrees on an axis-aligned ray");
            }
        }
    }

    /**
     * Test method for
     * {@link AccelerationStructure#traverse(Ray, double, AccelerationStructure.IntersectionVisitor)}.
     */
    @Test
    void testTraverse() {
        List<AccelerationStructure> structures = structures();
        Random random = new Random(SEED + 2);

        // ============ Equivalence Partitions Tests ==============

        // TC01: All the intersections within a limited distance are visited exactly once
        for (int i = 0; i < RAYS; i++) {
            Ray ray = new Ray(randomPoint(random, 60), randomVector(random, 1));
            double maxDistance = random.nextDouble() * 150;
            List<Intersection> expected = collect(structures.getFirst(), ray, maxDistance);
            for (AccelerationStructure structure : structures.subList(1, structures.size()))
                assertEquals(expected.stream().map(x -> x.point).toList(),
                        collect(structure, ray, maxDistance).stream().map(x -> x.point).toList(),
                        structure.getClass().getSimpleName() + " visited different intersections");
        }

        // =============== Boundary Values Tests ==================

        // TC11: The visitor stops the traversal after the first intersection
        Ray ray = new Ray(new Point(0, 100, 0), new Vector(0, -1, 0));
        for (AccelerationStructure structure : structures) {
            int[] visits = {0};
            structure.traverse(ray, Double.POSITIVE_INFINITY, intersection -> ++visits[0] < 1);
            assertEquals(1, visits[0], structure.getClass().getSimpleName() + " did not stop when asked");
        }
    }

    /**
     * Test method for {@link AccelerationStructure#findClosestIntersection(Ray)} on degenerate
     * scenes.
     */
    @Test
    void testDegenerateScenes() {
        Ray ray = new Ray(new Point(0, 10, 0), new Vector(0, -1, 0));

        // =============== Boundary Values Tests ==================

        // TC11: An empty scene
        Geometries empty = new Geometries();
        assertNull(new RegularGrid(empty.flatten(), 10).findClosestIntersection(ray), "Hit in an empty grid");
        assertNull(new AdaptiveGrid(empty.flatten(), 4, 4).findClosestIntersection(ray), "Hit in an empty octree");

        // TC12: A scene with unbounded geometry only
        Geometries planeOnly = new Geometries(new Plane(Point.ZERO, Vector.AXIS_Y));
        assertEquals(Point.ZERO, new RegularGrid(planeOnly.flatten(), 10).findClosestIntersection(ray).point,
                "The grid missed an unbounded geometry");
        assertEquals(Point.ZERO, new AdaptiveGrid(planeOnly.flatten(), 4, 4).findClosestIntersection(ray).point,
                "The octree missed an unbounded geometry");
    }
}
