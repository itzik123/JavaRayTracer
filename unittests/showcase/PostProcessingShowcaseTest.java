package showcase;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import geometries.impl.Plane;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import renderer.Camera;
import renderer.RayTracerType;
import renderer.postprocessing.BloomFilter;
import renderer.postprocessing.ColorGradingFilter;
import renderer.postprocessing.FXAAFilter;
import renderer.postprocessing.PostProcessFilter;
import renderer.postprocessing.PostProcessingEngine;
import renderer.postprocessing.UnsharpMaskingFilter;
import scene.Scene;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Showcase of the post-processing pipeline: bloom, color grading, FXAA and unsharp masking, on a
 * fresh render and on previously rendered images.
 *
 * @author itzik123
 */
@Showcase
class PostProcessingShowcaseTest {

    /** Default constructor to satisfy JavaDoc generator */
    PostProcessingShowcaseTest() { /* to satisfy JavaDoc generator */ }

    /** The directory of the rendered images */
    private static final File IMAGES = new File("images");

    /**
     * Produce three glossy spheres without bloom, with bloom, and with the full pipeline.
     *
     * @throws IOException if an image cannot be read or written
     */
    @Test
    void testBloomPipeline() throws IOException {
        Scene scene = new Scene("Bloom showcase")
                .setBackground(new Color(10, 10, 15))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(20)));
        Material glossy = new Material().setKd(0.5).setKs(0.8).setShininess(60);
        scene.geometries.add(
                new Plane(new Point(0, -15, 0), Vector.AXIS_Y).setEmission(new Color(5, 5, 5))
                        .setMaterial(new Material().setKd(0.2).setKs(0.5).setShininess(50).setKr(0.5)),
                new Sphere(new Point(-30, 5, -50), 12).setEmission(new Color(200, 50, 50)).setMaterial(glossy),
                new Sphere(new Point(0, 5, -50), 12).setEmission(new Color(50, 200, 50)).setMaterial(glossy),
                new Sphere(new Point(30, 5, -50), 12).setEmission(new Color(50, 50, 200)).setMaterial(glossy));
        // A strong light that burns bright highlights into the spheres
        scene.lights.add(new PointLight(new Color(500, 500, 500), new Point(0, 50, 20)).setKl(0.00001).setKq(0.000005));

        Camera.getBuilder()
                .setLocation(new Point(0, 20, 80)).setDirection(new Point(0, 0, -50))
                .setVpDistance(100).setVpSize(150, 150)
                .setResolution(2000, 2000)
                .setMultithreading(-2)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .build()
                .renderImage()
                .writeToImage("Bloom_01_Off_Fixed");

        // The render is deterministic, so the saved image is filtered instead of rendering it again
        BloomFilter bloom = new BloomFilter().setThreshold(0.65f).setBlurRadius(20).setIntensity(1);
        filterSavedImage("Bloom_01_Off_Fixed", "Bloom_02_On_Fixed", bloom);
        filterSavedImage("Bloom_01_Off_Fixed", "Integration_Showcase_Perfect", bloom,
                new BloomFilter().setThreshold(0.7f).setBlurRadius(20).setIntensity(0.85f),
                new ColorGradingFilter().setContrast(1.05f).setSaturation(1.15f).setGamma(1.2f),
                new FXAAFilter().setJitterStrength(0.08f));
    }

    /**
     * Produce bloom-only, grading-only, FXAA-only and combined versions of a rendered image.
     *
     * @throws IOException if an image cannot be read or written
     */
    @Test
    void testFilterComparison() throws IOException {
        BufferedImage source = readImage("Laptop_Final_Masterpiece2_ASS_AG.png");
        String prefix = "Laptop_Final_Masterpiece_Angle2_ASS_AG_";

        BloomFilter bloom = new BloomFilter().setThreshold(0.4f).setBlurRadius(40).setIntensity(0.9f);
        ColorGradingFilter grading = new ColorGradingFilter().setExposure(1.1f).setContrast(1.15f)
                .setSaturation(1.6f).setGamma(1.2f);
        FXAAFilter fxaa = new FXAAFilter().setJitterStrength(0.12f).setMaxSearchSteps(20);

        writeImage(bloom.apply(source), prefix + "BloomOnly");
        writeImage(grading.apply(source), prefix + "GradedOnly");
        writeImage(fxaa.apply(source), prefix + "FXAAOnly");
        writeImage(new PostProcessingEngine().addFilter(bloom).addFilter(grading).process(source), prefix + "Masterpiece");
    }

    /**
     * Produce an FXAA-smoothed version of the aliased (one ray per pixel) cage render.
     *
     * @throws IOException if an image cannot be read or written
     */
    @Test
    void testFxaaOnAliasedImage() throws IOException {
        BufferedImage source = readImage("AA_Cage_Test_1_Rays.png");
        writeImage(new FXAAFilter().setJitterStrength(0.2f).apply(source), "AA_Cage_Test_1_Rays_FXAA");
    }

    /**
     * Produce a sharpened version of a blurry photograph with two unsharp-masking passes: a wide
     * one for local contrast and a narrow one for fine detail.
     *
     * @throws IOException if an image cannot be read or written
     */
    @Test
    void testUnsharpMasking() throws IOException {
        BufferedImage source = readImage("blurry.jpeg");
        PostProcessingEngine sharpen = new PostProcessingEngine()
                .addFilter(new UnsharpMaskingFilter().setBlurRadius(6).setAmount(0.7f).setThreshold(0))
                .addFilter(new UnsharpMaskingFilter().setBlurRadius(2).setAmount(1.2f).setThreshold(18));
        writeImage(sharpen.process(source), "Sharpened");
    }

    /**
     * Runs a saved image through a pipeline of filters and saves the result.
     *
     * @param sourceName the source image file name, without extension
     * @param targetName the result image file name, without extension
     * @param filters    the filters, in order
     * @throws IOException if an image cannot be read or written
     */
    private static void filterSavedImage(String sourceName, String targetName, PostProcessFilter... filters)
            throws IOException {
        PostProcessingEngine engine = new PostProcessingEngine();
        for (PostProcessFilter filter : filters) engine.addFilter(filter);
        writeImage(engine.process(readImage(sourceName + ".png")), targetName);
    }

    /**
     * Reads an image from the images directory, skipping the test if it does not exist.
     *
     * @param fileName the file name
     * @return the image
     * @throws IOException if the image cannot be read
     */
    private static BufferedImage readImage(String fileName) throws IOException {
        File file = new File(IMAGES, fileName);
        assumeTrue(file.isFile(), "Source image not found: " + file);
        return ImageIO.read(file);
    }

    /**
     * Writes an image to the images directory as PNG.
     *
     * @param image     the image
     * @param imageName the file name, without extension
     * @throws IOException if the image cannot be written
     */
    private static void writeImage(BufferedImage image, String imageName) throws IOException {
        ImageIO.write(image, "png", new File(IMAGES, imageName + ".png"));
    }
}
