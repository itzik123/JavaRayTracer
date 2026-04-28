package renderer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.MissingResourceException;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import renderer.Camera.Builder;

/**
 * Unit tests for class {@link Camera}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>Camera construction validity using {@link renderer.Camera.Builder}</li>
 * <li>{@link Camera#constructRay(int, int)}</li>
 * </ul>
 */
class CameraTests {

   /** Default constructor to satisfy documentation tools. */
   CameraTests() { /* Default constructor to satisfy documentation tools */ }

   /** Camera location used in ray construction tests. */
   private static final Point  LOCATION               = Point.ZERO;

   /** Forward direction used in tests. */
   private static final Vector V_TO                   = new Vector(0, 0, -5);

   /** Up direction used in tests. */
   private static final Vector V_UP                   = new Vector(0, 2, 0);

   /** Target point used in tests. */
   private static final Point  TARGET                 = new Point(0, 0, -5);

   /** Default view-plane distance used in tests. */
   private static final double VP_DISTANCE            = 10d;

   /** Error message for invalid ray construction. */
   private static final String ERROR_CONSTRUCT_RAY    = "constructRay() result is incorrect";

   /** Error message for unexpected exception in valid build scenario. */
   private static final String ERROR_VALID_BUILD      = "Valid camera build should not throw an exception";

   /** Error message for missing resource in camera build. */
   private static final String ERROR_MISSING_RESOURCE = "Expected MissingResourceException was not thrown";

   /** Error message for invalid argument in camera build. */
   private static final String ERROR_INVALID_ARGUMENT = "Expected IllegalArgumentException was not thrown";

   /**
    * Creates a basic builder with valid location and view-plane distance.
    * @return initialized camera builder
    */
   private Camera.Builder baseBuilder() {
      return Camera.getBuilder()
         .setLocation(LOCATION)
         .setVpDistance(VP_DISTANCE);
   }

   /**
    * Test method for {@link renderer.Camera.Builder}.
    * <p>
    * Verifies build validity only. The test checks whether camera construction
    * succeeds or fails with an exception, without checking geometric correctness.
    * </p>
    */
   @Test
   void testBuild() {
      // ============ Equivalence Partitions Tests ==============

      // EP01: Build succeeds with explicit forward and up vectors
      Builder builderEP01 = baseBuilder()
         .setDirection(V_TO, V_UP)
         .setVpSize(8, 8);
      assertDoesNotThrow(builderEP01::build, ERROR_VALID_BUILD);

      // EP02: Build succeeds with target point only
      Builder builderEP02 = baseBuilder()
         .setDirection(TARGET)
         .setVpSize(8, 8);
      assertDoesNotThrow(builderEP02::build, ERROR_VALID_BUILD);

      // EP03: Build succeeds with target point and explicit up vector
      Builder builderEP03 = baseBuilder()
         .setDirection(TARGET, V_UP)
         .setVpSize(8, 8);
      assertDoesNotThrow(builderEP03::build, ERROR_VALID_BUILD);

      // =============== Boundary Values Tests ==================

      // BV01: Build fails when camera location is missing
      Builder builderBV01 = Camera.getBuilder()
         .setDirection(V_TO, V_UP)
         .setVpDistance(VP_DISTANCE)
         .setVpSize(8, 8);
      assertThrows(MissingResourceException.class, builderBV01::build, ERROR_MISSING_RESOURCE);

      // BV02: Build fails when camera direction is missing
      Builder builderBV02 = Camera.getBuilder()
         .setLocation(LOCATION)
         .setVpDistance(VP_DISTANCE)
         .setVpSize(8, 8);
      assertThrows(MissingResourceException.class, builderBV02::build, ERROR_MISSING_RESOURCE);

      // BV03: Build fails when view-plane size is missing
      Builder builderBV03 = Camera.getBuilder()
         .setLocation(LOCATION)
         .setDirection(V_TO, V_UP)
         .setVpDistance(VP_DISTANCE);
      assertThrows(IllegalArgumentException.class, builderBV03::build,
                   "Build should fail when view-plane size was not set");

      // BV04: Build fails when view-plane distance is missing
      Builder builderBV04 = Camera.getBuilder()
         .setLocation(LOCATION)
         .setDirection(V_TO, V_UP)
         .setVpSize(8, 8);
      assertThrows(IllegalArgumentException.class, builderBV04::build,
                   "Build should fail when view-plane distance from camera was not set");

      // BV05: Build fails with zero view-plane width
      Builder builderBV05 = baseBuilder()
         .setDirection(V_TO, V_UP)
         .setVpSize(0, 8);
      assertThrows(IllegalArgumentException.class, builderBV05::build, ERROR_INVALID_ARGUMENT);

      // BV06: Build fails with zero view-plane height
      Builder builderBV06 = baseBuilder()
         .setDirection(V_TO, V_UP)
         .setVpSize(8, 0);
      assertThrows(IllegalArgumentException.class, builderBV06::build, ERROR_INVALID_ARGUMENT);

      // BV07: Build fails with negative view-plane width
      Builder builderBV07 = baseBuilder()
         .setDirection(V_TO, V_UP)
         .setVpSize(-8, 8);
      assertThrows(IllegalArgumentException.class, builderBV07::build, ERROR_INVALID_ARGUMENT);

      // BV08: Build fails with negative view-plane height
      Builder builderBV08 = baseBuilder()
         .setDirection(V_TO, V_UP)
         .setVpSize(8, -8);
      assertThrows(IllegalArgumentException.class, builderBV08::build, ERROR_INVALID_ARGUMENT);

      // BV09: Build fails with zero view-plane distance
      Builder builderBV09 = Camera.getBuilder()
         .setLocation(LOCATION)
         .setDirection(V_TO, V_UP)
         .setVpDistance(0)
         .setVpSize(8, 8);
      assertThrows(IllegalArgumentException.class, builderBV09::build, ERROR_INVALID_ARGUMENT);

      // BV10: Build fails with negative view-plane distance
      Builder builderBV10 = Camera.getBuilder()
         .setLocation(LOCATION)
         .setDirection(V_TO, V_UP)
         .setVpDistance(-1)
         .setVpSize(8, 8);
      assertThrows(IllegalArgumentException.class, builderBV10::build, ERROR_INVALID_ARGUMENT);

      // BV11: Build fails with zero horizontal resolution
      Builder builderBV11 = baseBuilder()
         .setDirection(V_TO, V_UP)
         .setVpSize(8, 8)
         .setResolution(0, 1);
      assertThrows(IllegalArgumentException.class, builderBV11::build, ERROR_INVALID_ARGUMENT);

      // BV12: Build fails with zero vertical resolution
      Builder builderBV12 = baseBuilder()
         .setDirection(V_TO, V_UP)
         .setVpSize(8, 8)
         .setResolution(1, 0);
      assertThrows(IllegalArgumentException.class, builderBV12::build, ERROR_INVALID_ARGUMENT);

      // BV13: Build fails with negative horizontal resolution
      Builder builderBV13 = baseBuilder()
         .setDirection(V_TO, V_UP)
         .setVpSize(8, 8)
         .setResolution(-1, 1);
      assertThrows(IllegalArgumentException.class, builderBV13::build, ERROR_INVALID_ARGUMENT);

      // BV14: Build fails with negative vertical resolution
      Builder builderBV14 = baseBuilder()
         .setDirection(V_TO, V_UP)
         .setVpSize(8, 8)
         .setResolution(1, -1);
      assertThrows(IllegalArgumentException.class, builderBV14::build, ERROR_INVALID_ARGUMENT);
   }

