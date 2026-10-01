package showcase;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import geometries.api.Intersectable;
import geometries.impl.Geometries;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Triangle;
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
import renderer.sampling.Jittered;
import scene.ObjParser;
import scene.Scene;

import java.io.File;

/**
 * Showcase renders of textured OBJ models: a cottage, a laptop on a glass table, an aircraft and a
 * detailed desk. Models that are not in the repository are skipped.
 *
 * @author itzik123
 */
@Showcase
class ObjModelShowcaseTest {

    /** Default constructor to satisfy JavaDoc generator */
    ObjModelShowcaseTest() { /* to satisfy JavaDoc generator */ }

    /** The laptop model */
    private static final String LAPTOP = "models/Laptop_High-Polay_HP_BI_2_obj.obj";
    /** The cottage model */
    private static final String COTTAGE = "models/cottage.obj";
    /** The desk model */
    private static final String DESK = "models/desk.obj";
    /** The aircraft model */
    private static final String AIRCRAFT = "models/E 45 Aircraft_obj.obj";

    /**
     * Loads a model, skipping the test if the file is not available.
     *
     * @param parser the configured parser
     * @param path   the path of the OBJ file
     * @return the model
     */
    private static Geometries load(ObjParser parser, String path) {
        assumeTrue(new File(path).isFile(), "Model not found: " + path);
        return parser.parse(path);
    }

