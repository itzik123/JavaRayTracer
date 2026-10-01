package showcase;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import geometries.impl.Polygon;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Benchmarks of the performance features, reproducing the tables in the README.
 * <p>
 * Every configuration renders the same image - only the time changes - and the render times are
 * printed. The brute-force render of the laptop takes about 45 minutes.
 *
 * @author itzik123
 */
@Showcase
class PerformanceBenchmarkTest {

    /** Default constructor to satisfy JavaDoc generator */
    PerformanceBenchmarkTest() { /* to satisfy JavaDoc generator */ }

    /** Voxels along each axis of the regular grid */
    private static final int GRID_RESOLUTION = 50;
    /** Geometries an octree cell may hold before it is split */
    private static final int OCTREE_CAPACITY = 15;
    /** Maximum depth of the octree */
    private static final int OCTREE_DEPTH = 6;
    /** Color tolerance of adaptive super-sampling */
    private static final double ADAPTIVE_TOLERANCE = 5;
    /** The laptop model (a free Free3D model, not redistributed with the project) */
    private static final String LAPTOP_MODEL = "models/Laptop_High-Polay_HP_BI_2_obj.obj";

    /**
     * A benchmark scene and its camera.
     */
    enum BenchmarkScene {
        /** The Utah teapot with 400 glass bubbles - 1,392 geometries */
        TEAPOT(PerformanceBenchmarkTest::teapot, new Point(0, 0, -1000), Point.ZERO, 200, 1000, 1000),
        /** A high-polygon laptop on a glass table - 130,351 geometries */
        LAPTOP(PerformanceBenchmarkTest::laptop, new Point(8, 7, 8), new Point(0, 1.58, -0.67), 10, 10, 400);

        /** Builds the scene */
        final Supplier<Scene> _scene;
        /** The camera location */
        final Point _location;
        /** The point the camera looks at */
        final Point _target;
        /** The size of the (square) view plane */
        final double _vpSize;
        /** The distance from the camera to the view plane */
        final double _vpDistance;
        /** The image resolution (both axes) */
        final int _resolution;

        /**
         * Creates a benchmark scene.
         *
         * @param scene      builds the scene
         * @param location   the camera location
         * @param target     the point the camera looks at
         * @param vpSize     the size of the view plane
         * @param vpDistance the distance to the view plane
         * @param resolution the image resolution
         */
        BenchmarkScene(Supplier<Scene> scene, Point location, Point target, double vpSize, double vpDistance, int resolution) {
            _scene = scene;
            _location = location;
            _target = target;
            _vpSize = vpSize;
            _vpDistance = vpDistance;
            _resolution = resolution;
        }
    }

    /**
     * A combination of pixel sampling and multithreading.
     */
    enum SamplingConfiguration {
        /** 3x3 jittered super-sampling on one thread */
        JITTERED_3X3_SINGLE_THREAD(3, 0, 0),
        /** 3x3 jittered super-sampling on all cores but two */
        JITTERED_3X3_MULTITHREADED(3, 0, -2),
        /** 5x5 jittered super-sampling on all cores but two */
        JITTERED_5X5_MULTITHREADED(5, 0, -2),
        /** Adaptive super-sampling up to depth 3 (5 to 41 rays per pixel) on all cores but two */
        ADAPTIVE_MULTITHREADED(1, 3, -2);

        /** Jittered samples along each pixel axis */
        final int _antiAliasing;
        /** Maximum depth of adaptive super-sampling (0 disables it) */
        final int _adaptiveDepth;
        /** The multithreading mode */
        final int _threads;

        /**
         * Creates a sampling configuration.
         *
         * @param antiAliasing  jittered samples along each pixel axis
         * @param adaptiveDepth the maximum depth of adaptive super-sampling
         * @param threads       the multithreading mode
         */
        SamplingConfiguration(int antiAliasing, int adaptiveDepth, int threads) {
            _antiAliasing = antiAliasing;
            _adaptiveDepth = adaptiveDepth;
            _threads = threads;
        }
    }

    /**
     * Returns every scene with every acceleration structure.
     *
     * @return every scene with every acceleration structure
     */
    static Stream<Arguments> accelerationBenchmarks() {
        return Arrays.stream(BenchmarkScene.values())
                .flatMap(scene -> Arrays.stream(RayTracerType.values()).map(type -> Arguments.of(scene, type)));
    }

    /**
     * Renders a scene with an acceleration structure, on one thread and with one ray per pixel.
     *
     * @param scene the scene
     * @param type  the acceleration structure
     */
    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource("accelerationBenchmarks")
    void testAccelerationStructures(BenchmarkScene scene, RayTracerType type) {
        benchmark(scene, type, 1, 0, 0, scene + "_" + type);
    }

