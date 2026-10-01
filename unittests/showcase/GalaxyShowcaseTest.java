package showcase;

import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import renderer.Camera;
import renderer.RayTracerType;
import scene.ObjParser;
import scene.Scene;

import java.io.File;
import java.util.Random;

/**
 * Showcase of a procedurally generated spiral galaxy - over 11,000 glowing spheres and about 200
 * lights - with a spaceship model flying in front of the camera.
 *
 * @author itzik123
 */
@Showcase
class GalaxyShowcaseTest {

    /** Default constructor to satisfy JavaDoc generator */
    GalaxyShowcaseTest() { /* to satisfy JavaDoc generator */ }

    /** The spaceship model; the galaxy is rendered alone if it is not available */
    private static final String SPACESHIP_MODEL = "models/E 45 Aircraft_obj.obj";
    /** The depth of the galaxy center */
    private static final double GALAXY_DEPTH = -900;
    /** The tilt of the galaxy disk around the X axis */
    private static final double TILT_X = Math.toRadians(65);
    /** The tilt of the galaxy disk around the Z axis */
    private static final double TILT_Z = Math.toRadians(45);

    /** Produce the galaxy, with the spaceship when its model is available */
    @Test
    void testSpaceshipInTheGalaxy() {
        Scene scene = new Scene("Milky Way").setAmbientLight(new AmbientLight(new Color(2, 2, 6)));
        Random random = new Random(42);
        Point center = tilted(0, 0, 0);

        // The core: a white sun, a halo of small stars and a cloud of bright particles, all
        // transparent enough to let the sun's light through
        scene.geometries.add(new Sphere(center, 38).setEmission(new Color(255, 245, 220))
                .setMaterial(new Material().setKd(0.2).setKs(0.8).setShininess(400)));
        scene.lights.add(new PointLight(new Color(1000, 800, 600), center).setKl(0.00005).setKq(0.000005));
        for (int i = 0; i < 400; i++) {
            Point p = randomCorePoint(random, 30, 15);
            scene.geometries.add(new Sphere(p, 0.8 + random.nextDouble() * 2)
                    .setEmission(new Color(230 + random.nextInt(25), 240 + random.nextInt(15), 255))
                    .setMaterial(glowingDust()));
        }
        for (int i = 0; i < 300; i++) {
            Point p = randomCorePoint(random, 60, 40);
            Color color = new Color(245 + random.nextInt(10), 245 + random.nextInt(10), 255);
            scene.geometries.add(new Sphere(p, 1.5 + random.nextDouble() * 3).setEmission(color).setMaterial(glowingDust()));
        }

        // Four main spiral arms and four shorter arms between them
        int arms = 4;
        for (int arm = 0; arm < arms; arm++) {
            double offset = 2 * Math.PI / arms * arm;
            for (int i = 0; i < 1500; i++) {
                double distance = 60 + (800 - 60) * Math.pow(i / 1500.0, 0.85);
                addArmStar(scene, random, distance, offset - distance / 160.0 + (random.nextDouble() - 0.5) * 0.4);
            }
        }
        for (int arm = 0; arm < arms; arm++) {
            double offset = 2 * Math.PI / arms * arm + Math.PI / arms;
            for (int i = 0; i < 800; i++) {
                double distance = 70 + (450 - 70) * Math.pow(i / 800.0, 0.9);
                addArmStar(scene, random, distance, offset - distance / 130.0 + (random.nextDouble() - 0.5) * 0.6);
            }
        }

        // Distant background stars and star clusters
        for (int i = 0; i < 1500; i++) {
            Point p = new Point((random.nextDouble() - 0.5) * 9000, (random.nextDouble() - 0.5) * 9000,
                    -1500 - random.nextDouble() * 5000);
            int shade = 180 + random.nextInt(75);
            Color color = random.nextBoolean() ? new Color(shade, shade, 255) : new Color(255, 255, 255);
            scene.geometries.add(new Sphere(p, 2 + random.nextDouble() * 5).setEmission(color));
        }
        for (int c = 0; c < 6; c++) {
            double cx = (random.nextDouble() - 0.5) * 6000;
            double cy = (random.nextDouble() - 0.5) * 6000;
            double cz = -2000 - random.nextDouble() * 3000;
            for (int i = 0; i < 70; i++) {
                Point p = new Point(cx + random.nextGaussian() * 150, cy + random.nextGaussian() * 150,
                        cz + random.nextGaussian() * 150);
                scene.geometries.add(new Sphere(p, 3 + random.nextDouble() * 6)
                        .setEmission(new Color(200 + random.nextInt(55), 220 + random.nextInt(35), 255)));
            }
        }

        // Cinematic lighting: magenta and ice-blue washes, and a spot on the galaxy center
        scene.lights.add(new DirectionalLight(new Color(60, 20, 70), new Vector(-1, 0.8, -1)));
        scene.lights.add(new DirectionalLight(new Color(20, 30, 80), new Vector(1, -0.5, 0.5)));
        Point spotPosition = center.add(new Vector(0, 800, 300));
        scene.lights.add(new SpotLight(new Color(800, 700, 1000), spotPosition, center.subtract(spotPosition))
                .setKl(0.000005).setKq(0.0000001));

        boolean withSpaceship = new File(SPACESHIP_MODEL).isFile();
        if (withSpaceship)
            scene.geometries.add(new ObjParser().setOffset(new Vector(15, -12, 940)).parse(SPACESHIP_MODEL));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.ADAPTIVE_GRID)
                .setAdaptiveGridParameters(8, 15)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(1000, 1000).setVpDistance(1000)
                .setResolution(5000, 5000)
                .setMultithreading(-1)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage(withSpaceship ? "SpaceShipMilkyWayGalaxy2" : "MilkyWayGalaxy");
    }

    /**
     * Picks a random point in a flattened shell around the galaxy core.
     *
     * @param random      the random generator
     * @param radiusRange the range of distances beyond the minimum of 40
     * @param thickness   the thickness of the shell
     * @return the point in the scene
     */
    private static Point randomCorePoint(Random random, double radiusRange, double thickness) {
        double r = 40 + random.nextDouble() * radiusRange;
        double theta = random.nextDouble() * 2 * Math.PI;
        double phi = random.nextDouble() * Math.PI;
        return tilted(r * Math.sin(phi) * Math.cos(theta), (random.nextDouble() - 0.5) * thickness, r * Math.cos(phi));
    }

    /**
     * Returns a nearly transparent material, so particles glow without blocking the lights.
     *
     * @return a nearly transparent material, so particles glow without blocking the lights
     */
    private static Material glowingDust() {
        return new Material().setKd(0.2).setKs(0.8).setShininess(400).setKt(0.95);
    }

    /**
     * Adds a star of a spiral arm; about one star in fifty is also a point light.
     *
     * @param scene    the scene
     * @param random   the random generator
     * @param distance the distance of the star from the galaxy center
     * @param angle    the angle of the star around the galaxy center
     */
    private static void addArmStar(Scene scene, Random random, double distance, double angle) {
        // The disk is an ellipse, stretched along one axis for a more dynamic look
        double flatX = distance * 1.7 * Math.cos(angle);
        double flatY = (random.nextDouble() - 0.5) * 35;
        double flatZ = distance * 0.5 * Math.sin(angle);

        // Pink-violet core, purple middle, deep blue rim with occasional ice-blue stars
        Color emission;
        if (distance < 250) emission = new Color(220 + random.nextInt(35), 100 + random.nextInt(60), 255);
        else if (distance < 550) emission = new Color(200 + random.nextInt(55), 10 + random.nextInt(20), 220 + random.nextInt(35));
        else {
            emission = new Color(10 + random.nextInt(30), 20 + random.nextInt(40), 255);
            if (random.nextDouble() > 0.85) emission = new Color(100 + random.nextInt(50), 180 + random.nextInt(50), 255);
        }

        Point p = tilted(flatX, flatY, flatZ);
        boolean isLight = random.nextDouble() > 0.98;
        // Stars are (almost) transparent, so they glow without blocking the lights behind them
        Material material = isLight ? new Material().setKt(1.0) : glowingDust();
        scene.geometries.add(new Sphere(p, 0.8 + random.nextDouble() * 3.5).setEmission(emission).setMaterial(material));
        if (isLight) scene.lights.add(new PointLight(emission.scale(1.5), p).setKl(0.001).setKq(0.0001));
    }

    /**
     * Places a point of the flat galaxy disk in the scene: tilts it around the X and Z axes and
     * moves it to the galaxy depth.
     *
     * @param x the X coordinate on the disk
     * @param y the Y coordinate (thickness) on the disk
     * @param z the Z coordinate on the disk
     * @return the point in the scene
     */
    private static Point tilted(double x, double y, double z) {
        double y1 = y * Math.cos(TILT_X) - z * Math.sin(TILT_X);
        double z1 = y * Math.sin(TILT_X) + z * Math.cos(TILT_X);
        return new Point(x * Math.cos(TILT_Z) - y1 * Math.sin(TILT_Z), x * Math.sin(TILT_Z) + y1 * Math.cos(TILT_Z),
                z1 + GALAXY_DEPTH);
    }
}
