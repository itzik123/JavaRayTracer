package renderer;

import static java.awt.Color.BLUE;

import org.junit.jupiter.api.Test;

import geometries.api.Geometry;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.*;
import scene.Scene;
import scene.SceneXMLParser;

import java.util.List;

/**
 * Test rendering a basic image
 * @author Dan Zilberstein
 */
class LightsTests {
   /** Constant for tests resolution */
   private static final int RESOLUTION = 500;

   /** Default constructor to satisfy JavaDoc generator */
   LightsTests() { /* to satisfy JavaDoc generator */ }

   /** First scene for some of tests */
   private final Scene           _scene1                   = new Scene("Test scene");
   /** Second scene for some of tests */
   private final Scene           _scene2                   = new Scene("Test scene")
      .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

   /** First camera builder for some of tests */
   private final Camera.Builder  _camera1                  = Camera.getBuilder()                                //
      .setRayTracer(_scene1, RayTracerType.SIMPLE)                                                              //
      .setLocation(new Point(0, 0, 1000))                                                                       //
      .setDirection(Point.ZERO, Vector.AXIS_Y)                                                                  //
      .setVpSize(150, 150).setVpDistance(1000);

   /** Second camera builder for some of tests */
   private final Camera.Builder  _camera2                  = Camera.getBuilder()                                //
      .setRayTracer(_scene2, RayTracerType.SIMPLE)                                                              //
      .setLocation(new Point(0, 0, 1000))                                                                       //
      .setDirection(Point.ZERO, Vector.AXIS_Y)                                                                  //
      .setVpSize(200, 200).setVpDistance(1000);

   /** Shininess value for most of the geometries in the tests */
   private static final int      SHININESS                 = 301;
   /** Diffusion attenuation factor for some of the geometries in the tests */
   private static final double   KD                        = 0.5;
   /** Diffusion attenuation factor for some of the geometries in the tests */
   private static final Double3  KD3                       = new Double3(0.2, 0.6, 0.4);

   /** Specular attenuation factor for some of the geometries in the tests */
   private static final double   KS                        = 0.5;
   /** Specular attenuation factor for some of the geometries in the tests */
   private static final Double3  KS3                       = new Double3(0.2, 0.4, 0.3);

   /** Material for some of the geometries in the tests */
   private static final Material MATERIAL                  = new Material().setKd(KD3).setKs(KS3)
      .setShininess(SHININESS);
   /** Light color for tests with triangles */
   private static final Color    TRIANGLES_LIGHT_COLOR     = new Color(800, 500, 250);
   /** Light color for tests with sphere */
   private static final Color    SPHERE_LIGHT_COLOR        = new Color(800, 500, 0);
   /** Color of the sphere */
   private static final Color    SPHERE_COLOR              = new Color(BLUE).reduce(2);

   /** Center of the sphere */
   private static final Point    SPHERE_CENTER             = new Point(0, 0, -50);
   /** Radius of the sphere */
   private static final double   SPHERE_RADIUS             = 50D;

   /** The triangles' vertices for the tests with triangles */
   private static final Point[]  VERTICES                  =
      {
        // the shared left-bottom:
        new Point(-110, -110, -150),
        // the shared right-top:
        new Point(95, 100, -150),
        // the right-bottom
        new Point(110, -110, -150),
        // the left-top
        new Point(-75, 78, 100)
      };
   /** Position of the light in tests with sphere */
   private static final Point    SPHERE_LIGHT_POSITION     = new Point(-50, -50, 25);
   /** Light direction (directional and spot) in tests with sphere */
   private static final Vector   SPHERE_LIGHT_DIRECTION    = new Vector(1, 1, -0.5);
   /** Position of the light in tests with triangles */
   private static final Point    TRIANGLES_LIGHT_POSITION  = new Point(30, 10, -100);
   /** Light direction (directional and spot) in tests with triangles */
   private static final Vector   TRIANGLES_LIGHT_DIRECTION = new Vector(-2, -2, -2);

   /** The sphere in appropriate tests */
   private static final Geometry SPHERE                    = new Sphere(SPHERE_CENTER, SPHERE_RADIUS)
      .setEmission(SPHERE_COLOR).setMaterial(new Material().setKd(KD).setKs(KS).setShininess(SHININESS));
   /** The first triangle in appropriate tests */
   private static final Geometry TRIANGLE1                 = new Triangle(VERTICES[0], VERTICES[1], VERTICES[2])
      .setMaterial(MATERIAL);
   /** The first triangle in appropriate tests */
   private static final Geometry TRIANGLE2                 = new Triangle(VERTICES[0], VERTICES[1], VERTICES[3])
      .setMaterial(MATERIAL);

