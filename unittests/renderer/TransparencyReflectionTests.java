package renderer;

import static java.awt.Color.BLUE;
import static java.awt.Color.RED;

import org.junit.jupiter.api.Test;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.SpotLight;
import primitives.*;
import scene.Scene;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Tube;
import geometries.impl.Cylinder;
import lighting.DirectionalLight;
import lighting.PointLight;
import scene.SceneXMLParser;

import java.util.Random;

/**
 * Tests for reflection and transparency functionality, test for partial
 * shadows
 * (with transparency)
 * @author Dan Zilberstein
 */
class TransparencyReflectionTests {
   /** Default constructor to satisfy JavaDoc generator */
   TransparencyReflectionTests() { /* to satisfy JavaDoc generator */ }

   /** Scene for the tests */
   private final Scene          _scene         = new Scene("Test scene");
   /** Camera builder for the tests with triangles */
   private final Camera.Builder _cameraBuilder = Camera.getBuilder()     //
      .setRayTracer(_scene, RayTracerType.SIMPLE);

   /** Produce a picture of a sphere lighted by a spot light */
   @Test
   @SuppressWarnings("java:S109")
   void testTwoSpheres() {
      _scene.geometries.add( //
                            new Sphere(new Point(0, 0, -50), 50D).setEmission(new Color(BLUE)) //
                               .setMaterial(new Material().setKd(0.4).setKs(0.3).setShininess(100).setKt(0.3)), //
                            new Sphere(new Point(0, 0, -50), 25D).setEmission(new Color(RED)) //
                               .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(100))); //
      _scene.lights.add( //
                        new SpotLight(new Color(1000, 600, 0), new Point(-100, -100, 500), new Vector(-1, -1, -2)) //
                           .setKl(0.0004).setKq(0.0000006));

