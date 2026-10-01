package renderer.postprocessing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Random;

/**
 * Unit tests for {@link Pixels}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Pixels#rgb(int, int, int)}</li>
 * <li>{@link Pixels#luma(int)}</li>
 * <li>{@link Pixels#boxBlur(int[], int, int, int)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class PixelsTest {

    /** Default constructor to satisfy JavaDoc generator */
    PixelsTest() { /* to satisfy JavaDoc generator */ }

    /** Delta value for accuracy when comparing float values */
    private static final double DELTA = 1e-5;

    /**
     * Test method for {@link Pixels#rgb(int, int, int)}.
     */
    @Test
    void testRgb() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Channels in range are packed
        assertEquals(0x0A141E, Pixels.rgb(10, 20, 30), "Wrong packed pixel");

        // =============== Boundary Values Tests ==================

        // TC11: Channels out of range are clamped
        assertEquals(0xFF00FF, Pixels.rgb(300, -5, 255), "Channels were not clamped");
    }

    /**
     * Test method for {@link Pixels#luma(int)}.
     */
    @Test
    void testLuma() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Green is perceived brighter than red, and red brighter than blue
        float red = Pixels.luma(0xFF0000);
        float green = Pixels.luma(0x00FF00);
        float blue = Pixels.luma(0x0000FF);
        assertTrue(green > red, "Green is not brighter than red");
        assertTrue(red > blue, "Red is not brighter than blue");

        // =============== Boundary Values Tests ==================

        // TC11: Black and white
        assertEquals(0, Pixels.luma(0x000000), DELTA, "Black is not dark");
        assertEquals(1, Pixels.luma(0xFFFFFF), DELTA, "White is not bright");
    }

    /**
     * Test method for {@link Pixels#boxBlur(int[], int, int, int)}.
     */
    @Test
    void testBoxBlur() {
        Random random = new Random(42);
        int width = 23;
        int height = 17;
        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) pixels[i] = random.nextInt(0x1000000);

        // ============ Equivalence Partitions Tests ==============

        // TC01: The sliding-window blur matches a direct average, for several radii
        for (int radius : new int[]{1, 3, 8})
            assertArrayEquals(naiveBlur(pixels, width, height, radius), Pixels.boxBlur(pixels, width, height, radius),
                    "Wrong blur with radius " + radius);

        // =============== Boundary Values Tests ==================

        // TC11: Radius 0 leaves the image unchanged
        assertArrayEquals(pixels, Pixels.boxBlur(pixels, width, height, 0), "Radius 0 changed the image");

        // TC12: A radius larger than the image
        assertArrayEquals(naiveBlur(pixels, width, height, 50), Pixels.boxBlur(pixels, width, height, 50),
                "Wrong blur with a radius larger than the image");
    }

    /**
     * Blurs an image by directly averaging every window: a horizontal pass, then a vertical pass.
     *
     * @param pixels the source pixels
     * @param width  the image width
     * @param height the image height
     * @param radius the blur radius
     * @return the blurred pixels
     */
    private static int[] naiveBlur(int[] pixels, int width, int height, int radius) {
        int[] horizontal = new int[pixels.length];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                horizontal[y * width + x] = average(pixels, width, height, x, y, radius, true);
        int[] result = new int[pixels.length];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                result[y * width + x] = average(horizontal, width, height, x, y, radius, false);
        return result;
    }

    /**
     * Averages the pixels within a radius along one axis.
     *
     * @param pixels     the pixels
     * @param width      the image width
     * @param height     the image height
     * @param x          the column
     * @param y          the row
     * @param radius     the radius
     * @param horizontal {@code true} to average along the row, {@code false} along the column
     * @return the average pixel
     */
    private static int average(int[] pixels, int width, int height, int x, int y, int radius, boolean horizontal) {
        int r = 0, g = 0, b = 0, count = 0;
        for (int i = -radius; i <= radius; i++) {
            int px = horizontal ? x + i : x;
            int py = horizontal ? y : y + i;
            if (px < 0 || px >= width || py < 0 || py >= height) continue;
            int p = pixels[py * width + px];
            r += Pixels.red(p);
            g += Pixels.green(p);
            b += Pixels.blue(p);
            count++;
        }
        return r / count << 16 | g / count << 8 | b / count;
    }
}
