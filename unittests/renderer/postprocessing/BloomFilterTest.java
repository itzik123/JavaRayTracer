package renderer.postprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static renderer.postprocessing.TestImages.gray;
import static renderer.postprocessing.TestImages.image;
import static renderer.postprocessing.TestImages.rgbAt;
import static renderer.postprocessing.TestImages.samePixels;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

/**
 * Unit tests for {@link BloomFilter}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link BloomFilter#apply(BufferedImage)}</li>
 * <li>{@link BloomFilter#setThreshold(float)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class BloomFilterTest {

    /** Default constructor to satisfy JavaDoc generator */
    BloomFilterTest() { /* to satisfy JavaDoc generator */ }

    /** The image size (both axes) */
    private static final int SIZE = 21;
    /** The dark background level */
    private static final int BACKGROUND = 20;
    /** A dark image with one white pixel in the center */
    private static final BufferedImage STAR = image(SIZE, SIZE,
            (x, y) -> x == SIZE / 2 && y == SIZE / 2 ? 0xFFFFFF : gray(BACKGROUND));

    /**
     * Test method for {@link BloomFilter#apply(BufferedImage)}.
     */
    @Test
    void testApply() {
        BufferedImage result = new BloomFilter().setThreshold(0.5f).setBlurRadius(3).setIntensity(1).apply(STAR);

        // ============ Equivalence Partitions Tests ==============

        // TC01: A bright pixel spreads a glow onto its neighbors
        assertTrue(Pixels.red(rgbAt(result, SIZE / 2 + 2, SIZE / 2)) > BACKGROUND, "The bright pixel did not glow");

        // TC02: The glow never darkens a pixel
        for (int y = 0; y < SIZE; y++)
            for (int x = 0; x < SIZE; x++)
                assertTrue(Pixels.red(rgbAt(result, x, y)) >= Pixels.red(rgbAt(STAR, x, y)), "Bloom darkened a pixel");

        // =============== Boundary Values Tests ==================

        // TC11: Pixels beyond the glow radius are unchanged
        assertEquals(gray(BACKGROUND), rgbAt(result, 0, 0), "A pixel beyond the glow radius changed");

        // TC12: An image without bright pixels is unchanged
        BufferedImage dark = image(SIZE, SIZE, (x, y) -> gray(10 * (x % 5)));
        assertTrue(samePixels(dark, new BloomFilter().apply(dark)), "Bloom changed an image without bright pixels");
    }

    /**
     * Test method for {@link BloomFilter#setThreshold(float)}.
     */
    @Test
    void testSetThreshold() {
        // =============== Boundary Values Tests ==================

        // TC11: Threshold 1, which no pixel can exceed
        assertThrows(IllegalArgumentException.class, () -> new BloomFilter().setThreshold(1), "Threshold 1 was accepted");

        // TC12: Negative threshold
        assertThrows(IllegalArgumentException.class, () -> new BloomFilter().setThreshold(-0.1f),
                "A negative threshold was accepted");
    }
}
