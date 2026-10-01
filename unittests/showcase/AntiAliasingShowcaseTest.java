package showcase;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import renderer.Camera;
import renderer.RayTracerType;
import renderer.sampling.Jittered;
import scene.Scene;

/**
 * Showcase of anti-aliasing by jittered super-sampling: the same scenes rendered with one ray
 * per pixel and with increasing numbers of rays, reporting the render times.
 *
 * @author itzik123
 */
@Showcase
class AntiAliasingShowcaseTest {

    /** Default constructor to satisfy JavaDoc generator */
    AntiAliasingShowcaseTest() { /* to satisfy JavaDoc generator */ }

    /** Produce a scene of diagonal edges and spheres, without and with 9x9 anti-aliasing */
    @Test
    void testAntiAliasingOnOff() {
        Scene scene = new Scene("Anti-aliasing showcase")
                .setBackground(new Color(10, 10, 15))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(5)));

        Material shiny = new Material().setKd(0.5).setKs(0.5).setShininess(60).setKr(0.2);
        Material matte = new Material().setKd(0.8).setKs(0.2).setShininess(30);
        Material glass = new Material().setKd(0.2).setKs(0.2).setShininess(30).setKt(0.8);

        scene.geometries.add(
                new Plane(new Point(0, -10, 0), Vector.AXIS_Y).setEmission(new Color(20, 20, 20)).setMaterial(shiny),
                // Diagonal edges show aliasing best
                new Triangle(new Point(-30, -10, -80), new Point(0, 30, -80), new Point(30, -10, -80))
                        .setEmission(new Color(50, 0, 0)).setMaterial(matte),
                new Triangle(new Point(-50, -10, -70), new Point(-20, 20, -70), new Point(0, -10, -70))
                        .setEmission(new Color(0, 50, 0)).setMaterial(matte),
                new Triangle(new Point(0, -10, -70), new Point(20, 20, -70), new Point(50, -10, -70))
                        .setEmission(new Color(0, 0, 50)).setMaterial(matte),
                new Sphere(new Point(0, 0, -50), 10).setMaterial(glass),
                new Sphere(new Point(-15, 0, -45), 4).setEmission(new Color(255, 100, 100)).setMaterial(shiny),
                new Sphere(new Point(15, 0, -45), 4).setEmission(new Color(100, 255, 100)).setMaterial(shiny),
                new Sphere(new Point(0, 15, -45), 4).setEmission(new Color(100, 100, 255)).setMaterial(shiny),
                new Sphere(new Point(-10, -10, -40), 3).setEmission(new Color(255, 255, 100)).setMaterial(shiny),
                new Sphere(new Point(10, -10, -40), 3).setEmission(new Color(255, 100, 255)).setMaterial(shiny));

        scene.lights.add(new DirectionalLight(new Color(50, 50, 50), new Vector(1, -1, -1)));
        scene.lights.add(new PointLight(new Color(100, 100, 0), new Point(-20, 20, -20)).setKl(0.0001).setKq(0.00005));
        scene.lights.add(new SpotLight(new Color(200, 200, 200), new Point(0, 30, -10), new Vector(0, -1, -3))
                .setKl(0.0001).setKq(0.00005));

        Camera.Builder camera = Camera.getBuilder()
                .setLocation(new Point(0, 10, 20)).setDirection(new Point(0, 0, -50))
                .setVpDistance(100).setVpSize(150, 150)
                .setResolution(600, 600)
                .setMultithreading(-2)
                .setRayTracer(scene, RayTracerType.SIMPLE);

        timedRender(camera, "AntiAliasing_Showcase_Off");
        timedRender(camera.setAntiAliasing(9, new Jittered()), "AntiAliasing_Showcase_On");
    }

    /**
     * Produce a cage of thin diagonal bars in front of glass and metal spheres, with 1, 9, 49
     * and 121 rays per pixel.
     */
    @Test
    void testAntiAliasingResolutions() {
        Scene scene = new Scene("Anti-aliasing cage")
                .setBackground(new Color(15, 15, 20))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(10)));

        scene.geometries.add(
                new Plane(new Point(0, -20, 0), Vector.AXIS_Y).setEmission(new Color(10, 10, 10))
                        .setMaterial(new Material().setKd(0.2).setKs(0.5).setShininess(40).setKr(0.5)),
                new Sphere(new Point(18, -2, -30), 18).setEmission(new Color(100, 20, 20))
                        .setMaterial(new Material().setKd(0.4).setKs(0.6).setShininess(80).setKr(0.2)),
                new Sphere(new Point(-18, -2, -30), 18).setEmission(new Color(0, 0, 10))
                        .setMaterial(new Material().setKs(0.8).setShininess(60).setKt(0.9)));
        Material bars = new Material().setKd(0.8).setKs(0.2).setShininess(20);
        for (int i = -3; i <= 3; i++)
            scene.geometries.add(new Cylinder(0.6, new Ray(new Point(-50, i * 10 - 5, 20), new Vector(1, 0.4, 0)), 100)
                    .setEmission(new Color(40, 40, 40)).setMaterial(bars));

        scene.lights.add(new SpotLight(new Color(400, 400, 400), new Point(-40, 50, 60), new Vector(1, -1, -1))
                .setKl(0.00001).setKq(0.000005));
        scene.lights.add(new PointLight(new Color(0, 50, 150), new Point(0, 10, -80)).setKl(0.0001).setKq(0.0001));

        Camera.Builder camera = Camera.getBuilder()
                .setLocation(new Point(0, 10, 80)).setDirection(new Point(0, 0, -30))
                .setVpDistance(100).setVpSize(100, 100)
                .setResolution(800, 800)
                .setMultithreading(-2)
                .setRayTracer(scene, RayTracerType.SIMPLE);

        for (int resolution : new int[]{1, 3, 7, 11})
            timedRender(camera.setAntiAliasing(resolution, new Jittered()),
                    "AA_Cage_Test_" + resolution * resolution + "_Rays");
    }

    /**
     * Renders an image and prints how long the rendering took.
     *
     * @param camera    the camera builder
     * @param imageName the image file name
     */
    private static void timedRender(Camera.Builder camera, String imageName) {
        long start = System.nanoTime();
        Camera built = camera.build().renderImage();
        System.out.printf("%s: %.2f s%n", imageName, (System.nanoTime() - start) / 1e9);
        built.writeToImage(imageName);
    }
}
