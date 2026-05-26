package renderer;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Test rendering a basic image
 * @author Dan
 */
@SuppressWarnings("java:S109")
class RenderStage6Tests {
   /** Default constructor to satisfy JavaDoc generator */
   RenderStage6Tests() { /* to satisfy JavaDoc generator */ }

   /** Resolution (both X and Y) */
   private static final int    RESOLUTION           = 1001;

   /** View plane size (both height and width) */
   private static final double SIZE                 = 500D;

   /** Distance from camera to view plane */
   private static final double DISTANCE             = 100D;

   /** Grid interval (pixels) */
   private static final int    INTERVAL             = 100;

   /** Z axis location of triangles */
   private static final double Z                    = -100D;
   /** Most left X */
   private static final double XL                   = -100D;
   /** Middle X */
   private static final double XM                   = 0D;
   /** Most right X */
   private static final double XR                   = 100D;
   /** Bottom Y */
   private static final double YB                   = -100D;
   /** Middle Y */
   private static final double YM                   = 0D;
   /** Top Y */
   private static final double YT                   = 100D;
   /** Left, Top point */
   private static final Point  P_LT                 = new Point(XL, YT, Z);
   /** Left, Middle point */
   private static final Point  P_LM                 = new Point(XL, YM, Z);
   /** Left, Bottom point */
   private static final Point  P_LB                 = new Point(XL, YB, Z);
   /** Middle, Top point */
   private static final Point  P_MT                 = new Point(XM, YT, Z);
   /** Middle, Bottom point */
   private static final Point  P_MB                 = new Point(XM, YB, Z);
   /** Right, Middle point */
   private static final Point  P_RM                 = new Point(XR, YM, Z);
   /** Right, Bottom point */
   private static final Point  P_RB                 = new Point(XR, YB, Z);
   /** Sphere center point */
   private static final Point  O                    = new Point(XM, YM, Z);
   /** Sphere radius */
   private static final double RADIUS               = 50D;

   /** The sphere in the tests */
   private final Sphere        _sphere              = new Sphere(O, RADIUS);
   /** The left top triangle in the tests */
   private final Triangle      _triangleLeftTop     = new Triangle(P_LM, P_MT, P_LT);
   /** The left bottom triangle in the tests */
   private final Triangle      _triangleLeftBottom  = new Triangle(P_LM, P_MB, P_LB);
   /** The right bottom triangle in the tests */
   private final Triangle      _triangleRightBottom = new Triangle(P_RM, P_MB, P_RB);

   /**
    * Build camera and render image with grid
    * @param scene    the scene to be used for the image
    * @param fileName the name of the image file
    */
   private static void createImage(Scene scene, String fileName) {
      Camera.getBuilder() //
         .setResolution(RESOLUTION, RESOLUTION) //
         .setLocation(Point.ZERO).setDirection(new Point(0, 0, -1), Vector.AXIS_Y) //
         .setVpDistance(DISTANCE).setVpSize(SIZE, SIZE) //
         .setRayTracer(scene, RayTracerType.SIMPLE) //
         .build() //
         .renderImage() //
         .printGrid(INTERVAL, new Color(WHITE)) //
         .writeToImage(fileName);
   }

   /**
    * Produce a scene with basic 3D model - including individual emission lights of
    * the
    * bodies and render it into a png image with a grid
    */
   @Test
   void testRenderEmissionColor() {
      Scene scene = new Scene("Emission color").setAmbientLight(new AmbientLight(new Color(51, 51, 51)));
      scene.geometries //
         .add(_sphere, // no emission
              _triangleLeftTop.setEmission(new Color(GREEN)),
              _triangleLeftBottom.setEmission(new Color(RED)),
              _triangleRightBottom.setEmission(new Color(BLUE)));
      createImage(scene, "emission render test");
   }

   /**
    * Produce a scene with basic 3D model - including ambient light attenuation
    * factors of the
    * bodies and render it into a png image with a grid
    */
   @Test
   @Disabled("To be updated and enabled by students")
   void testRenderAmbientColor() {
      Scene scene = new Scene("Ambient colors"); // TODO by students
      scene.geometries //
         .add(_sphere, // TODO by students
              _triangleLeftTop, // TODO by students
              _triangleLeftBottom, // TODO by students
              _triangleRightBottom // TODO by students
         );
      createImage(scene, "ambient render test");
   }
}