    /**
     * Renders the teapot scene with the octree and a sampling configuration.
     *
     * @param configuration the sampling and multithreading configuration
     */
    @ParameterizedTest(name = "{0}")
    @EnumSource(SamplingConfiguration.class)
    void testSamplingAndMultithreading(SamplingConfiguration configuration) {
        benchmark(BenchmarkScene.TEAPOT, RayTracerType.ADAPTIVE_GRID, configuration._antiAliasing,
                configuration._adaptiveDepth, configuration._threads, "TEAPOT_" + configuration);
    }

    /**
     * Renders a benchmark scene, prints the render time and saves the image.
     *
     * @param benchmark     the scene
     * @param type          the acceleration structure
     * @param antiAliasing  jittered samples along each pixel axis
     * @param adaptiveDepth the maximum depth of adaptive super-sampling (0 disables it)
     * @param threads       the multithreading mode
     * @param name          the benchmark name, printed and used for the image file name
     */
    private static void benchmark(BenchmarkScene benchmark, RayTracerType type,
                                  int antiAliasing, int adaptiveDepth, int threads, String name) {
        Camera camera = Camera.getBuilder()
                .setRayTracer(benchmark._scene.get(), type)
                .setGridResolution(GRID_RESOLUTION)
                .setAdaptiveGridParameters(OCTREE_CAPACITY, OCTREE_DEPTH)
                .setLocation(benchmark._location).setDirection(benchmark._target)
                .setVpSize(benchmark._vpSize, benchmark._vpSize).setVpDistance(benchmark._vpDistance)
                .setResolution(benchmark._resolution, benchmark._resolution)
                .setAntiAliasing(antiAliasing, new Jittered())
                .setAdaptiveSuperSampling(adaptiveDepth, ADAPTIVE_TOLERANCE)
                .setMultithreading(threads)
                .build();

        long start = System.nanoTime();
        camera.renderImage();
        System.out.printf("%-40s %8.2f s%n", name, (System.nanoTime() - start) / 1e9);
        camera.writeToImage("Benchmark_" + name);
    }

    /**
     * Returns the Utah teapot with a cone of transparent, reflective bubbles.
     *
     * @return the Utah teapot with a cone of transparent, reflective bubbles
     */
    private static Scene teapot() {
        Scene scene = new Scene("Teapot");
        scene.geometries.add(new ObjParser()
                .setEmission(new Color(200, 0, 0))
                .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(60))
                .parse("models/teapot.obj"));

        Random random = new Random(5786);
        Material bubble = new Material().setKs(0.1).setKr(0.2).setKt(0.1);
        for (int i = 0; i < 400; i++) {
            double height = random.nextDouble() * 70;
            double distance = random.nextDouble() * height * 0.3;
            double angle = random.nextDouble() * 2 * Math.PI;
            Point center = new Point(90 + distance * Math.cos(angle), 30 + height, 0.2 + distance * Math.sin(angle));
            scene.geometries.add(new Sphere(center, 0.1 + random.nextDouble() * 0.9).setMaterial(bubble));
        }
        scene.lights.add(new PointLight(new Color(500, 500, 500), new Point(100, 0, -100)).setKq(0.000001));
        return scene;
    }

    /**
     * Returns the laptop on a glass table; skips the benchmark if the model, which is not part
     * of the repository, is missing.
     *
     * @return the laptop on a glass table
     */
    private static Scene laptop() {
        assumeTrue(new File(LAPTOP_MODEL).isFile(), "Model not found: " + LAPTOP_MODEL);
        Scene scene = new Scene("Laptop")
                .setBackground(new Color(10, 15, 25))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(10)));
        scene.lights.add(new DirectionalLight(new Color(255, 245, 230).reduce(5), new Vector(1, -1, 1)));
        scene.lights.add(new PointLight(new Color(150, 150, 200), new Point(0, 15, 0)).setKl(0.01).setKq(0.001));
        scene.geometries.add(new ObjParser().setPlanarUvMaterials("Display").parse(LAPTOP_MODEL));
        scene.geometries.add(new Polygon(new Point(-7, 0.01, 4.33), new Point(7, 0.01, 4.33),
                new Point(7, 0.01, -5.67), new Point(-7, 0.01, -5.67))
                .setEmission(new Color(5, 10, 15))
                .setMaterial(new Material().setKd(0.05).setKs(0.8).setShininess(100).setKt(0.7).setKr(0.2)));
        return scene;
    }
}
