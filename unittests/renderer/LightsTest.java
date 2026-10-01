package renderer;

import static java.awt.Color.BLUE;

import geometries.api.Geometry;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.SceneXMLParser;

import java.util.List;

/**
 * Rendering tests of the light sources and the Phong reflection model, on a sphere and on a pair
 * of triangles.
 *
 * @author Dan Zilberstein
 */
class LightsTest {

    /** Default constructor to satisfy JavaDoc generator */
    LightsTest() { /* to satisfy JavaDoc generator */ }

    /** The image resolution (both axes) */
    private static final int RESOLUTION = 500;
    /** The shininess of the test geometries */
    private static final int SHININESS = 301;
    /** The diffuse factor of the sphere */
    private static final double KD = 0.5;
    /** The diffuse factor of the triangles */
    private static final Double3 KD3 = new Double3(0.2, 0.6, 0.4);
    /** The specular factor of the sphere */
    private static final double KS = 0.5;
    /** The specular factor of the triangles */
    private static final Double3 KS3 = new Double3(0.2, 0.4, 0.3);
    /** The material of the triangles */
    private static final Material MATERIAL = new Material().setKd(KD3).setKs(KS3).setShininess(SHININESS);

    /** The light color in the triangle tests */
    private static final Color TRIANGLES_LIGHT_COLOR = new Color(800, 500, 250);
    /** The light color in the sphere tests */
    private static final Color SPHERE_LIGHT_COLOR = new Color(800, 500, 0);
    /** The emission of the sphere */
    private static final Color SPHERE_COLOR = new Color(BLUE).reduce(2);
    /** The center of the sphere */
    private static final Point SPHERE_CENTER = new Point(0, 0, -50);
    /** The radius of the sphere */
    private static final double SPHERE_RADIUS = 50;

    /** The triangle vertices: shared left-bottom, shared right-top, right-bottom, left-top */
    private static final Point[] VERTICES = {
            new Point(-110, -110, -150),
            new Point(95, 100, -150),
            new Point(110, -110, -150),
            new Point(-75, 78, 100)
    };
    /** The light position in the sphere tests */
    private static final Point SPHERE_LIGHT_POSITION = new Point(-50, -50, 25);
    /** The light direction in the sphere tests */
    private static final Vector SPHERE_LIGHT_DIRECTION = new Vector(1, 1, -0.5);
    /** The light position in the triangle tests */
    private static final Point TRIANGLES_LIGHT_POSITION = new Point(30, 10, -100);
    /** The light direction in the triangle tests */
    private static final Vector TRIANGLES_LIGHT_DIRECTION = new Vector(-2, -2, -2);

    /** The sphere */
    private static final Geometry SPHERE = new Sphere(SPHERE_CENTER, SPHERE_RADIUS)
            .setEmission(SPHERE_COLOR)
            .setMaterial(new Material().setKd(KD).setKs(KS).setShininess(SHININESS));
    /** The first triangle */
    private static final Geometry TRIANGLE1 = new Triangle(VERTICES[0], VERTICES[1], VERTICES[2]).setMaterial(MATERIAL);
    /** The second triangle */
    private static final Geometry TRIANGLE2 = new Triangle(VERTICES[0], VERTICES[1], VERTICES[3]).setMaterial(MATERIAL);

    /** The scene of the sphere tests */
    private final Scene _sphereScene = new Scene("Sphere scene");
    /** The scene of the triangle tests, with a dim ambient light */
    private final Scene _trianglesScene = new Scene("Triangles scene")
            .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

    /** The camera of the sphere tests */
    private final Camera.Builder _sphereCamera = Camera.getBuilder()
            .setRayTracer(_sphereScene, RayTracerType.SIMPLE)
            .setLocation(new Point(0, 0, 1000)).setDirection(Point.ZERO, Vector.AXIS_Y)
            .setVpSize(150, 150).setVpDistance(1000)
            .setResolution(RESOLUTION, RESOLUTION);
    /** The camera of the triangle tests */
    private final Camera.Builder _trianglesCamera = Camera.getBuilder()
            .setRayTracer(_trianglesScene, RayTracerType.SIMPLE)
            .setLocation(new Point(0, 0, 1000)).setDirection(Point.ZERO, Vector.AXIS_Y)
            .setVpSize(200, 200).setVpDistance(1000)
            .setResolution(RESOLUTION, RESOLUTION);

    /** Produce a picture of a sphere lit by a directional light */
    @Test
    void testSphereDirectional() {
        _sphereScene.geometries.add(SPHERE);
        _sphereScene.lights.add(new DirectionalLight(SPHERE_LIGHT_COLOR, SPHERE_LIGHT_DIRECTION));
        _sphereCamera.build().renderImage().writeToImage("lightSphereDirectional");
    }

    /** Produce a picture of a sphere lit by a point light */
    @Test
    void testSpherePoint() {
        _sphereScene.geometries.add(SPHERE);
        _sphereScene.lights.add(new PointLight(SPHERE_LIGHT_COLOR, SPHERE_LIGHT_POSITION).setKl(0.001).setKq(0.0002));
        _sphereCamera.build().renderImage().writeToImage("lightSpherePoint");
    }

