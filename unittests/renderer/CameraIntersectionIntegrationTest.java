package renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import geometries.api.Intersectable;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import java.util.List;

/**
 * Integration tests of {@link Camera} ray construction with geometry intersections.
 * <p>
 * The tests verify the total number of intersections between the rays of a 3x3 view plane and
 * {@link Sphere}, {@link Plane} and {@link Triangle} geometries.
 *
 * @author ityy35-bit
 */
class CameraIntersectionIntegrationTest {

    /** Default constructor to satisfy JavaDoc generator */
    CameraIntersectionIntegrationTest() { /* to satisfy JavaDoc generator */ }

    /** The view plane resolution (both axes) */
    private static final int RESOLUTION = 3;
    /** A camera at (0,0,0.5) */
    private static final Camera CAMERA_FRONT = buildCamera(new Point(0, 0, 0.5));
    /** A camera at the origin */
    private static final Camera CAMERA_ORIGIN = buildCamera(Point.ZERO);

    /**
     * Builds a camera looking down -Z at a 3x3 view plane of size 3 at distance 1.
     *
     * @param location the camera location
     * @return the camera
     */
    private static Camera buildCamera(Point location) {
        return Camera.getBuilder()
                .setLocation(location)
                .setDirection(new Vector(0, 0, -1), Vector.AXIS_Y)
                .setVpDistance(1).setVpSize(3, 3)
                .setResolution(RESOLUTION, RESOLUTION)
                .build();
    }

    /**
     * Asserts the total number of intersections of the rays through all the pixels with a
     * geometry.
     *
     * @param expected the expected number of intersections
     * @param camera   the camera
     * @param geometry the geometry
     * @param message  the failure message
     */
    private static void assertIntersections(int expected, Camera camera, Intersectable geometry, String message) {
        int count = 0;
        for (int i = 0; i < RESOLUTION; ++i)
            for (int j = 0; j < RESOLUTION; ++j) {
                List<Point> intersections = geometry.findIntersections(camera.constructRay(j, i));
                if (intersections != null) count += intersections.size();
            }
        assertEquals(expected, count, message);
    }

    /**
     * Integration test of camera rays with spheres.
     */
    @Test
    void testCameraSphereIntegration() {
        // TC01: A small sphere in front of the view plane (2 points)
        assertIntersections(2, CAMERA_FRONT, new Sphere(new Point(0, 0, -3), 1), "Wrong count for a small sphere");

        // TC02: A large sphere in front of the view plane (18 points)
        assertIntersections(18, CAMERA_FRONT, new Sphere(new Point(0, 0, -2.5), 2.5), "Wrong count for a large sphere");

        // TC03: A medium sphere in front of the view plane (10 points)
        assertIntersections(10, CAMERA_FRONT, new Sphere(new Point(0, 0, -2), 2), "Wrong count for a medium sphere");

        // TC04: A sphere containing the camera (9 points)
        assertIntersections(9, CAMERA_ORIGIN, new Sphere(new Point(0, 0, -1), 4), "Wrong count for a sphere around the camera");

        // TC05: A sphere behind the camera (0 points)
        assertIntersections(0, CAMERA_ORIGIN, new Sphere(new Point(0, 0, 1), 0.5), "Wrong count for a sphere behind the camera");
    }

    /**
     * Integration test of camera rays with planes.
     */
    @Test
    void testCameraPlaneIntegration() {
        // TC01: A plane parallel to the view plane (9 points)
        assertIntersections(9, CAMERA_ORIGIN, new Plane(new Point(0, 0, -5), Vector.AXIS_Z),
                "Wrong count for a parallel plane");

        // TC02: A slightly tilted plane (9 points)
        assertIntersections(9, CAMERA_ORIGIN, new Plane(new Point(0, 0, -5), new Vector(0, 1, 2)),
                "Wrong count for a slightly tilted plane");

        // TC03: A steeply tilted plane, missed by the bottom row (6 points)
        assertIntersections(6, CAMERA_ORIGIN, new Plane(new Point(0, 0, -5), new Vector(0, 1, 1)),
                "Wrong count for a steeply tilted plane");
    }

    /**
     * Integration test of camera rays with triangles.
     */
    @Test
    void testCameraTriangleIntegration() {
        // TC01: A small triangle, hit by the center ray only (1 point)
        assertIntersections(1, CAMERA_ORIGIN,
                new Triangle(new Point(0, 1, -2), new Point(1, -1, -2), new Point(-1, -1, -2)),
                "Wrong count for a small triangle");

        // TC02: A tall triangle, hit by the center and top rays (2 points)
        assertIntersections(2, CAMERA_ORIGIN,
                new Triangle(new Point(0, 20, -2), new Point(1, -1, -2), new Point(-1, -1, -2)),
                "Wrong count for a tall triangle");
    }
}
