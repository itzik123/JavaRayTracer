package renderer;

import static java.awt.Color.BLUE;
import static java.awt.Color.RED;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.SceneXMLParser;

/**
 * Rendering tests of reflection and transparency, including the partial shadow cast by a
 * transparent object.
 *
 * @author Dan Zilberstein
 */
class TransparencyReflectionTest {

    /** Default constructor to satisfy JavaDoc generator */
    TransparencyReflectionTest() { /* to satisfy JavaDoc generator */ }

    /** The scene of the tests */
    private final Scene _scene = new Scene("Transparency and reflection scene");
    /** The camera of the tests */
    private final Camera.Builder _cameraBuilder = Camera.getBuilder()
            .setRayTracer(_scene, RayTracerType.SIMPLE);

    /** Produce a picture of a transparent sphere containing an opaque one */
    @Test
    void testTwoSpheres() {
        _scene.geometries.add(
                new Sphere(new Point(0, 0, -50), 50).setEmission(new Color(BLUE))
                        .setMaterial(new Material().setKd(0.4).setKs(0.3).setShininess(100).setKt(0.3)),
                new Sphere(new Point(0, 0, -50), 25).setEmission(new Color(RED))
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(100)));
        _scene.lights.add(new SpotLight(new Color(1000, 600, 0), new Point(-100, -100, 500), new Vector(-1, -1, -2))
                .setKl(0.0004).setKq(0.0000006));

        _cameraBuilder
                .setLocation(new Point(0, 0, 1000)).setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpDistance(1000).setVpSize(150, 150)
                .setResolution(500, 500)
                .build()
                .renderImage()
                .writeToImage("refractionTwoSpheres");
    }

    /** Produce a picture of two nested spheres reflected in two mirrors */
    @Test
    void testTwoSpheresOnMirrors() {
        _scene.geometries.add(
                new Sphere(new Point(-950, -900, -1000), 400).setEmission(new Color(0, 50, 100))
                        .setMaterial(new Material().setKd(0.25).setKs(0.25).setShininess(20).setKt(new Double3(0.5, 0, 0))),
                new Sphere(new Point(-950, -900, -1000), 200).setEmission(new Color(100, 50, 20))
                        .setMaterial(new Material().setKd(0.25).setKs(0.25).setShininess(20)),
                new Triangle(new Point(1500, -1500, -1500), new Point(-1500, 1500, -1500), new Point(670, 670, 3000))
                        .setEmission(new Color(20, 20, 20))
                        .setMaterial(new Material().setKr(1)),
                new Triangle(new Point(1500, -1500, -1500), new Point(-1500, 1500, -1500), new Point(-1500, -1500, -2000))
                        .setEmission(new Color(20, 20, 20))
                        .setMaterial(new Material().setKr(new Double3(0.5, 0, 0.4))));
        _scene.setAmbientLight(new AmbientLight(new Color(26, 26, 26)));
        _scene.lights.add(new SpotLight(new Color(1020, 400, 400), new Point(-750, -750, -150), new Vector(-1, -1, -4))
                .setKl(0.00001).setKq(0.000005));

        _cameraBuilder
                .setLocation(new Point(0, 0, 10000)).setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpDistance(10000).setVpSize(2500, 2500)
                .setResolution(500, 500)
                .build()
                .renderImage()
                .writeToImage("reflectionTwoSpheresMirrored");
    }

    /** Produce a picture of two triangles partially shaded by a transparent sphere */
    @Test
    void testTrianglesTransparentSphere() {
        _scene.geometries.add(
                new Triangle(new Point(-150, -150, -115), new Point(150, -150, -135), new Point(75, 75, -150))
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(60)),
                new Triangle(new Point(-150, -150, -115), new Point(-70, 70, -140), new Point(75, 75, -150))
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(60)),
                new Sphere(new Point(60, 50, -50), 30).setEmission(new Color(BLUE))
                        .setMaterial(new Material().setKd(0.2).setKs(0.2).setShininess(30).setKt(0.6)));
        _scene.setAmbientLight(new AmbientLight(new Color(38, 38, 38)));
        _scene.lights.add(new SpotLight(new Color(700, 400, 400), new Point(60, 50, 0), new Vector(0, 0, -1))
                .setKl(4E-5).setKq(2E-7));

        _cameraBuilder
                .setLocation(new Point(0, 0, 1000)).setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpDistance(1000).setVpSize(200, 200)
                .setResolution(600, 600)
                .build()
                .renderImage()
                .writeToImage("refractionShadow");
    }

    /** Produce the transparent-sphere picture from a scene loaded from an XML file */
    @Test
    void testTwoSpheresXml() {
        Camera.getBuilder()
                .setRayTracer(SceneXMLParser.parse("scenes/twoSpheresTransparency.xml"), RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000)).setDirection(new Vector(0, 0, -1), Vector.AXIS_Y)
                .setVpDistance(1000).setVpSize(150, 150)
                .setResolution(500, 500)
                .build()
                .renderImage()
                .writeToImage("refractionTwoSpheresXML");
    }
}