    /** Produce a picture of a sphere lit by a spot light */
    @Test
    void testSphereSpot() {
        _sphereScene.geometries.add(SPHERE);
        _sphereScene.lights.add(new SpotLight(SPHERE_LIGHT_COLOR, SPHERE_LIGHT_POSITION, SPHERE_LIGHT_DIRECTION)
                .setKl(0.001).setKq(0.0001));
        _sphereCamera.build().renderImage().writeToImage("lightSphereSpot");
    }

    /** Produce a picture of a sphere lit by a narrow spot light */
    @Test
    void testSphereSpotSharp() {
        _sphereScene.geometries.add(SPHERE);
        _sphereScene.lights.add(new SpotLight(SPHERE_LIGHT_COLOR, SPHERE_LIGHT_POSITION, SPHERE_LIGHT_DIRECTION)
                .setKl(0.001).setKq(0.00004).setNarrowBeam(10));
        _sphereCamera.build().renderImage().writeToImage("lightSphereSpotSharp");
    }

    /** Produce a picture of a sphere lit by a directional, a point and a spot light together */
    @Test
    void testSphereMultipleLights() {
        _sphereScene.geometries.add(SPHERE);
        _sphereScene.lights.addAll(List.of(
                new DirectionalLight(new Color(0, 100, 100), new Vector(1, 1, -1)),
                new PointLight(new Color(500, 0, 0), new Point(50, 50, 50)).setKl(0.0001).setKq(0.00005),
                new SpotLight(new Color(0, 500, 0), new Point(-50, -50, 50), new Vector(1, 1, -2))
                        .setKl(0.0001).setKq(0.00005)));
        _sphereCamera.build().renderImage().writeToImage("lightSphereMultiple");
    }

    /** Produce a picture of a sphere lit by a spot light, loaded from an XML file */
    @Test
    void testSphereSpotXml() {
        Camera.getBuilder()
                .setRayTracer(SceneXMLParser.parse("scenes/sphereSpotLight.xml"), RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000)).setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(150, 150).setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("lightSphereSpotXml");
    }

    /** Produce a picture of two triangles lit by a directional light */
    @Test
    void testTrianglesDirectional() {
        _trianglesScene.geometries.add(TRIANGLE1, TRIANGLE2);
        _trianglesScene.lights.add(new DirectionalLight(TRIANGLES_LIGHT_COLOR, TRIANGLES_LIGHT_DIRECTION));
        _trianglesCamera.build().renderImage().writeToImage("lightTrianglesDirectional");
    }

    /** Produce a picture of two triangles lit by a point light */
    @Test
    void testTrianglesPoint() {
        _trianglesScene.geometries.add(TRIANGLE1, TRIANGLE2);
        _trianglesScene.lights.add(new PointLight(TRIANGLES_LIGHT_COLOR, TRIANGLES_LIGHT_POSITION)
                .setKl(0.001).setKq(0.0002));
        _trianglesCamera.build().renderImage().writeToImage("lightTrianglesPoint");
    }

    /** Produce a picture of two triangles lit by a spot light */
    @Test
    void testTrianglesSpot() {
        _trianglesScene.geometries.add(TRIANGLE1, TRIANGLE2);
        _trianglesScene.lights.add(new SpotLight(TRIANGLES_LIGHT_COLOR, TRIANGLES_LIGHT_POSITION, TRIANGLES_LIGHT_DIRECTION)
                .setKl(0.001).setKq(0.0001));
        _trianglesCamera.build().renderImage().writeToImage("lightTrianglesSpot");
    }

    /** Produce a picture of two triangles lit by a narrow spot light */
    @Test
    void testTrianglesSpotSharp() {
        _trianglesScene.geometries.add(TRIANGLE1, TRIANGLE2);
        _trianglesScene.lights.add(new SpotLight(TRIANGLES_LIGHT_COLOR, TRIANGLES_LIGHT_POSITION, TRIANGLES_LIGHT_DIRECTION)
                .setKl(0.001).setKq(0.00004).setNarrowBeam(10));
        _trianglesCamera.build().renderImage().writeToImage("lightTrianglesSpotSharp");
    }

    /** Produce a picture of two triangles lit by a directional, a point and a spot light together */
    @Test
    void testTrianglesMultipleLights() {
        _trianglesScene.geometries.add(TRIANGLE1, TRIANGLE2);
        _trianglesScene.lights.addAll(List.of(
                new DirectionalLight(new Color(100, 100, 0), new Vector(0, -1, -1)),
                new PointLight(new Color(300, 0, 300), new Point(40, -70, -100)).setKl(0.0005).setKq(0.00015),
                new SpotLight(new Color(0, 300, 300), new Point(-20, 50, -50), new Vector(2, -2, -3))
                        .setKl(0.001).setKq(0.0001)));
        _trianglesCamera.build().renderImage().writeToImage("lightTrianglesMultiple");
    }
}