   /** Produce a picture of a sphere lighted by a directional light */
   @Test
   void testSphereDirectional() {
      _scene1.geometries.add(SPHERE);
      _scene1.lights.add(new DirectionalLight(SPHERE_LIGHT_COLOR, SPHERE_LIGHT_DIRECTION));

      _camera1 //
         .setResolution(RESOLUTION, RESOLUTION) //
         .build() //
         .renderImage() //
         .writeToImage("lightSphereDirectional");
   }

   /** Produce a picture of a sphere lighted by a point light */
   @Test
   @SuppressWarnings("java:S109")
   void testSpherePoint() {
      _scene1.geometries.add(SPHERE);
      _scene1.lights.add(new PointLight(SPHERE_LIGHT_COLOR, SPHERE_LIGHT_POSITION) //
         .setKl(0.001).setKq(0.0002));

      _camera1 //
         .setResolution(500, 500) //
         .build() //
         .renderImage() //
         .writeToImage("lightSpherePoint");
   }

   /** Produce a picture of a sphere lighted by a spotlight */
   @Test
   @SuppressWarnings("java:S109")
   void testSphereSpot() {
      _scene1.geometries.add(SPHERE);
      _scene1.lights.add(new SpotLight(SPHERE_LIGHT_COLOR, SPHERE_LIGHT_POSITION, SPHERE_LIGHT_DIRECTION) //
         .setKl(0.001).setKq(0.0001));

      _camera1 //
         .setResolution(RESOLUTION, RESOLUTION) //
         .build() //
         .renderImage() //
         .writeToImage("lightSphereSpot");
   }

   /** Produce a picture of two triangles lighted by a directional light */
   @Test
   void testTrianglesDirectional() {
      _scene2.geometries.add(TRIANGLE1, TRIANGLE2);
      _scene2.lights.add(new DirectionalLight(TRIANGLES_LIGHT_COLOR, TRIANGLES_LIGHT_DIRECTION));

      _camera2.setResolution(RESOLUTION, RESOLUTION) //
         .build() //
         .renderImage() //
         .writeToImage("lightTrianglesDirectional");
   }

   /** Produce a picture of two triangles lighted by a point light */
   @Test
   @SuppressWarnings("java:S109")
   void testTrianglesPoint() {
      _scene2.geometries.add(TRIANGLE1, TRIANGLE2);
      _scene2.lights.add(new PointLight(TRIANGLES_LIGHT_COLOR, TRIANGLES_LIGHT_POSITION) //
         .setKl(0.001).setKq(0.0002));

      _camera2.setResolution(500, 500) //
         .build() //
         .renderImage() //
         .writeToImage("lightTrianglesPoint");
   }

   /** Produce a picture of two triangles lighted by a spotlight */
   @Test
   @SuppressWarnings("java:S109")
   void testTrianglesSpot() {
      _scene2.geometries.add(TRIANGLE1, TRIANGLE2);
      _scene2.lights.add(new SpotLight(TRIANGLES_LIGHT_COLOR, TRIANGLES_LIGHT_POSITION, TRIANGLES_LIGHT_DIRECTION) //
         .setKl(0.001).setKq(0.0001));

      _camera2.setResolution(500, 500) //
         .build() //
         .renderImage() //
         .writeToImage("lightTrianglesSpot");
   }

   /** Produce a picture of a sphere lighted by a narrow spotlight */
   @Test
   @SuppressWarnings("java:S109")
   void testSphereSpotSharp() {
      _scene1.geometries.add(SPHERE);
      _scene1.lights
         .add(new SpotLight(SPHERE_LIGHT_COLOR, SPHERE_LIGHT_POSITION, new Vector(1, 1, -0.5)) //
            .setKl(0.001).setKq(0.00004).setNarrowBeam(10));

      _camera1.setResolution(500, 500) //
         .build() //
         .renderImage() //
         .writeToImage("lightSphereSpotSharp");
   }

   /** Produce a picture of two triangles lighted by a narrow spotlight */
   @Test
   @SuppressWarnings("java:S109")
   void testTrianglesSpotSharp() {
      _scene2.geometries.add(TRIANGLE1, TRIANGLE2);
      _scene2.lights.add(new SpotLight(TRIANGLES_LIGHT_COLOR, TRIANGLES_LIGHT_POSITION, TRIANGLES_LIGHT_DIRECTION) //
         .setKl(0.001).setKq(0.00004).setNarrowBeam(10));

      _camera2.setResolution(500, 500) //
         .build() //
         .renderImage() //
         .writeToImage("lightTrianglesSpotSharp");
   }

