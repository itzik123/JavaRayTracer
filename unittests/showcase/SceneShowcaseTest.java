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
     * Produce a studio shot of a glass cube frame: frosted glass edges joining brushed-metal
     * spheres, on a polished floor. Demonstrates glossy reflection and diffuse glass.
     */
    @Test
    void testGlassCube() {
        Scene scene = new Scene("Glass Cube Studio")
                .setBackground(new Color(110, 115, 140))
                .setAmbientLight(new AmbientLight(new Color(25, 25, 30)));

        // Brushed metal: blurred reflections
        Material redMetal = brushedMetal(new Double3(0.6, 0.1, 0.1));
        Material greenMetal = brushedMetal(new Double3(0.1, 0.6, 0.1));
        Material blueMetal = brushedMetal(new Double3(0.1, 0.1, 0.6));
        Material yellowMetal = brushedMetal(new Double3(0.6, 0.6, 0.1));

        // Frosted glass: blurred refractions
        Material clearGlass = frostedGlass(new Double3(0.85, 0.85, 0.9));
        Material redGlass = frostedGlass(new Double3(0.85, 0.4, 0.4));
        Material goldGlass = frostedGlass(new Double3(0.85, 0.8, 0.3));

        int s = 40;
        double sphereRadius = 19;
        Point tfl = new Point(-s, s, s), tfr = new Point(s, s, s), tbl = new Point(-s, s, -s), tbr = new Point(s, s, -s);
        Point bfl = new Point(-s, -s, s), bfr = new Point(s, -s, s), bbl = new Point(-s, -s, -s), bbr = new Point(s, -s, -s);

        scene.geometries.add(
                new Sphere(tfl, sphereRadius).setMaterial(redMetal),
                new Sphere(tfr, sphereRadius).setMaterial(greenMetal),
                new Sphere(tbl, sphereRadius).setMaterial(blueMetal),
                new Sphere(tbr, sphereRadius).setMaterial(yellowMetal),
                new Sphere(bfl, sphereRadius).setMaterial(greenMetal),
                new Sphere(bfr, sphereRadius).setMaterial(blueMetal),
                new Sphere(bbl, sphereRadius).setMaterial(yellowMetal),
                new Sphere(bbr, sphereRadius).setMaterial(redMetal));

        Color goldTint = new Color(30, 25, 5);
        Color redTint = new Color(30, 5, 5);
        Color clearTint = new Color(15, 20, 25);
        addGlassEdge(scene, tfl, bfl, sphereRadius, goldTint, goldGlass);
        for (Point[] edge : List.of(new Point[]{tfl, tfr}, new Point[]{tbl, tbr}, new Point[]{bbl, bbr}))
            addGlassEdge(scene, edge[0], edge[1], sphereRadius, redTint, redGlass);
        for (Point[] edge : List.of(new Point[]{tfl, tbl}, new Point[]{tfr, tbr}, new Point[]{tfr, bfr},
                new Point[]{tbl, bbl}, new Point[]{tbr, bbr}, new Point[]{bfl, bfr}, new Point[]{bfl, bbl},
                new Point[]{bfr, bbr}))
            addGlassEdge(scene, edge[0], edge[1], sphereRadius, clearTint, clearGlass);

        // The studio: a polished floor and two matte walls
        double floorY = -s - sphereRadius + 1;
        Material floor = new Material().setKd(0.5).setKs(0.4).setShininess(80).setKr(0.3)
                .setBlurRadius(0.05).setBlurGlassRays(36);
        Material wall = new Material().setKd(0.8);
        scene.geometries.add(
                new Polygon(new Point(-200, floorY, 200), new Point(200, floorY, 200), new Point(200, floorY, -200),
                        new Point(-200, floorY, -200)).setEmission(new Color(45, 55, 70)).setMaterial(floor),
                new Polygon(new Point(-120, floorY, 200), new Point(-120, floorY, -200), new Point(-120, 300, -200),
                        new Point(-120, 300, 200)).setEmission(new Color(90, 45, 110)).setMaterial(wall),
                new Polygon(new Point(-200, floorY, -120), new Point(200, floorY, -120), new Point(200, 300, -120),
                        new Point(-200, 300, -120)).setEmission(new Color(45, 65, 115)).setMaterial(wall));

        scene.lights.add(new PointLight(new Color(400, 400, 400), new Point(120, 180, 120)).setKl(0.0001).setKq(0.00001));
        scene.lights.add(new DirectionalLight(new Color(60, 60, 60), new Vector(1, -0.5, -1)));

        Point location = new Point(120, 75, 180);
        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(location)
                .setDirection(new Point(0, -5, 0))
                .setVpSize(150, 150).setVpDistance(140)
                .setResolution(1000, 1000)
                .setMultithreading(-2)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage("classic_glass_cube_v10_glossy");
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
     * Creates a brushed metal: a colored, blurred mirror.
     *
     * @param color the color of the metal
     * @return the material
     */
    private static Material brushedMetal(Double3 color) {
        return new Material().setKd(color).setKa(color).setKs(0.8).setShininess(300).setKr(0.25)
                .setBlurRadius(0.25).setBlurGlassRays(64);
    }

    /**
     * Creates a frosted glass: tinted, blurred transparency.
     *
     * @param tint the transparency per channel
     * @return the material
     */
    private static Material frostedGlass(Double3 tint) {
        return new Material().setKs(0.8).setShininess(120).setKt(tint).setKr(0.15)
                .setBlurRadius(0.15).setBlurGlassRays(64);
    }

    /**
     * Adds a glass cylinder between two corner spheres, sunk slightly into both.
     *
     * @param scene        the scene
     * @param from         the center of the first sphere
     * @param to           the center of the second sphere
     * @param sphereRadius the radius of the corner spheres
     * @param tint         the emission of the glass
     * @param glass        the glass material
     */
    private static void addGlassEdge(Scene scene, Point from, Point to, double sphereRadius, Color tint, Material glass) {
        double penetration = 8;
        Vector direction = to.subtract(from).normalize();
        Point start = from.add(direction.scale(sphereRadius - penetration));
        double height = from.distance(to) - 2 * (sphereRadius - penetration);
        scene.geometries.add(new Cylinder(11, new Ray(start, direction), height).setEmission(tint).setMaterial(glass));
    }
}
