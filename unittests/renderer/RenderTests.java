package renderer;

import static java.awt.Color.YELLOW;

import org.junit.jupiter.api.Test;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import primitives.Color;
import primitives.Point;
import scene.Scene;

/**
 * End-to-end rendering tests.
 * <p>
 * These tests demonstrate the full rendering pipeline:
 * scene construction → camera setup → ray tracing → image generation.
 * <p>
 * The first test produces a simple scene intended as a reference image
 * for validating Camera and Renderer implementations.
 */
@SuppressWarnings("java:S109")
class RenderTests {
   /** Default constructor to satisfy JavaDoc generator */
   RenderTests() { /* to satisfy JavaDoc generator */ }

   /**
    * Physical size of View Plane (it is a square: SIZExSIZE)
    */
   static final double VP_SIZE     = 500;
   /**
    * Distance from Camera to View Plane
    */
   static final double VP_DISTANCE = 100;

   /**
    * Camera location point
    */
   static final Point  LOCATION    = Point.ZERO;
   /**
    * Camera direction target point
    */
   static final Point  LOOK_AT     = new Point(0, 0, -1);
   /**
    * Image resolution (it is a square: NxN)
    */
   static final int    RESOLUTION  = 1000;

   /**
    * Creates a base camera builder for the tests.
    * @return camera builder configured with the common test settings
    */
   private static Camera.Builder baseCameraBuilder() {
      return Camera.getBuilder() //
         .setLocation(LOCATION).setDirection(LOOK_AT) //
         .setVpDistance(VP_DISTANCE).setVpSize(VP_SIZE, VP_SIZE) //
         .setResolution(RESOLUTION, RESOLUTION);
   }

   /**
    * Produce a scene with basic 3D model and render it into a png image with a
    * grid
    */
   @Test
   void testBasicRenderTwoColors() {
      Scene        scene  = new Scene("Two colors")                   //
         .setBackground(new Color(75, 127, 90))                       //
         .setAmbientLight(new AmbientLight(new Color(255, 191, 191)));

      final double Z      = -100D;
      // Left, Middle, Right X Bottom, Middle, Top
      Point        pLM    = new Point(-100, 0, Z);
      Point        pMT    = new Point(0, 100, Z);
      Point        pLT    = new Point(-100, 100, Z);
      Point        pMB    = new Point(0, -100, Z);
      Point        pLB    = new Point(-100, -100, Z);
      Point        pRM    = new Point(100, 0, Z);
      Point        pRB    = new Point(100, -100, Z);
      Point        o      = new Point(0, 0, Z);
      double       radius = 50D;

      scene.geometries //
         .add(// center
              new Sphere(o, radius),
              // up left
              new Triangle(pLM, pMT, pLT),
              // down left
              new Triangle(pLM, pMB, pLB),
              // down right
              new Triangle(pRM, pMB, pRB));

      baseCameraBuilder() //
         .setRayTracer(scene, RayTracerType.SIMPLE) //
         .build() //
         .renderImage() //
         .printGrid(100, new Color(YELLOW)) //
         .writeToImage("Two colors render test");
   }

   /**
    * Renders a scene loaded from an XML file.
    * <p>
    * Note: parsing logic should not be implemented inside tests.
    * @param  builder the camera builder to use
    * @param  xmlName the XML scene file name
    * @return         the camera after rendering
    */
   Camera renderSceneXML(Camera.Builder builder, String xmlName) {
      Scene scene = new Scene("Using XML");
      // Parse from XML file into scene object instead of the new Scene above,
      // Use the code you added in appropriate packages.
      // ...
      // NB: unit tests is not the correct place to put XML parsing code.

      return builder //
         .setRayTracer(scene, RayTracerType.SIMPLE) //
         .build() //
         .renderImage(); //
   }

   /**
    * Renders a scene loaded from a JSON file.
    * <p>
    * Note: parsing logic should not be implemented inside tests.
    * @param  builder  the camera builder to use
    * @param  jsonName the JSON scene file name
    * @return          the camera after rendering
    */
   static Camera renderSceneJSON(Camera.Builder builder, String jsonName) {
      Scene scene = new Scene("Using JSON");
      // Parse from JSON file into scene object instead of the new Scene above,
      // Use the code you added in appropriate packages.
      // ...
      // NB: unit tests is not the correct place to put JSON parsing code.

      return builder //
         .setRayTracer(scene, RayTracerType.SIMPLE) //
         .build() //
         .renderImage(); //
   }

   /** Test for XML based scene - for bonus */
   @Test
   void testBasicRenderXml() {
      renderSceneXML(baseCameraBuilder(), "basicRenderTestTwoColors") //
         .printGrid(100, new Color(YELLOW)) //
         .writeToImage("render test xml");
   }

   /** Test for JSON based scene - for bonus */
   @Test
   void testBasicRenderJson() {
      renderSceneJSON(baseCameraBuilder(), "basicRenderTestTwoColors") //
         .printGrid(100, new Color(YELLOW)) //
         .writeToImage("render test json");
   }
}
