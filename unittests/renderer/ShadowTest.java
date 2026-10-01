package renderer;

import static java.awt.Color.BLUE;

import geometries.api.Intersectable;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Rendering tests of shadows: a triangle between a sphere and a spot light, moved step by step,
 * and a sphere casting its shadow on a pair of triangles.
 *
 * @author Dan Zilberstein
 */
class ShadowTest {

    /** Default constructor to satisfy JavaDoc generator */
    ShadowTest() { /* to satisfy JavaDoc generator */ }

    /** The image resolution (both axes) */
    private static final int RESOLUTION = 600;
    /** The spot light position of most of the sphere-triangle tests */
    private static final Point SPOT_POSITION = new Point(-100, -100, 200);

    /** The scene of the tests */
    private final Scene _scene = new Scene("Shadow scene");
    /** The camera of the tests */
    private final Camera.Builder _cameraBuilder = Camera.getBuilder()
            .setLocation(new Point(0, 0, 1000)).setDirection(Point.ZERO, Vector.AXIS_Y)
            .setVpDistance(1000).setVpSize(200, 200)
            .setResolution(RESOLUTION, RESOLUTION)
            .setRayTracer(_scene, RayTracerType.SIMPLE);

    /** The sphere of the sphere-triangle tests */
    private final Intersectable _sphere = new Sphere(new Point(0, 0, -200), 60)
            .setEmission(new Color(BLUE))
            .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(99));
    /** The material of the triangle in the sphere-triangle tests */
    private final Material _triangleMaterial = new Material().setKd(0.5).setKs(0.5).setShininess(30);

    /**
     * Renders a sphere shaded by a triangle placed between it and a spot light.
     *
     * @param imageName    the image file name
     * @param triangle     the triangle
     * @param spotPosition the spot light position
     */
    private void renderSphereAndTriangle(String imageName, Triangle triangle, Point spotPosition) {
        _scene.geometries.add(_sphere, triangle.setEmission(new Color(BLUE)).setMaterial(_triangleMaterial));
        _scene.lights.add(new SpotLight(new Color(400, 240, 0), spotPosition, new Vector(1, 1, -3))
                .setKl(1E-5).setKq(1.5E-7));
        _cameraBuilder.build().renderImage().writeToImage(imageName);
    }

    /** Produce a picture of a sphere shaded by a triangle */
    @Test
    void testSphereTriangleInitial() {
        renderSphereAndTriangle("shadowSphereTriangleInitial",
                new Triangle(new Point(-70, -40, 0), new Point(-40, -70, 0), new Point(-68, -68, -4)), SPOT_POSITION);
    }

    /** Produce the sphere-triangle picture with the triangle moved towards the light */
    @Test
    void testSphereTriangleMove1() {
        renderSphereAndTriangle("shadowSphereTriangleMove1",
                new Triangle(new Point(-70, -40, 10), new Point(-40, -70, 10), new Point(-68, -68, 6)), SPOT_POSITION);
    }

    /** Produce the sphere-triangle picture with the triangle moved further towards the light */
    @Test
    void testSphereTriangleMove2() {
        renderSphereAndTriangle("shadowSphereTriangleMove2",
                new Triangle(new Point(-75, -45, 5), new Point(-45, -75, 5), new Point(-63, -68, -4)), SPOT_POSITION);
    }

    /** Produce the sphere-triangle picture with the light moved closer */
    @Test
    void testSphereTriangleSpot1() {
        renderSphereAndTriangle("shadowSphereTriangleSpot1",
                new Triangle(new Point(-70, -40, 0), new Point(-40, -70, 0), new Point(-68, -68, -4)),
                new Point(-90, -90, 170));
    }

    /** Produce the sphere-triangle picture with the light moved even closer */
    @Test
    void testSphereTriangleSpot2() {
        renderSphereAndTriangle("shadowSphereTriangleSpot2",
                new Triangle(new Point(-70, -40, 0), new Point(-40, -70, 0), new Point(-68, -68, -4)),
                new Point(-80, -80, 130));
    }

    /** Produce a picture of two triangles shaded by a sphere, lit by a spot light */
    @Test
    void testTrianglesSphere() {
        Material triangleMaterial = new Material().setKd(0.2).setKs(0.4).setShininess(111);
        _scene.geometries.add(
                new Triangle(new Point(-150, -150, -115), new Point(150, -150, -135), new Point(75, 75, -150))
                        .setMaterial(triangleMaterial),
                new Triangle(new Point(-150, -150, -115), new Point(-70, 70, -140), new Point(75, 75, -150))
                        .setMaterial(triangleMaterial),
                new Sphere(new Point(0, 0, -11), 30)
                        .setEmission(new Color(BLUE))
                        .setMaterial(new Material().setKd(0.5).setKs(0.4).setShininess(111)));
        _scene.setAmbientLight(new AmbientLight(new Color(38, 38, 38)));
        _scene.lights.add(new SpotLight(new Color(700, 400, 400), new Point(40, 40, 115), new Vector(-1, -1, -1))
                .setKl(1E-6).setKq(1E-12));
        _cameraBuilder.build().renderImage().writeToImage("shadowTrianglesSphere");
    }
}