    /** Produce a textured cottage from three angles */
    @Test
    void testCottage() {
        Scene scene = new Scene("Cottage")
                .setBackground(new Color(135, 206, 235))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(20)));
        scene.lights.add(new DirectionalLight(new Color(255, 240, 220).reduce(4), new Vector(-1, -0.8, -1)));
        scene.geometries.add(new Plane(new Point(0, -0.1, 0), Vector.AXIS_Y).setEmission(new Color(70, 70, 75))
                .setMaterial(new Material().setKd(0.8).setKs(0.1).setShininess(10)));
        scene.geometries.add(load(new ObjParser(), COTTAGE));

        Point center = new Point(0.2, 3.36, 0.3);
        renderCottage(scene, new Point(14, 5, 18), center, "Cottage_Showcase_1_FrontRight");
        renderCottage(scene, new Point(-14, 1.5, 18), new Point(0.2, 4.5, 0.3), "Cottage_Showcase_2_LowLeft");
        renderCottage(scene, new Point(0.2, 25, 8), center, "Cottage_Showcase_3_TopDown");
    }

    /**
     * Renders the cottage scene from one angle.
     *
     * @param scene     the scene
     * @param location  the camera location
     * @param target    the point to look at
     * @param imageName the image file name
     */
    private static void renderCottage(Scene scene, Point location, Point target, String imageName) {
        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(location).setDirection(target)
                .setVpDistance(100).setVpSize(150, 150)
                .setResolution(1080, 1080)
                .setAntiAliasing(5, new Jittered())
                .setMultithreading(-2)
                .setDebugPrint(1)
                .build()
                .renderImage()
                .writeToImage(imageName);
    }

    /** Produce a laptop, whose screen texture is mapped by planar projection */
    @Test
    void testLaptop() {
        Scene scene = new Scene("Laptop")
                .setBackground(new Color(135, 206, 235))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(5)));
        scene.lights.add(new DirectionalLight(new Color(255, 250, 240).reduce(2), new Vector(1, -1, 1)));
        scene.geometries.add(load(new ObjParser().setPlanarUvMaterials("Display"), LAPTOP));

        Point target = new Point(0, 1.58, -0.67);
        Point location = new Point(8, 7, 8);
        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(location).setDirection(target)
                .setVpSize(10, 10).setVpDistance(10)
                .setResolution(600, 600)
                .setMultithreading(-2)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage("Laptop_Render_Perfect_Fit");
    }

    /** Produce the laptop on a frosted glass table, with adaptive super-sampling and the octree */
    @Test
    void testLaptopOnGlassTable() {
        Point target = new Point(0, 1.58, -0.67);
        renderLaptopOnTable(new Material().setKd(0.05).setKs(0.8).setShininess(100).setKt(0.7).setKr(0.2)
                        .setBlurRadius(0.05).setBlurGlassRays(12),
                target, target.add(new Vector(8, 5.42, 8.67).scale(0.8)), "Laptop_Final_Masterpiece_ASS_AG");
    }

    /** Produce the laptop on a clear glass table, from a low angle focused on the screen */
    @Test
    void testLaptopOnGlassTableLowAngle() {
        Point target = new Point(-0.5, 2.8, -2.0);
        renderLaptopOnTable(new Material().setKd(0.05).setKs(0.8).setShininess(100).setKt(0.7).setKr(0.2),
                target, target.add(new Vector(-9, 3.2, 7.5).scale(1.15)), "Laptop_Final_Masterpiece2_ASS_AG");
    }

    /**
     * Renders the laptop on a glass table in a dark studio.
     *
     * @param tableMaterial the glass material of the table
     * @param target        the point to look at
     * @param location      the camera location
     * @param imageName     the image file name
     */
    private static void renderLaptopOnTable(Material tableMaterial, Point target, Point location, String imageName) {
        Scene scene = new Scene("Laptop on a glass table")
                .setBackground(new Color(10, 15, 25))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(10)));
        scene.lights.add(new DirectionalLight(new Color(255, 250, 240).reduce(5), new Vector(1, -1, 1)));
        scene.lights.add(new PointLight(new Color(100, 100, 150), new Point(0, 10, 2)).setKl(0.01).setKq(0.001));
        scene.geometries.add(load(new ObjParser().setPlanarUvMaterials("Display"), LAPTOP));
        scene.geometries.add(new Polygon(new Point(-7, 0.01, 4.33), new Point(7, 0.01, 4.33),
                new Point(7, 0.01, -5.67), new Point(-7, 0.01, -5.67))
                .setEmission(new Color(5, 10, 15)).setMaterial(tableMaterial));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.ADAPTIVE_GRID)
                .setAdaptiveGridParameters(15, 8)
                .setLocation(location).setDirection(target)
                .setVpSize(8, 8).setVpDistance(10)
                .setResolution(4000, 4000)
                .setAdaptiveSuperSampling(3, 5.0)
                .setMultithreading(-1)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage(imageName);
    }

    /** Produce a textured aircraft under a dark blue sky */
    @Test
    void testAircraft() {
        Scene scene = new Scene("Aircraft")
                .setBackground(new Color(25, 45, 85).reduce(2))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(10)));
        scene.lights.add(new DirectionalLight(new Color(150, 150, 150), new Vector(1, -1, -1)));
        scene.lights.add(new PointLight(new Color(255, 255, 255), new Point(-100, 100, 100)).setKl(0.001).setKq(0.0001));
        scene.geometries.add(load(new ObjParser(), AIRCRAFT));

        Point target = new Point(-0.5, 0.5, 0);
        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.ADAPTIVE_GRID)
                .setAdaptiveGridParameters(15, 6)
                .setLocation(target.add(new Vector(1, 0.2, -1.5).normalize().scale(7))).setDirection(target)
                .setVpSize(10, 10).setVpDistance(10)
                .setResolution(800, 800)
                .setAdaptiveSuperSampling(3, 5)
                .setMultithreading(-2)
                .build()
                .renderImage()
                .writeToImage("Aircraft_Showcase");
    }

    /**
     * Produce a detailed desk model (about 650,000 triangles) with a glass desktop and glossy
     * screens, in a softly lit room.
     */
    @Test
    void testDesk() {
        Geometries desk = load(new ObjParser().setScale(42).setOffset(new Vector(50, -210, -50)), DESK);

        // The model has no glass materials, so they are assigned by material color and orientation
        Material glassTop = new Material().setKd(0.10).setKs(0.65).setKt(0.60).setKr(0.14)
                .setBlurRadius(0.05).setBlurGlassRays(8).setShininess(900);
        Material screen = new Material().setKd(0.02).setKs(0.9).setKr(0.35).setShininess(800);
        for (Intersectable child : desk.flatten()) {
            if (!(child instanceof Triangle triangle)) continue;
            double r = triangle.getMaterial().kD._d1();
            double g = triangle.getMaterial().kD._d2();
            double up = Math.abs(triangle.getNormal(null).dotProduct(Vector.AXIS_Y));
            if (r > 0.75 && r < 0.90 && up > 0.95) triangle.setMaterial(glassTop); // horizontal desktop wood
            else if (isDarkScreenMaterial(r, g) && up < 0.2) triangle.setMaterial(screen); // vertical screens
        }

        Scene scene = new Scene("Desk").setAmbientLight(new AmbientLight(new Color(55, 52, 48)));
        scene.lights.add(new DirectionalLight(new Color(220, 200, 170), new Vector(1, -1, -1)));
        scene.lights.add(new DirectionalLight(new Color(120, 130, 180), new Vector(-1, -0.8, -0.5)));
        scene.lights.add(new DirectionalLight(new Color(60, 55, 50), new Vector(0, 1, -0.2)));
        scene.lights.add(new PointLight(new Color(280, 260, 230), new Point(0, 300, 500)).setKl(0.000004).setKq(0.0000004));
        scene.lights.add(new PointLight(new Color(100, 120, 250), new Point(400, 200, 300)).setKl(0.000006).setKq(0.0000006));
        scene.lights.add(new SpotLight(new Color(320, 300, 260), new Point(50, 500, 150), new Vector(-0.1, -1, -0.4))
                .setKl(0.00003).setKq(0.000003).setNarrowBeam(3));

        // A room: floor, three walls and a ceiling
        scene.geometries.add(
                new Plane(new Point(0, -210, 0), Vector.AXIS_Y).setEmission(new Color(25, 25, 30))
                        .setMaterial(new Material().setKd(0.5).setKs(0.3).setShininess(50)),
                new Plane(new Point(0, 0, -320), Vector.AXIS_Z).setEmission(new Color(35, 40, 50))
                        .setMaterial(new Material().setKd(0.6).setShininess(1)),
                new Plane(new Point(-310, 0, 0), Vector.AXIS_X).setEmission(new Color(30, 35, 45))
                        .setMaterial(new Material().setKd(0.6).setShininess(1)),
                new Plane(new Point(600, 0, 0), new Vector(-1, 0, 0)).setEmission(new Color(35, 40, 50))
                        .setMaterial(new Material().setKd(0.6).setShininess(1)),
                new Plane(new Point(0, 500, 0), new Vector(0, -1, 0)).setEmission(new Color(20, 20, 20))
                        .setMaterial(new Material().setKd(0.8).setShininess(1)),
                desk);

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.ADAPTIVE_GRID)
                .setAdaptiveGridParameters(60, 10)
                .setLocation(new Point(470, 275, 400)).setDirection(new Point(120, 80, 160))
                .setVpSize(500, 500).setVpDistance(680)
                .setResolution(4000, 4000)
                .setMultithreading(4)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage("desk_bvh3");
    }

    /**
     * Recognizes the two near-black materials of the desk model used for its screens.
     *
     * @param red   the red diffuse factor
     * @param green the green diffuse factor
     * @return {@code true} for a screen material
     */
    private static boolean isDarkScreenMaterial(double red, double green) {
        return Math.abs(red - 0.05) < 0.01 && Math.abs(green - 0.045) < 0.01
                || Math.abs(red - 0.02) < 0.01 && Math.abs(green - 0.02) < 0.01;
    }
}