      _cameraBuilder
         .setLocation(new Point(0, 0, 1000)) //
         .setDirection(Point.ZERO, Vector.AXIS_Y) //
         .setVpDistance(1000).setVpSize(150, 150) //
         .setResolution(500, 500) //
         .build() //
         .renderImage() //
         .writeToImage("refractionTwoSpheres");
   }

   /** Produce a picture of a sphere lighted by a spot light */
   @Test
   @SuppressWarnings("java:S109")
   void testTwoSpheresOnMirrors() {
      _scene.geometries.add( //
                            new Sphere(new Point(-950, -900, -1000), 400D).setEmission(new Color(0, 50, 100)) //
                               .setMaterial(new Material().setKd(0.25).setKs(0.25).setShininess(20) //
                                  .setKt(new Double3(0.5, 0, 0))), //
                            new Sphere(new Point(-950, -900, -1000), 200D).setEmission(new Color(100, 50, 20)) //
                               .setMaterial(new Material().setKd(0.25).setKs(0.25).setShininess(20)), //
                            new Triangle(new Point(1500, -1500, -1500), new Point(-1500, 1500, -1500), //
                                         new Point(670, 670, 3000)) //
                               .setEmission(new Color(20, 20, 20)) //
                               .setMaterial(new Material().setKr(1)), //
                            new Triangle(new Point(1500, -1500, -1500), new Point(-1500, 1500, -1500), //
                                         new Point(-1500, -1500, -2000)) //
                               .setEmission(new Color(20, 20, 20)) //
                               .setMaterial(new Material().setKr(new Double3(0.5, 0, 0.4))));
      _scene.setAmbientLight(new AmbientLight(new Color(26, 26, 26)));
      _scene.lights.add(new SpotLight(new Color(1020, 400, 400), new Point(-750, -750, -150), new Vector(-1, -1, -4)) //
         .setKl(0.00001).setKq(0.000005));

      _cameraBuilder
         .setLocation(new Point(0, 0, 10000)) //
         .setDirection(Point.ZERO, Vector.AXIS_Y) //
         .setVpDistance(10000).setVpSize(2500, 2500) //
         .setResolution(500, 500) //
         .build() //
         .renderImage() //
         .writeToImage("reflectionTwoSpheresMirrored");
   }

   /**
    * Produce a picture of a two triangles lighted by a spot light with a
    * partially
    * transparent Sphere producing partial shadow
    */
   @Test
   @SuppressWarnings("java:S109")
   void testTrianglesTransparentSphere() {
      _scene.geometries.add(
                            new Triangle(new Point(-150, -150, -115), new Point(150, -150, -135),
                                         new Point(75, 75, -150))
                               .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(60)),
                            new Triangle(new Point(-150, -150, -115), new Point(-70, 70, -140), new Point(75, 75, -150))
                               .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(60)),
                            new Sphere(new Point(60, 50, -50), 30D).setEmission(new Color(BLUE))
                               .setMaterial(new Material().setKd(0.2).setKs(0.2).setShininess(30).setKt(0.6)));
      _scene.setAmbientLight(new AmbientLight(new Color(38, 38, 38)));
      _scene.lights.add(
                        new SpotLight(new Color(700, 400, 400), new Point(60, 50, 0), new Vector(0, 0, -1))
                           .setKl(4E-5).setKq(2E-7));

      _cameraBuilder
         .setLocation(new Point(0, 0, 1000)) //
         .setDirection(Point.ZERO, Vector.AXIS_Y) //
         .setVpDistance(1000).setVpSize(200, 200) //
         .setResolution(600, 600) //
         .build() //
         .renderImage() //
         .writeToImage("refractionShadow");
   }

    /**
     * Bonus 1: The Shattered Dimension Vortex
     * A completely crazy, computationally heavy, cinematic scene.
     * Features 500+ objects, infinite mirrors, massive refraction,
     * 25+ light sources, and strictly utilizes ALL 6 geometries.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testBonus1ShatteredDimension() {
        Scene scene = new Scene("Shattered Dimension")
                .setAmbientLight(new AmbientLight(new Color(2, 2, 2))) // חושך כמעט מוחלט, נותן לאורות לעבוד
                .setBackground(Color.BLACK);

        // --- Materials ---
        Material mirrorMat = new Material().setKd(0.0).setKs(0.8).setShininess(400).setKr(0.85);
        Material pureGlass = new Material().setKd(0.0).setKs(0.9).setShininess(500).setKt(0.95).setKr(0.05);
        Material coloredGlass = new Material().setKd(0.1).setKs(0.8).setShininess(200).setKt(0.8).setKr(0.2);
        Material metallicMat = new Material().setKd(0.4).setKs(0.8).setShininess(300).setKr(0.4);
        Material matteMat = new Material().setKd(0.8).setKs(0.2).setShininess(10);

        // 1. Plane: Reflective Infinite Floor
        scene.geometries.add(new Plane(new Point(0, -80, 0), new Vector(0, 1, 0))
                .setEmission(new Color(5, 5, 10)).setMaterial(mirrorMat));

        // 2. Polygons: Massive Mirror Walls (Back, Left, Right) to create an infinite reflection box
        scene.geometries.add(
                // Back Wall (Planar on Z)
                new Polygon(new Point(-300, -80, -400), new Point(300, -80, -400), new Point(300, 300, -400), new Point(-300, 300, -400))
                        .setEmission(new Color(2, 2, 2)).setMaterial(mirrorMat),
                // Left Wall (Planar on X)
                new Polygon(new Point(-300, -80, 100), new Point(-300, -80, -400), new Point(-300, 300, -400), new Point(-300, 300, 100))
                        .setEmission(new Color(2, 2, 2)).setMaterial(mirrorMat),
                // Right Wall (Planar on X)
                new Polygon(new Point(300, -80, -400), new Point(300, -80, 100), new Point(300, 300, 100), new Point(300, 300, -400))
                        .setEmission(new Color(2, 2, 2)).setMaterial(mirrorMat)
        );

        // 3. Tubes: Neon chaotic laser vines weaving through the scene
        scene.geometries.add(
                new Tube(2.5, new Ray(new Point(-200, -50, -100), new Vector(1, 0.4, -0.2))).setEmission(new Color(0, 255, 200)).setMaterial(metallicMat),
                new Tube(1.5, new Ray(new Point(200, -20, -50), new Vector(-1, 0.6, -0.5))).setEmission(new Color(255, 0, 150)).setMaterial(metallicMat),
                new Tube(3.0, new Ray(new Point(0, -80, -250), new Vector(0.2, 1, 0.5))).setEmission(new Color(150, 0, 255)).setMaterial(metallicMat)
        );

        // 4. Sphere: Giant Pure Glass Core
        scene.geometries.add(new Sphere(new Point(0, 30, -150), 45)
                .setEmission(Color.BLACK).setMaterial(pureGlass));

        Random rnd = new Random(111); // Seed קבוע כדי שהתוצאה תהיה יפה ועקבית

        // 5. Procedural Vortex: 500 Objects (Spheres, Triangles, Cylinders)
        for (int i = 0; i < 100; i++) {
            double height = -70 + rnd.nextDouble() * 250;
            double radius = 55 + (height + 70) * 0.4 + rnd.nextDouble() * 40; // צורת טורנדו
            double angle = height * 0.15 + rnd.nextDouble() * Math.PI * 2;

            double x = Math.cos(angle) * radius;
            double z = -150 + Math.sin(angle) * radius;
            Point p = new Point(x, height, z);

            // צבעים מטורפים: פסטל, סייברפאנק, אש
            Color col = new Color(50 + rnd.nextInt(205), 20 + rnd.nextInt(150), 100 + rnd.nextInt(155));

            // חלוקת חומרים
            int matType = rnd.nextInt(4);
            Material mat = (matType == 0) ? coloredGlass : (matType == 1 ? metallicMat : matteMat);

            // בחירת גיאומטריה באקראי
            int geoType = rnd.nextInt(3);
            if (geoType == 0) {
                // Spheres
                scene.geometries.add(new Sphere(p, 2 + rnd.nextDouble() * 6).setEmission(col).setMaterial(mat));
            } else if (geoType == 1) {
                // Triangles (Glass/Metal Shards)
                double s = 6 + rnd.nextDouble() * 10;
                Point p2 = new Point(x + s, height + rnd.nextDouble()*s, z + rnd.nextDouble()*s);
                Point p3 = new Point(x - s, height + rnd.nextDouble()*s, z - rnd.nextDouble()*s);
                scene.geometries.add(new Triangle(p, p2, p3).setEmission(col).setMaterial(mat));
            } else {
                // Cylinders (Floating pillars/debris)
                Vector dir = new Vector(rnd.nextDouble() + 0.1, rnd.nextDouble() + 0.1, rnd.nextDouble() + 0.1).normalize();
                scene.geometries.add(new Cylinder(1 + rnd.nextDouble() * 2.5, new Ray(p, dir), 5 + rnd.nextDouble() * 15)
                        .setEmission(col).setMaterial(mat));
            }

            // פיזור 25 מקורות אור בתוך המערבולת שיאירו את כל הבלגן הזה מבפנים!
            if (i % 20 == 0) {
                scene.lights.add(new PointLight(col.scale(1.5), p).setKl(0.005).setKq(0.0001));
            }
        }

        // --- Main Cinematic Lights ---
        scene.lights.add(new SpotLight(new Color(1000, 1000, 1000), new Point(0, 200, 50), new Vector(0, -1, -1))
                .setKl(0.0001).setKq(0.00001));
        scene.lights.add(new DirectionalLight(new Color(15, 15, 30), new Vector(1, -1, -1))); // אור מילוי כחלחל

        // --- Render ---
        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 10, 180)) // מבט מלמטה כלפי מעלה אל תוך המערבולת
                .setDirection(new Point(0, 30, -150).subtract(new Point(0, 10, 180)), new Vector(0, 1, 0))
                .setVpSize(200, 200).setVpDistance(150)
                .setResolution(600, 600) // תתכונן למעבד רותח
                .setAntiAliasing(9, new Jittered())
                .setMultithreading(-2)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage("bonus1_shattered_dimension_v2");
    }

    @Test
    @SuppressWarnings("java:S109")
    void testGlassCubeStudioSet() {
        Scene scene = new Scene("Glass Cube Studio Set")
                .setBackground(new Color(110, 115, 140))
                .setAmbientLight(new AmbientLight(new Color(25, 25, 30)));

        // --- Metallic Spheres Materials (From v6 - Perfect Chrome Look) ---
        Material redMetal    = new Material().setKd(new Double3(0.6, 0.1, 0.1)).setKa(new Double3(0.6, 0.1, 0.1)).setKs(0.8).setShininess(300).setKr(0.25);
        Material greenMetal  = new Material().setKd(new Double3(0.1, 0.6, 0.1)).setKa(new Double3(0.1, 0.6, 0.1)).setKs(0.8).setShininess(300).setKr(0.25);
        Material blueMetal   = new Material().setKd(new Double3(0.1, 0.1, 0.6)).setKa(new Double3(0.1, 0.1, 0.6)).setKs(0.8).setShininess(300).setKr(0.25);
        Material yellowMetal = new Material().setKd(new Double3(0.6, 0.6, 0.1)).setKa(new Double3(0.6, 0.6, 0.1)).setKs(0.8).setShininess(300).setKr(0.25);

        // --- Glass Materials (Moderate transparency and reflection) ---
        Material clearGlass = new Material().setKd(0.0).setKs(0.8).setShininess(120).setKt(new Double3(0.85, 0.85, 0.9)).setKr(0.15);
        Material redGlass   = new Material().setKd(0.0).setKs(0.8).setShininess(120).setKt(new Double3(0.85, 0.4, 0.4)).setKr(0.15);
        Material goldGlass  = new Material().setKd(0.0).setKs(0.8).setShininess(120).setKt(new Double3(0.85, 0.8, 0.3)).setKr(0.15);

        // --- Room Materials ---
        Material floorMat     = new Material().setKd(0.5).setKs(0.4).setShininess(80).setKr(0.3);
        Material matteWallMat = new Material().setKd(0.8).setKs(0.0).setShininess(0).setKr(0.0);

        // --- Proportions ---
        int s = 40;
        double sphereRadius = 19;
        double cylRadius = 11;

        Point tfl = new Point(-s, s, s);
        Point tfr = new Point(s, s, s);
        Point tbl = new Point(-s, s, -s);
        Point tbr = new Point(s, s, -s);

        Point bfl = new Point(-s, -s, s);
        Point bfr = new Point(s, -s, s);
        Point bbl = new Point(-s, -s, -s);
        Point bbr = new Point(s, -s, -s);

        // 1. Metallic Spheres (Emission is BLACK)
        scene.geometries.add(
                new Sphere(tfl, sphereRadius).setEmission(Color.BLACK).setMaterial(redMetal),
                new Sphere(tfr, sphereRadius).setEmission(Color.BLACK).setMaterial(greenMetal),
                new Sphere(tbl, sphereRadius).setEmission(Color.BLACK).setMaterial(blueMetal),
                new Sphere(tbr, sphereRadius).setEmission(Color.BLACK).setMaterial(yellowMetal),

                new Sphere(bfl, sphereRadius).setEmission(Color.BLACK).setMaterial(greenMetal),
                new Sphere(bfr, sphereRadius).setEmission(Color.BLACK).setMaterial(blueMetal),
                new Sphere(bbl, sphereRadius).setEmission(Color.BLACK).setMaterial(yellowMetal),
                new Sphere(bbr, sphereRadius).setEmission(Color.BLACK).setMaterial(redMetal)
        );

        // 2. Tinted Glass Edges (Cylinders with very subtle emission from v7)
        Color goldTint  = new Color(30, 25, 5);
        Color redTint   = new Color(30, 5, 5);
        Color clearTint = new Color(15, 20, 25);

        createGlassEdge(scene, tfl, bfl, sphereRadius, cylRadius, goldTint, goldGlass);

        createGlassEdge(scene, tfl, tfr, sphereRadius, cylRadius, redTint, redGlass);
        createGlassEdge(scene, tbl, tbr, sphereRadius, cylRadius, redTint, redGlass);
        createGlassEdge(scene, bbl, bbr, sphereRadius, cylRadius, redTint, redGlass);

        createGlassEdge(scene, tfl, tbl, sphereRadius, cylRadius, clearTint, clearGlass);
        createGlassEdge(scene, tfr, tbr, sphereRadius, cylRadius, clearTint, clearGlass);
        createGlassEdge(scene, tfr, bfr, sphereRadius, cylRadius, clearTint, clearGlass);
        createGlassEdge(scene, tbl, bbl, sphereRadius, cylRadius, clearTint, clearGlass);
        createGlassEdge(scene, tbr, bbr, sphereRadius, cylRadius, clearTint, clearGlass);
        createGlassEdge(scene, bfl, bfr, sphereRadius, cylRadius, clearTint, clearGlass);
        createGlassEdge(scene, bfl, bbl, sphereRadius, cylRadius, clearTint, clearGlass);
        createGlassEdge(scene, bfr, bbr, sphereRadius, cylRadius, clearTint, clearGlass);

        // 3. The Room (Polygons instead of infinite Planes!)
        double floorY = -s - sphereRadius + 1;

        // Floor Polygon
        scene.geometries.add(new Polygon(
                new Point(-200, floorY, 200),
                new Point(200, floorY, 200),
                new Point(200, floorY, -200),
                new Point(-200, floorY, -200))
                .setEmission(new Color(45, 55, 70)).setMaterial(floorMat));

        // Left Wall Polygon (X = -120)
        scene.geometries.add(new Polygon(
                new Point(-120, floorY, 200),
                new Point(-120, floorY, -200),
                new Point(-120, 300, -200),
                new Point(-120, 300, 200))
                .setEmission(new Color(90, 45, 110)).setMaterial(matteWallMat));

        // Back Wall Polygon (Z = -120)
        scene.geometries.add(new Polygon(
                new Point(-200, floorY, -120),
                new Point(200, floorY, -120),
                new Point(200, 300, -120),
                new Point(-200, 300, -120))
                .setEmission(new Color(45, 65, 115)).setMaterial(matteWallMat));

        // --- Lights (Strictly from v6 to avoid overexposure) ---
        scene.lights.add(new PointLight(new Color(400, 400, 400), new Point(120, 180, 120))
                .setKl(0.0001).setKq(0.00001));

        scene.lights.add(new DirectionalLight(new Color(60, 60, 60), new Vector(1, -0.5, -1)));

        //scene.lights.add(new SpotLight(new Color(250, 250, 250), new Point(-100, 150, -100), new Vector(1, -1, 1))
        //     .setKl(0.0001).setKq(0.00001));

        // --- Camera & Render ---
        Point cameraLoc = new Point(120, 75, 180);
        Point target = new Point(0, -5, 0);

        Vector vTo = target.subtract(cameraLoc).normalize();
        Vector vRight = vTo.crossProduct(new Vector(0, 1, 0)).normalize();
        Vector vUp = vRight.crossProduct(vTo).normalize();

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(cameraLoc)
                .setDirection(vTo, vUp)
                .setVpSize(150, 150).setVpDistance(140)
                .setResolution(1000, 1000)
                .setAntiAliasing(9, new Jittered())
                .setMultithreading(-2)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage("classic_glass_cube_v10");
    }

    /**
     * Helper method to create a glass cylinder edge.
     * Uses minimal emission tint and deep penetration.
     */
    private void createGlassEdge(Scene scene, Point p1, Point p2, double sphereRadius, double cylRadius, Color tint, Material glassMat) {
        Vector dir = p2.subtract(p1).normalize();
        double totalDistance = p1.distance(p2);

        double penetration = 8.0;
        Point start = p1.add(dir.scale(sphereRadius - penetration));
        double height = totalDistance - 2 * (sphereRadius - penetration);

        scene.geometries.add(new geometries.impl.Cylinder(cylRadius, new Ray(start, dir), height)
                .setEmission(tint)
                .setMaterial(glassMat));
    }

    /**
     * Produce a picture of a sphere lighted by a spot light,
     * loaded entirely from an XML file to test parser compatibility
     * with transparency (kT) and reflection (kR).
     */
    @Test
    @SuppressWarnings("java:S109")
    void testTwoSpheresXML() {
        try {
            Scene scene = SceneXMLParser.parse("two_spheres_transparency.xml");

            Camera.getBuilder()
                    .setRayTracer(scene, RayTracerType.SIMPLE)
                    .setLocation(new Point(0, 0, 1000))
                    .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                    .setVpDistance(1000).setVpSize(150, 150)
                    .setResolution(500, 500)
                    .build()
                    .renderImage()
                    .writeToImage("refractionTwoSpheresXML");

        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("XML parsing failed: " + e.getMessage());
        }
    }
}