    /**
     * Produce a picture of a sphere lighted by multiple light sources (Directional, Point, Spot)
     */
    @Test
    @SuppressWarnings("java:S109")
    void testSphereMultipleLights() {
        _scene1.geometries.add(SPHERE);
        _scene1.lights.addAll(List.of(
                new DirectionalLight(new Color(0, 100, 100), new Vector(1, 1, -1)),
                new PointLight(new Color(500, 0, 0), new Point(50, 50, 50))
                        .setKl(0.0001).setKq(0.00005),
                new SpotLight(new Color(0, 500, 0), new Point(-50, -50, 50), new Vector(1, 1, -2))
                        .setKl(0.0001).setKq(0.00005)
        ));

        _camera1.setResolution(500, 500)
                .build()
                .renderImage()
                .writeToImage("lightSphereMultiple");
    }

    /**
     * Produce a picture of two triangles lighted by multiple light sources (Directional, Point, Spot)
     */
    @Test
    @SuppressWarnings("java:S109")
    void testTrianglesMultipleLights() {
        _scene2.geometries.add(TRIANGLE1, TRIANGLE2);
        _scene2.lights.addAll(List.of(
                new DirectionalLight(new Color(100, 100, 0), new Vector(0, -1, -1)),
                new PointLight(new Color(300, 0, 300), new Point(40, -70, -100))
                        .setKl(0.0005).setKq(0.00015),
                new SpotLight(new Color(0, 300, 300), new Point(-20, 50, -50), new Vector(2, -2, -3))
                        .setKl(0.001).setKq(0.0001)
        ));

        _camera2.setResolution(500, 500)
                .build()
                .renderImage()
                .writeToImage("lightTrianglesMultiple");
    }

    /**
     * Produce a picture of a sphere lighted by a spotlight using XML
     */
    @Test
    @SuppressWarnings("java:S109")
    void testSphereSpotXml() {
        try {
            Scene scene = SceneXMLParser.parse("sphere_spot_test.xml");

            Camera.getBuilder()
                    .setRayTracer(scene, RayTracerType.SIMPLE)
                    .setLocation(new Point(0, 0, 1000))
                    .setDirection(Point.ZERO, Vector.AXIS_Y)
                    .setVpSize(150, 150).setVpDistance(1000)
                    .setResolution(500, 500)
                    .build()
                    .renderImage()
                    .writeToImage("lightSphereSpotXml");
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Failed loading XML scene: " + e.getMessage());
        }
    }

