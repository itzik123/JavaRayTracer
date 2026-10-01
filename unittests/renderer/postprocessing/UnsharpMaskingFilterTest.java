package renderer.postprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static renderer.postprocessing.TestImages.gray;
import static renderer.postprocessing.TestImages.image;
import static renderer.postprocessing.TestImages.rgbAt;
import static renderer.postprocessing.TestImages.samePixels;
import static renderer.postprocessing.TestImages.uniform;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

/**
 * Unit tests for {@link UnsharpMaskingFilter}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link UnsharpMaskingFilter#apply(BufferedImage)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class UnsharpMaskingFilterTest {

    /** Default constructor to satisfy JavaDoc generator */
    UnsharpMaskingFilterTest() { /* to satisfy JavaDoc generator */ }

    /** The image width */
    private static final int WIDTH = 10;
    /** A soft vertical edge between dark gray (left) and light gray (right) */
    private static final BufferedImage EDGE = image(WIDTH, 4, (x, y) -> gray(x < WIDTH / 2 ? 80 : 170));

    /**
     * Test method for {@link UnsharpMaskingFilter#apply(BufferedImage)}.
     */
    @Test
    void testApply() {
        BufferedImage result = new UnsharpMaskingFilter().setBlurRadius(2).setAmount(1).setThreshold(0).apply(EDGE);

        // ============ Equivalence Partitions Tests ==============

        // TC01: The contrast across the edge increases
        int darkSide = Pixels.red(rgbAt(result, WIDTH / 2 - 1, 1));
        int lightSide = Pixels.red(rgbAt(result, WIDTH / 2, 1));
        assertTrue(darkSide < 80 && lightSide > 170, "The edge was not sharpened");

        // =============== Boundary Values Tests ==================

        // TC11: A uniform image is unchanged
        BufferedImage flat = uniform(WIDTH, WIDTH, gray(100));
        assertTrue(samePixels(flat, new UnsharpMaskingFilter().apply(flat)), "Sharpening changed a uniform image");

        // TC12: A threshold above the edge contrast leaves the image unchanged
        assertTrue(samePixels(EDGE, new UnsharpMaskingFilter().setThreshold(255).apply(EDGE)),
                "Sharpening ignored the threshold");
    }
}
