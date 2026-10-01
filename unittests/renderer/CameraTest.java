package renderer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import renderer.sampling.Jittered;

import java.util.List;
import java.util.MissingResourceException;

/**
 * Unit tests for {@link Camera}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Camera.Builder#build()}</li>
 * <li>{@link Camera#constructRay(int, int)}</li>
 * <li>{@link Camera#constructRays(int, int)}</li>
 * <li>{@link Camera.Builder#setDepthOfField(double, double)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class CameraTest {

    /** Default constructor to satisfy JavaDoc generator */
    CameraTest() { /* to satisfy JavaDoc generator */ }

    /** The camera location */
    private static final Point LOCATION = Point.ZERO;
    /** A (non-unit) viewing direction along -Z */
    private static final Vector V_TO = new Vector(0, 0, -5);
    /** A (non-unit) up direction along Y */
    private static final Vector V_UP = new Vector(0, 2, 0);
    /** A target point in front of the camera */
    private static final Point TARGET = new Point(0, 0, -5);
    /** The view plane distance */
    private static final double VP_DISTANCE = 10;
    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Message for wrong constructed rays */
    private static final String ERROR_RAY = "constructRay() returned a wrong ray";
    /** Message for unexpected build failures */
    private static final String ERROR_VALID_BUILD = "A valid camera build threw an exception";
    /** Message for builds that should fail */
    private static final String ERROR_INVALID_BUILD = "An invalid camera build did not throw";

    /**
     * Creates a builder with a location and a view plane distance.
     *
     * @return the builder
     */
    private static Camera.Builder baseBuilder() {
        return Camera.getBuilder().setLocation(LOCATION).setVpDistance(VP_DISTANCE);
    }

    /**
     * Test method for {@link Camera.Builder#build()}.
     */
    @Test
    void testBuild() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Direction given by vectors
        assertDoesNotThrow(() -> baseBuilder().setDirection(V_TO, V_UP).setVpSize(8, 8).build(), ERROR_VALID_BUILD);

        // TC02: Direction given by a target point
        assertDoesNotThrow(() -> baseBuilder().setDirection(TARGET).setVpSize(8, 8).build(), ERROR_VALID_BUILD);

        // TC03: Direction given by a target point and an up vector
        assertDoesNotThrow(() -> baseBuilder().setDirection(TARGET, V_UP).setVpSize(8, 8).build(), ERROR_VALID_BUILD);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Missing settings
        // TC11: Missing location
        assertThrows(MissingResourceException.class,
                () -> Camera.getBuilder().setDirection(V_TO, V_UP).setVpDistance(VP_DISTANCE).setVpSize(8, 8).build(),
                ERROR_INVALID_BUILD);

        // TC12: Missing direction
        assertThrows(MissingResourceException.class,
                () -> baseBuilder().setVpSize(8, 8).build(), ERROR_INVALID_BUILD);

        // TC13: Missing view plane size
        assertThrows(IllegalArgumentException.class,
                () -> baseBuilder().setDirection(V_TO, V_UP).build(), ERROR_INVALID_BUILD);

        // TC14: Missing view plane distance
        assertThrows(IllegalArgumentException.class,
                () -> Camera.getBuilder().setLocation(LOCATION).setDirection(V_TO, V_UP).setVpSize(8, 8).build(),
                ERROR_INVALID_BUILD);

        // **** Group 2: Non-positive view plane
        // TC21: Zero width
        assertThrows(IllegalArgumentException.class, () -> baseBuilder().setDirection(V_TO, V_UP).setVpSize(0, 8).build(),
                ERROR_INVALID_BUILD);

        // TC22: Zero height
        assertThrows(IllegalArgumentException.class, () -> baseBuilder().setDirection(V_TO, V_UP).setVpSize(8, 0).build(),
                ERROR_INVALID_BUILD);

        // TC23: Negative width
        assertThrows(IllegalArgumentException.class, () -> baseBuilder().setDirection(V_TO, V_UP).setVpSize(-8, 8).build(),
                ERROR_INVALID_BUILD);

        // TC24: Negative height
        assertThrows(IllegalArgumentException.class, () -> baseBuilder().setDirection(V_TO, V_UP).setVpSize(8, -8).build(),
                ERROR_INVALID_BUILD);

        // TC25: Zero distance
        assertThrows(IllegalArgumentException.class,
                () -> baseBuilder().setVpDistance(0).setDirection(V_TO, V_UP).setVpSize(8, 8).build(), ERROR_INVALID_BUILD);

        // TC26: Negative distance
        assertThrows(IllegalArgumentException.class,
                () -> baseBuilder().setVpDistance(-1).setDirection(V_TO, V_UP).setVpSize(8, 8).build(), ERROR_INVALID_BUILD);

        // **** Group 3: Non-positive resolution
        // TC31: Zero columns
        assertThrows(IllegalArgumentException.class,
                () -> baseBuilder().setDirection(V_TO, V_UP).setVpSize(8, 8).setResolution(0, 1).build(), ERROR_INVALID_BUILD);

        // TC32: Zero rows
        assertThrows(IllegalArgumentException.class,
                () -> baseBuilder().setDirection(V_TO, V_UP).setVpSize(8, 8).setResolution(1, 0).build(), ERROR_INVALID_BUILD);

        // TC33: Negative columns
        assertThrows(IllegalArgumentException.class,
                () -> baseBuilder().setDirection(V_TO, V_UP).setVpSize(8, 8).setResolution(-1, 1).build(), ERROR_INVALID_BUILD);

        // TC34: Negative rows
        assertThrows(IllegalArgumentException.class,
                () -> baseBuilder().setDirection(V_TO, V_UP).setVpSize(8, 8).setResolution(1, -1).build(), ERROR_INVALID_BUILD);

        // **** Group 4: Invalid orientation
        // TC41: Up vector parallel to the viewing direction
        assertThrows(IllegalArgumentException.class,
                () -> baseBuilder().setDirection(V_TO, new Vector(0, 0, 1)).setVpSize(8, 8).build(), ERROR_INVALID_BUILD);
    }

    /**
     * Test method for {@link Camera#constructRay(int, int)}.
     */
    @Test
    void testConstructRay() {
        Camera camera4x4 = baseBuilder().setVpSize(8, 8).setResolution(4, 4).setDirection(V_TO, V_UP).build();
        Camera camera3x3 = baseBuilder().setVpSize(6, 6).setResolution(3, 3).setDirection(V_TO, V_UP).build();

        // ============ Equivalence Partitions Tests ==============

        // TC01: An inner pixel, direction given by vectors
        assertEquals(new Ray(LOCATION, new Vector(-1, 1, -10)), camera4x4.constructRay(1, 1), ERROR_RAY);

        // TC02: An inner pixel, direction given by a target point
        Camera byTarget = baseBuilder().setVpSize(8, 8).setResolution(4, 4).setDirection(TARGET).build();
        assertEquals(new Ray(LOCATION, new Vector(-1, 1, -10)), byTarget.constructRay(1, 1), ERROR_RAY);

        // TC03: An inner pixel, direction given by a target point and an up vector
        Camera byTargetAndUp = baseBuilder().setVpSize(8, 8).setResolution(4, 4).setDirection(TARGET, V_UP).build();
        assertEquals(new Ray(LOCATION, new Vector(-1, 1, -10)), byTargetAndUp.constructRay(1, 1), ERROR_RAY);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Even resolution (4x4)
        // TC11: A corner pixel
        assertEquals(new Ray(LOCATION, new Vector(-3, 3, -10)), camera4x4.constructRay(0, 0), ERROR_RAY);

        // TC12: An edge pixel
        assertEquals(new Ray(LOCATION, new Vector(-1, 3, -10)), camera4x4.constructRay(1, 0), ERROR_RAY);

        // **** Group 2: Odd resolution (3x3)
        // TC21: The center pixel
        assertEquals(new Ray(LOCATION, new Vector(0, 0, -10)), camera3x3.constructRay(1, 1), ERROR_RAY);

        // TC22: The middle pixel of the top row
        assertEquals(new Ray(LOCATION, new Vector(0, 2, -10)), camera3x3.constructRay(1, 0), ERROR_RAY);

        // TC23: The middle pixel of the left column
        assertEquals(new Ray(LOCATION, new Vector(-2, 0, -10)), camera3x3.constructRay(0, 1), ERROR_RAY);

        // TC24: A corner pixel
        assertEquals(new Ray(LOCATION, new Vector(-2, 2, -10)), camera3x3.constructRay(0, 0), ERROR_RAY);

        // **** Group 3: Other camera settings
        // TC31: The central ray is not affected by anti-aliasing
        Camera antiAliased = baseBuilder().setVpSize(6, 6).setResolution(3, 3).setDirection(V_TO, V_UP)
                .setAntiAliasing(3, new Jittered()).build();
        assertEquals(new Ray(LOCATION, new Vector(0, 0, -10)), antiAliased.constructRay(1, 1), ERROR_RAY);

        // TC32: Rotating the camera by 90° turns up into right
        Camera rotated = baseBuilder().setVpSize(6, 6).setResolution(3, 3).setDirection(V_TO, V_UP).rotate(90).build();
        assertEquals(new Ray(LOCATION, new Vector(2, 0, -10)), rotated.constructRay(1, 0), ERROR_RAY);
    }

    /**
     * Test method for {@link Camera#constructRays(int, int)}.
     */
    @Test
    void testConstructRays() {
        Camera.Builder builder = baseBuilder().setVpSize(8, 8).setResolution(4, 4).setDirection(V_TO, V_UP);
        Camera withoutAntiAliasing = builder.build();
        Camera withAntiAliasing = builder.setAntiAliasing(3, new Jittered()).build();

        // ============ Equivalence Partitions Tests ==============

        // TC01: Without anti-aliasing - the single central ray
        List<Ray> single = withoutAntiAliasing.constructRays(1, 1);
        assertEquals(List.of(new Ray(LOCATION, new Vector(-1, 1, -10))), single, "Wrong rays without anti-aliasing");

        // TC02: With 3x3 anti-aliasing - nine distinct rays
        List<Ray> beam = withAntiAliasing.constructRays(1, 1);
        assertEquals(9, beam.size(), "Wrong number of anti-aliasing rays");
        assertEquals(9, beam.stream().distinct().count(), "Anti-aliasing rays are not distinct");

        // =============== Boundary Values Tests ==================

        // TC11: A corner pixel
        assertEquals(9, withAntiAliasing.constructRays(0, 0).size(), "Wrong number of anti-aliasing rays in a corner");

        // TC12: Changing the builder does not affect a camera it already built
        assertEquals(1, withoutAntiAliasing.constructRays(1, 1).size(), "A built camera changed with its builder");
    }

    /**
     * Test method for {@link Camera.Builder#setDepthOfField(double, double)}.
     */
    @Test
    void testSetDepthOfField() {
        Camera.Builder builder = baseBuilder().setVpSize(8, 8).setResolution(4, 4).setDirection(V_TO, V_UP);
        // The pinhole ray of pixel (1,1) has direction (-1,1,-10), so it meets the plane at depth 20 here
        Point focus = new Point(-2, 2, -20);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Rays leave from random points of the aperture and all meet on the plane in focus
        Camera lens = builder.setDepthOfField(1, 20).build();
        for (int i = 0; i < 20; i++) {
            Ray ray = lens.constructRays(1, 1).getFirst();
            assertEquals(0, ray.origin().getZ(), DELTA, "A ray left the lens plane");
            assertTrue(ray.origin().distance(LOCATION) <= 1 + DELTA, "A ray left from outside the aperture");
            Point onFocusPlane = ray.getPoint((-20 - ray.origin().getZ()) / ray.direction().getZ());
            assertEquals(0, onFocusPlane.distance(focus), DELTA, "A ray missed the point in focus");
        }

        // =============== Boundary Values Tests ==================

        // TC11: A zero aperture is a pinhole camera
        assertEquals(List.of(new Ray(LOCATION, new Vector(-1, 1, -10))),
                builder.setDepthOfField(0, 20).build().constructRays(1, 1), "A zero aperture changed the ray");

        // TC12: A negative aperture
        assertThrows(IllegalArgumentException.class, () -> builder.setDepthOfField(-1, 20),
                "A negative aperture did not throw");

        // TC13: A zero focus distance
        assertThrows(IllegalArgumentException.class, () -> builder.setDepthOfField(1, 0),
                "A zero focus distance did not throw");
    }
}
