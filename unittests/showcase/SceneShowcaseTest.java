package showcase;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import renderer.Camera;
import renderer.RayTracerType;
import renderer.sampling.Jittered;
import scene.Scene;
import scene.SceneXMLParser;

import java.util.List;
import java.util.Random;

/**
 * Showcase scenes built from the primitive geometries: glossy and frosted materials, mirrors,
 * transparency and hundreds of objects.
 *
 * @author itzik123
 */
@Showcase
class SceneShowcaseTest {

    /** Default constructor to satisfy JavaDoc generator */
    SceneShowcaseTest() { /* to satisfy JavaDoc generator */ }

    /** The radius of the glass cube's corner balls */
    private static final double BALL_RADIUS = 20;
    /** The distance between the centers of adjacent corner balls */
    private static final double CUBE_EDGE = 100;
    /** The radius of the glass rods */
    private static final double ROD_RADIUS = 9;
    /** How deep the rods are sunk into the balls */
    private static final double ROD_SINK = 4;

    /**
     * Produce "The Shattered Dimension": a vortex of 500 random spheres, glass shards and
     * cylinders inside a box of mirrors, lit from within by 10 point lights. Uses all six
     * geometry types and the adaptive grid.
     */
    @Test
    void testShatteredDimension() {
        Scene scene = new Scene("Shattered Dimension").setAmbientLight(new AmbientLight(new Color(2, 2, 2)));

        Material mirror = new Material().setKd(0.0).setKs(0.8).setShininess(400).setKr(0.85);
        Material pureGlass = new Material().setKd(0.0).setKs(0.9).setShininess(500).setKt(0.95).setKr(0.05);
        Material coloredGlass = new Material().setKd(0.1).setKs(0.8).setShininess(200).setKt(0.8).setKr(0.2);
        Material metal = new Material().setKd(0.4).setKs(0.8).setShininess(300).setKr(0.4);
        Material matte = new Material().setKd(0.8).setKs(0.2).setShininess(10);

        // A mirror floor and three mirror walls reflect each other endlessly
        scene.geometries.add(
                new Plane(new Point(0, -80, 0), Vector.AXIS_Y).setEmission(new Color(5, 5, 10)).setMaterial(mirror),
                new Polygon(new Point(-300, -80, -400), new Point(300, -80, -400), new Point(300, 300, -400),
                        new Point(-300, 300, -400)).setEmission(new Color(2, 2, 2)).setMaterial(mirror),
                new Polygon(new Point(-300, -80, 100), new Point(-300, -80, -400), new Point(-300, 300, -400),
                        new Point(-300, 300, 100)).setEmission(new Color(2, 2, 2)).setMaterial(mirror),
                new Polygon(new Point(300, -80, -400), new Point(300, -80, 100), new Point(300, 300, 100),
                        new Point(300, 300, -400)).setEmission(new Color(2, 2, 2)).setMaterial(mirror));

        // Neon tubes weaving through the scene, around a glass core
        scene.geometries.add(
                new Tube(2.5, new Ray(new Point(-200, -50, -100), new Vector(1, 0.4, -0.2)))
                        .setEmission(new Color(0, 255, 200)).setMaterial(metal),
                new Tube(1.5, new Ray(new Point(200, -20, -50), new Vector(-1, 0.6, -0.5)))
                        .setEmission(new Color(255, 0, 150)).setMaterial(metal),
                new Tube(3.0, new Ray(new Point(0, -80, -250), new Vector(0.2, 1, 0.5)))
                        .setEmission(new Color(150, 0, 255)).setMaterial(metal),
                new Sphere(new Point(0, 30, -150), 45).setMaterial(pureGlass));

        // A tornado of random debris, with a light source every 50 objects
        Random random = new Random(111);
        for (int i = 0; i < 500; i++) {
            double height = -70 + random.nextDouble() * 250;
            double radius = 55 + (height + 70) * 0.4 + random.nextDouble() * 40;
            double angle = height * 0.15 + random.nextDouble() * Math.PI * 2;
            double x = Math.cos(angle) * radius;
            double z = -150 + Math.sin(angle) * radius;
            Point p = new Point(x, height, z);

            Color color = new Color(50 + random.nextInt(205), 20 + random.nextInt(150), 100 + random.nextInt(155));
            int materialType = random.nextInt(4);
            Material material = materialType == 0 ? coloredGlass : materialType == 1 ? metal : matte;

            switch (random.nextInt(3)) {
                case 0 -> scene.geometries.add(new Sphere(p, 2 + random.nextDouble() * 6)
                        .setEmission(color).setMaterial(material));
                case 1 -> {
                    double s = 6 + random.nextDouble() * 10;
                    Point p2 = new Point(x + s, height + random.nextDouble() * s, z + random.nextDouble() * s);
                    Point p3 = new Point(x - s, height + random.nextDouble() * s, z - random.nextDouble() * s);
                    scene.geometries.add(new Triangle(p, p2, p3).setEmission(color).setMaterial(material));
                }
                default -> {
                    Vector direction = new Vector(random.nextDouble() + 0.1, random.nextDouble() + 0.1,
                            random.nextDouble() + 0.1);
                    scene.geometries.add(new Cylinder(1 + random.nextDouble() * 2.5, new Ray(p, direction),
                            5 + random.nextDouble() * 15).setEmission(color).setMaterial(material));
                }
            }
            if (i % 50 == 0) scene.lights.add(new PointLight(color.scale(1.5), p).setKl(0.005).setKq(0.0001));
        }

        scene.lights.add(new SpotLight(new Color(1000, 1000, 1000), new Point(0, 200, 50), new Vector(0, -1, -1))
                .setKl(0.0001).setKq(0.00001));
        scene.lights.add(new DirectionalLight(new Color(15, 15, 30), new Vector(1, -1, -1)));

        Point location = new Point(0, 10, 180);
        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.ADAPTIVE_GRID)
                .setAdaptiveGridParameters(5, 15)
                .setLocation(location)
                .setDirection(new Point(0, 30, -150).subtract(location), Vector.AXIS_Y)
                .setVpSize(200, 200).setVpDistance(150)
                .setResolution(4000, 4000)
                .setMultithreading(-1)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage("bonus1_shattered_dimension_v3");
    }

    /**
     * Produce a cube frame of glass rods joining mirror-like spheres on a polished floor - a
     * classic ray tracing scene, with real refraction, colored glass and soft shadows.
     */
    @Test
    void testGlassCube() {
        Scene scene = new Scene("Glass Cube")
                .setBackground(new Color(102, 102, 154))
                .setAmbientLight(new AmbientLight(new Color(28, 28, 38)));

        // The corners of the cube, named Top/Bottom, Front/Back, Left/Right
        double half = CUBE_EDGE / 2;
        double top = BALL_RADIUS + CUBE_EDGE;
        Point tfl = new Point(-half, top, half), tfr = new Point(half, top, half);
        Point tbl = new Point(-half, top, -half), tbr = new Point(half, top, -half);
        Point bfl = new Point(-half, BALL_RADIUS, half), bfr = new Point(half, BALL_RADIUS, half);
        Point bbl = new Point(-half, BALL_RADIUS, -half), bbr = new Point(half, BALL_RADIUS, -half);

        Material red = ballMaterial(new Double3(0.58, 0.15, 0.2));
        Material yellow = ballMaterial(new Double3(0.55, 0.55, 0.2));
        Material blue = ballMaterial(new Double3(0.1, 0.1, 0.65));
        Material green = ballMaterial(new Double3(0.08, 0.45, 0.15));
        scene.geometries.add(
                new Sphere(tfl, BALL_RADIUS).setMaterial(red), new Sphere(bbr, BALL_RADIUS).setMaterial(red),
                new Sphere(tfr, BALL_RADIUS).setMaterial(yellow), new Sphere(bfl, BALL_RADIUS).setMaterial(yellow),
                new Sphere(tbl, BALL_RADIUS).setMaterial(blue), new Sphere(bfr, BALL_RADIUS).setMaterial(blue),
                new Sphere(tbr, BALL_RADIUS).setMaterial(green), new Sphere(bbl, BALL_RADIUS).setMaterial(green));

        addRod(scene, tfl, bfl, glassMaterial(new Double3(0.88, 0.86, 0.2)));
        addRod(scene, tbr, tfr, glassMaterial(new Double3(0.85, 0.1, 0.18)));
        addRod(scene, bbl, bbr, glassMaterial(new Double3(0.9, 0.4, 0.15)));
        Material clearGlass = glassMaterial(new Double3(0.92, 0.92, 0.95));
        for (Point[] edge : List.of(new Point[]{tfl, tfr}, new Point[]{tfl, tbl}, new Point[]{tbl, tbr},
                new Point[]{tfr, bfr}, new Point[]{tbl, bbl}, new Point[]{tbr, bbr}, new Point[]{bfl, bfr},
                new Point[]{bfl, bbl}, new Point[]{bfr, bbr}))
            addRod(scene, edge[0], edge[1], clearGlass);

        scene.geometries.add(new Polygon(new Point(-245, 0, -245), new Point(245, 0, -245),
                new Point(245, 0, 245), new Point(-245, 0, 245))
                .setEmission(new Color(5, 5, 8)).setMaterial(new Material().setKd(0.3).setKr(0.25)));

        scene.lights.add(new PointLight(new Color(250, 250, 250), new Point(500, 445, 500)).setSoftShadows(30, 16));
        scene.lights.add(new PointLight(new Color(230, 230, 230), new Point(-420, 530, 500)).setSoftShadows(30, 16));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.ADAPTIVE_GRID)
                .setLocation(new Point(-100, 171, 202)).setDirection(new Point(-17, 82, 45))
                .setVpSize(101, 101).setVpDistance(100)
                .setResolution(1600, 1600)
                .setAntiAliasing(4, new Jittered())
                .setMultithreading(-2)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage("glassCube");
    }

    /**
     * Produce the "Crystal Gallery" XML scene from the side, with anti-aliasing and a tilted
     * camera: frosted and glossy spheres, a pyramid, a cylinder and a tube.
     */
    @Test
    void testCrystalGallery() {
        Camera.getBuilder()
                .setRayTracer(SceneXMLParser.parse("scenes/crystalGallery.xml"), RayTracerType.SIMPLE)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620).setVpDistance(1000)
                .setResolution(1000, 1000)
                .setAntiAliasing(9, new Jittered())
                .setMultithreading(-2)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage("xml_crystalGallery_sideView_glossy_diffuse");
    }

    /**
     * Creates the material of a corner ball: a colored, mirror-like satin finish.
     *
     * @param color the color of the ball
     * @return the material
     */
    private static Material ballMaterial(Double3 color) {
        return new Material().setKd(color).setKa(color).setKs(0.25).setShininess(25).setKr(0.35);
    }

    /**
     * Creates a solid glass with a refractive index of 1.5.
     *
     * @param tint the transparency per channel, which colors the glass
     * @return the material
     */
    private static Material glassMaterial(Double3 tint) {
        return new Material().setKs(0.8).setShininess(400).setKt(tint).setIor(1.5);
    }

    /**
     * Adds a glass rod between two corner balls, sunk slightly into both.
     *
     * @param scene    the scene
     * @param from     the center of the first ball
     * @param to       the center of the second ball
     * @param material the glass material
     */
    private static void addRod(Scene scene, Point from, Point to, Material material) {
        Vector direction = to.subtract(from).normalize();
        Point start = from.add(direction.scale(BALL_RADIUS - ROD_SINK));
        double length = from.distance(to) - 2 * (BALL_RADIUS - ROD_SINK);
        scene.geometries.add(new Cylinder(ROD_RADIUS, new Ray(start, direction), length).setMaterial(material));
    }
}