    /**
     * Custom cool scene demonstrating multiple shapes, colors, emissions, and light sources!
     */
    @Test
    @SuppressWarnings("java:S109")
    void testCustomCoolScene() {
        Scene customScene = new Scene("Cool Custom Scene")
                .setAmbientLight(new AmbientLight(new Color(10, 10, 10))); // חושך יחסי

        Material shinyMat = new Material().setKd(0.5).setKs(0.5).setShininess(100);
        Material matteMat = new Material().setKd(0.8).setKs(0.2).setShininess(20);

        customScene.geometries.add(
                new Sphere(new Point(0, 0, -100), 40)
                        .setEmission(new Color(50, 0, 20)).setMaterial(shinyMat),
                new Sphere(new Point(50, 30, -50), 15)
                        .setEmission(new Color(0, 50, 0)).setMaterial(shinyMat),
                new Triangle(new Point(-200, -50, -200), new Point(200, -50, -200), new Point(0, -50, 50))
                        .setEmission(new Color(10, 10, 10)).setMaterial(matteMat),
                new Triangle(new Point(-200, 200, -250), new Point(200, 200, -250), new Point(0, -100, -250))
                        .setEmission(new Color(0, 0, 20)).setMaterial(matteMat)
        );

        customScene.lights.addAll(List.of(
                new DirectionalLight(new Color(40, 40, 40), new Vector(1, -0.5, -1)),
                new PointLight(new Color(0, 0, 500), new Point(-40, 40, -50))
                        .setKl(0.0001).setKq(0.00005),
                new SpotLight(new Color(500, 500, 0), new Point(100, 100, 50), new Vector(-1, -1, -2))
                        .setKl(0.0001).setKq(0.00005)
        ));

        Camera.getBuilder()
                .setRayTracer(customScene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(new Point(0,0,-1).subtract(new Point(0, 0, 1000)), new Vector(0, 1, 0))
                .setVpSize(200, 200).setVpDistance(1000)
                .setResolution(5000, 5000)
                .build()
                .renderImage()
                .writeToImage("customCoolScene");
    }

    /**
     * Produce an epic, procedurally generated galaxy scene V6.
     * Features: Zoomed-out camera, vibrant colors, white central halo, and background star clusters.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testMilkyWayEpicGalaxyV6() {
        Scene scene = new Scene("Milky Way Galaxy V6")
                .setAmbientLight(new AmbientLight(new Color(2, 2, 6))); // רקע חשוך יותר

        Material starMat = new Material().setKd(0.2).setKs(0.8).setShininess(400);
        Material dustMat = new Material().setKd(0.8).setKs(0.2).setShininess(10);

        java.util.Random rnd = new java.util.Random(42);

        // 1. זוויות להטיה
        double tiltX = Math.toRadians(65);
        double tiltZ = Math.toRadians(45);

        double cosX = Math.cos(tiltX);
        double sinX = Math.sin(tiltX);
        double cosZ = Math.cos(tiltZ);
        double sinZ = Math.sin(tiltZ);

        // --- 1. השמש המרכזית, הילה וענן הליבה ---
        Point centerPoint = applyRotation(0, 0, 0, cosX, sinX, cosZ, sinZ, -900);

        // השמש הגדולה
        scene.geometries.add(new Sphere(centerPoint, 38D)
                .setEmission(new Color(255, 245, 220)) // לבן בוהק מאוד עם נטייה לזהב
                .setMaterial(starMat));

        scene.lights.add(new PointLight(new Color(1000, 800, 600), centerPoint)
                .setKl(0.00005).setKq(0.000005));

        // הילת כוכבים לבנים/תכלת קטנים סביב הליבה (באותה זווית של הגלקסיה)
        for (int i = 0; i < 400; i++) {
            double r = 40 + rnd.nextDouble() * 30; // קרוב יחסית לשמש
            double theta = rnd.nextDouble() * 2 * Math.PI;
            double phi = rnd.nextDouble() * Math.PI;

            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = (rnd.nextDouble() - 0.5) * 15; // דיסקה שטוחה להילה
            double z = r * Math.cos(phi);

            Point p = applyRotation(x, y, z, cosX, sinX, cosZ, sinZ, -900);

            scene.geometries.add(new Sphere(p, 0.8 + rnd.nextDouble() * 2)
                    .setEmission(new Color(230 + rnd.nextInt(25), 240 + rnd.nextInt(15), 255)) // לבן-תכלת חזק
                    .setMaterial(starMat));
        }

        // ענן חלקיקים חמים וסמיכים סביב הליבה
        for (int i = 0; i < 300; i++) {
            double r = 40 + rnd.nextDouble() * 60;
            double theta = rnd.nextDouble() * 2 * Math.PI;
            double phi = rnd.nextDouble() * Math.PI;

            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = (rnd.nextDouble() - 0.5) * 40;
            double z = r * Math.cos(phi);

            Point p = applyRotation(x, y, z, cosX, sinX, cosZ, sinZ, -900);
            Color coreColor = new Color(255, 60 + rnd.nextInt(60), 10); // כתום-אדום עז

            scene.geometries.add(new Sphere(p, 1.5 + rnd.nextDouble() * 3)
                    .setEmission(coreColor)
                    .setMaterial(starMat));
        }

        int numArms = 4;

        // --- 2. זרועות ספירלה ראשיות ומשניות ---
        int objectsPerMainArm = 1200;
        double maxMainRadius = 800;

        for (int arm = 0; arm < numArms; arm++) {
            double armOffset = (2 * Math.PI / numArms) * arm;
            for (int i = 0; i < objectsPerMainArm; i++) {
                double distance = 60 + (maxMainRadius - 60) * Math.pow((i / (double) objectsPerMainArm), 0.85);
                double angle = armOffset - (distance / 160.0) + (rnd.nextDouble() - 0.5) * 0.4;
                generateGalaxyObjectV6(scene, distance, angle, cosX, sinX, cosZ, sinZ, rnd, starMat, dustMat);
            }
        }

        int objectsPerMiniArm = 600;
        double maxMiniRadius = 450;

        for (int arm = 0; arm < numArms; arm++) {
            double armOffset = (2 * Math.PI / numArms) * arm + (Math.PI / numArms);
            for (int i = 0; i < objectsPerMiniArm; i++) {
                double distance = 70 + (maxMiniRadius - 70) * Math.pow((i / (double) objectsPerMiniArm), 0.9);
                double angle = armOffset - (distance / 130.0) + (rnd.nextDouble() - 0.5) * 0.6;
                generateGalaxyObjectV6(scene, distance, angle, cosX, sinX, cosZ, sinZ, rnd, starMat, dustMat);
            }
        }

        // --- 3. רקע עמוק וצבירי כוכבים ---
        // פיזור רגיל של כוכבי רקע (כחולים ולבנים)
        for (int i = 0; i < 1500; i++) {
            double x = (rnd.nextDouble() - 0.5) * 9000;
            double y = (rnd.nextDouble() - 0.5) * 9000;
            double z = -1500 - rnd.nextDouble() * 5000;

            int shade = 180 + rnd.nextInt(75);
            Color starColor = rnd.nextBoolean() ? new Color(shade, shade, 255) : new Color(255, 255, 255);

            scene.geometries.add(new Sphere(new Point(x, y, z), 2 + rnd.nextDouble() * 5)
                    .setEmission(starColor));
        }

        // יצירת צבירי כוכבים (Star Clusters) מרוחקים למראה מציאותי ועמוק
        for (int c = 0; c < 6; c++) { // 6 צבירים שונים בחלל
            double clusterX = (rnd.nextDouble() - 0.5) * 6000;
            double clusterY = (rnd.nextDouble() - 0.5) * 6000;
            double clusterZ = -2000 - rnd.nextDouble() * 3000;

            for(int i = 0; i < 70; i++) {
                double cx = clusterX + rnd.nextGaussian() * 150; // פיזור נורמלי סביב המרכז
                double cy = clusterY + rnd.nextGaussian() * 150;
                double cz = clusterZ + rnd.nextGaussian() * 150;

                scene.geometries.add(new Sphere(new Point(cx, cy, cz), 3 + rnd.nextDouble() * 6)
                        .setEmission(new Color(200 + rnd.nextInt(55), 220 + rnd.nextInt(35), 255)));
            }
        }

        scene.lights.add(new SpotLight(new Color(80, 80, 150), new Point(0, 400, 300), new Vector(0, -1, -3))
                .setKl(0.0001).setKq(0.00001));

        // --- רינדור ---
        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                // הגדלנו את שדה הראייה ל-350 כדי "להתרחק" ולראות שוליים שחורים
                .setVpSize(1000, 1000).setVpDistance(1000)
                .setResolution(5000, 5000) // !!! שנה כאן ל-5000 אחרי הטסט !!!
                .build()
                .renderImage()
                .writeToImage("milkyWayEpicGalaxyV6");
    }

    /**
     * פונקציית עזר ליצירת חלקיקי אבק כוכבים בצבעים עזים במיוחד (Vibrant)
     */
    private void generateGalaxyObjectV6(Scene scene, double distance, double angle,
                                        double cosX, double sinX, double cosZ, double sinZ,
                                        java.util.Random rnd, Material starMat, Material dustMat) {

        double stretchMajor = 1.7;
        double squeezeMinor = 0.5;

        double flatX = distance * stretchMajor * Math.cos(angle);
        double flatY = (rnd.nextDouble() - 0.5) * 35;
        double flatZ = distance * squeezeMinor * Math.sin(angle);

        Color emission;
        if (distance < 250) {
            emission = new Color(255, 20 + rnd.nextInt(50), 10); // אדום ניאון וכתום עז
        } else if (distance < 550) {
            emission = new Color(200 + rnd.nextInt(55), 10 + rnd.nextInt(20), 220 + rnd.nextInt(35)); // סגול ומגנטה צועק
        } else {
            emission = new Color(10 + rnd.nextInt(30), 20 + rnd.nextInt(40), 255); // כחול עמוק ונקי
            if (rnd.nextDouble() > 0.85) {
                emission = new Color(255, 30 + rnd.nextInt(40), 20); // קצוות באדום עז
            }
        }

        Point p = applyRotation(flatX, flatY, flatZ, cosX, sinX, cosZ, sinZ, -900);

        scene.geometries.add(new Sphere(p, 0.8 + rnd.nextDouble() * 3.5D)
                .setEmission(emission)
                .setMaterial(rnd.nextBoolean() ? starMat : dustMat));

        if (rnd.nextDouble() > 0.96) {
            scene.lights.add(new PointLight(emission.scale(1.5), p)
                    .setKl(0.001).setKq(0.0001));
        }
    }

    private Point applyRotation(double x, double y, double z, double cosX, double sinX, double cosZ, double sinZ, double zOffset) {
        double y1 = y * cosX - z * sinX;
        double z1 = y * sinX + z * cosX;
        double x2 = x * cosZ - y1 * sinZ;
        double y2 = x * sinZ + y1 * cosZ;
        return new Point(x2, y2, z1 + zOffset);
    }
}
