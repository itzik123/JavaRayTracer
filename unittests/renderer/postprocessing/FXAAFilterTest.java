package renderer.postprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static renderer.postprocessing.TestImages.gray;
import static renderer.postprocessing.TestImages.image;
import static renderer.postprocessing.TestImages.rgbAt;
import static renderer.postprocessing.TestImages.samePixels;
import static renderer.postprocessing.TestImages.uniform;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

/**
 * Unit tests for {@link FXAAFilter}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link FXAAFilter#apply(BufferedImage)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class FXAAFilterTest {

    /** Default constructor to satisfy JavaDoc generator */
    FXAAFilterTest() { /* to satisfy JavaDoc generator */ }

    /** The image size (both axes) */
    private static final int SIZE = 16;
    /** A staircase edge: white above the line y = x / 3, black below */
    private static final BufferedImage STAIRCASE = image(SIZE, SIZE, (x, y) -> y < x / 3 ? 0xFFFFFF : 0);

    /**
     * Test method for {@link FXAAFilter#apply(BufferedImage)}.
     */
    @Test
    void testApply() {
        BufferedImage result = new FXAAFilter().setJitterStrength(0).apply(STAIRCASE);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Pixels along the staircase are blended into intermediate grays
        boolean blended = false;
        for (int y = 1; y < SIZE - 1; y++)
            for (int x = 1; x < SIZE - 1; x++) {
                int red = Pixels.red(rgbAt(result, x, y));
                blended |= red > 0 && red < 255;
            }
        assertTrue(blended, "No edge pixel was smoothed");

        // TC02: Pixels far from the edge are unchanged
        assertEquals(0, rgbAt(result, 2, SIZE - 3), "A pixel far from the edge changed");

        // =============== Boundary Values Tests ==================

        // TC11: A uniform image is unchanged
        BufferedImage flat = uniform(SIZE, SIZE, gray(128));
        assertTrue(samePixels(flat, new FXAAFilter().apply(flat)), "FXAA changed a uniform image");

        // TC12: The image border is unchanged
        for (int i = 0; i < SIZE; i++) {
            assertEquals(rgbAt(STAIRCASE, i, 0), rgbAt(result, i, 0), "The top border changed");
            assertEquals(rgbAt(STAIRCASE, SIZE - 1, i), rgbAt(result, SIZE - 1, i), "The right border changed");
        }
    }
}