   /**
    * Test method for {@link Camera#constructRay(int, int)}.
    * <p>
    * Verifies ray construction through representative pixels in 3x3 and 4x4
    * view planes, including all three direction-setting overloads.
    * </p>
    */
   @Test
   void testConstructRay() {
      Camera camera4x4ByVectors     = baseBuilder()
         .setVpSize(8, 8)
         .setResolution(4, 4)
         .setDirection(V_TO, V_UP)
         .build();

      Camera camera4x4ByTarget      = baseBuilder()
         .setVpSize(8, 8)
         .setResolution(4, 4)
         .setDirection(TARGET)
         .build();

      Camera camera4x4ByTargetAndUp = baseBuilder()
         .setVpSize(8, 8)
         .setResolution(4, 4)
         .setDirection(TARGET, V_UP)
         .build();

      Camera camera3x3              = baseBuilder()
         .setDirection(V_TO, V_UP)
         .setVpSize(6, 6)
         .setResolution(3, 3)
         .build();

      // ============ Equivalence Partitions Tests ==============

      // EP01: Construct ray through an interior pixel in a 4x4 view plane using
      // setDirection(Vector, Vector)
      Ray    rayEP01                = camera4x4ByVectors.constructRay(1, 1);
      assertEquals(new Ray(LOCATION, new Vector(-1, 1, -10)), rayEP01, ERROR_CONSTRUCT_RAY);

      // EP02: Construct ray through an interior pixel in a 4x4 view plane using
      // setDirection(Point)
      Ray rayEP02 = camera4x4ByTarget.constructRay(1, 1);
      assertEquals(new Ray(LOCATION, new Vector(-1, 1, -10)), rayEP02, ERROR_CONSTRUCT_RAY);

      // EP03: Construct ray through an interior pixel in a 4x4 view plane using
      // setDirection(Point, Vector)
      Ray rayEP03 = camera4x4ByTargetAndUp.constructRay(1, 1);
      assertEquals(new Ray(LOCATION, new Vector(-1, 1, -10)), rayEP03, ERROR_CONSTRUCT_RAY);

      // =============== Boundary Values Tests ==================

      // BV01: Construct ray through a corner pixel in a 4x4 view plane
      Ray rayBV01 = camera4x4ByVectors.constructRay(0, 0);
      assertEquals(new Ray(LOCATION, new Vector(-3, 3, -10)), rayBV01, ERROR_CONSTRUCT_RAY);

      // BV02: Construct ray through a side pixel in a 4x4 view plane
      Ray rayBV02 = camera4x4ByVectors.constructRay(1, 0);
      assertEquals(new Ray(LOCATION, new Vector(-1, 3, -10)), rayBV02, ERROR_CONSTRUCT_RAY);

      // BV03: Construct ray through the center pixel in a 3x3 view plane
      Ray rayBV03 = camera3x3.constructRay(1, 1);
      assertEquals(new Ray(LOCATION, new Vector(0, 0, -10)), rayBV03, ERROR_CONSTRUCT_RAY);

      // BV04: Construct ray through the upper side middle pixel in a 3x3 view plane
      Ray rayBV04 = camera3x3.constructRay(1, 0);
      assertEquals(new Ray(LOCATION, new Vector(0, 2, -10)), rayBV04, ERROR_CONSTRUCT_RAY);

      // BV05: Construct ray through the left side middle pixel in a 3x3 view plane
      Ray rayBV05 = camera3x3.constructRay(0, 1);
      assertEquals(new Ray(LOCATION, new Vector(-2, 0, -10)), rayBV05, ERROR_CONSTRUCT_RAY);

      // BV06: Construct ray through a corner pixel in a 3x3 view plane
      Ray rayBV06 = camera3x3.constructRay(0, 0);
      assertEquals(new Ray(LOCATION, new Vector(-2, 2, -10)), rayBV06, ERROR_CONSTRUCT_RAY);
   }
}
