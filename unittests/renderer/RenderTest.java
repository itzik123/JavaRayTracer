package renderer;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static java.awt.Color.YELLOW;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import scene.Scene;
import scene.SceneXMLParser;

/**
 * End-to-end rendering tests of the basic pipeline: scene construction, camera setup, ray tracing
 * and image output. The scene is a sphere surrounded by three triangles, covering the four
 * quadrants of the view.
 *
 * @author Dan Zilberstein
 */
class RenderTest {

    /** Default constructor to satisfy JavaDoc generator */
    RenderTest() { /* to satisfy JavaDoc generator */ }

    /** The size of the (square) view plane */
    private static final double VP_SIZE = 500;
    /** The distance from the camera to the view plane */
    private static final double VP_DISTANCE = 100;
    /** The image resolution (both axes) */
    private static final int RESOLUTION = 1000;
    /** An odd image resolution, which puts the center of the view on a pixel center */
    private static final int ODD_RESOLUTION = 1001;
    /** The grid spacing, in pixels */
    private static final int GRID_INTERVAL = 100;

    /** The depth of the triangles and the sphere center */
    private static final double Z = -100;
    /** Left-middle point */
    private static final Point P_LM = new Point(-100, 0, Z);
    /** Middle-top point */
    private static final Point P_MT = new Point(0, 100, Z);
    /** Left-top point */
    private static final Point P_LT = new Point(-100, 100, Z);
    /** Middle-bottom point */
    private static final Point P_MB = new Point(0, -100, Z);
    /** Left-bottom point */
    private static final Point P_LB = new Point(-100, -100, Z);
    /** Right-middle point */
    private static final Point P_RM = new Point(100, 0, Z);
    /** Right-bottom point */
    private static final Point P_RB = new Point(100, -100, Z);
    /** The center of the sphere */
    private static final Point CENTER = new Point(0, 0, Z);
    /** The radius of the sphere */
    private static final double RADIUS = 50;

    /** The sphere in the center */
    private final Sphere _sphere = new Sphere(CENTER, RADIUS);
    /** The top-left triangle */
    private final Triangle _triangleLeftTop = new Triangle(P_LM, P_MT, P_LT);
    /** The bottom-left triangle */
    private final Triangle _triangleLeftBottom = new Triangle(P_LM, P_MB, P_LB);
    /** The bottom-right triangle */
    private final Triangle _triangleRightBottom = new Triangle(P_RM, P_MB, P_RB);

    /**
     * Renders a scene with a grid, from the origin looking down -Z, and saves it.
     *
     * @param scene      the scene
     * @param resolution the image resolution (both axes)
     * @param imageName  the image file name
     * @param gridColor  the grid color
     */
    private static void render(Scene scene, int resolution, String imageName, java.awt.Color gridColor) {
        Camera.getBuilder()
                .setLocation(Point.ZERO).setDirection(new Point(0, 0, -1))
                .setVpDistance(VP_DISTANCE).setVpSize(VP_SIZE, VP_SIZE)
                .setResolution(resolution, resolution)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .build()
                .renderImage()
                .printGrid(GRID_INTERVAL, new Color(gridColor))
                .writeToImage(imageName);
    }

    /** Produce a scene lit by ambient light only, over a colored background */
    @Test
    void testBasicRenderTwoColors() {
        Scene scene = new Scene("Two colors")
                .setBackground(new Color(75, 127, 90))
                .setAmbientLight(new AmbientLight(new Color(255, 191, 191)));
        scene.geometries.add(_sphere, _triangleLeftTop, _triangleLeftBottom, _triangleRightBottom);
        render(scene, RESOLUTION, "Two colors render test", YELLOW);
    }

    /** Produce the two-colors scene loaded from an XML file */
    @Test
    void testBasicRenderXml() {
        render(SceneXMLParser.parse("scenes/basicRenderTwoColors.xml"), RESOLUTION, "render test xml", YELLOW);
    }

    /** Produce a scene where every geometry has its own emission color */
    @Test
    void testRenderEmissionColor() {
        Scene scene = new Scene("Emission color").setAmbientLight(new AmbientLight(new Color(51, 51, 51)));
        scene.geometries.add(_sphere,
                _triangleLeftTop.setEmission(new Color(GREEN)),
                _triangleLeftBottom.setEmission(new Color(RED)),
                _triangleRightBottom.setEmission(new Color(BLUE)));
        render(scene, ODD_RESOLUTION, "emission render test", WHITE);
    }

    /** Produce a scene where every geometry reflects the white ambient light differently */
    @Test
    void testRenderAmbientColor() {
        Scene scene = new Scene("Ambient colors").setAmbientLight(new AmbientLight(new Color(WHITE)));
        scene.geometries.add(
                _sphere.setMaterial(new Material().setKa(0.4)),
                _triangleLeftTop.setMaterial(new Material().setKa(new Double3(0, 0.8, 0))),
                _triangleLeftBottom.setMaterial(new Material().setKa(new Double3(0.8, 0, 0))),
                _triangleRightBottom.setMaterial(new Material().setKa(new Double3(0, 0, 0.8))));
        render(scene, ODD_RESOLUTION, "ambient render test", WHITE);
    }
}
