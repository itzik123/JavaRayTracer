package renderer.postprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static renderer.postprocessing.TestImages.gray;
import static renderer.postprocessing.TestImages.image;
import static renderer.postprocessing.TestImages.rgbAt;
import static renderer.postprocessing.TestImages.samePixels;
import static renderer.postprocessing.TestImages.uniform;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

/**
 * Unit tests for {@link ColorGradingFilter}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link ColorGradingFilter#apply(BufferedImage)}</li>
 * <li>{@link ColorGradingFilter#setGamma(float)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class ColorGradingFilterTest {

    /** Default constructor to satisfy JavaDoc generator */
    ColorGradingFilterTest() { /* to satisfy JavaDoc generator */ }

    /** A colorful test image */
    private static final BufferedImage SOURCE = image(8, 8, (x, y) -> Pixels.rgb(30 * x, 30 * y, 200 - 20 * x));

    /**
     * Test method for {@link ColorGradingFilter#apply(BufferedImage)}.
     */
    @Test
    void testApply() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Doubling the exposure doubles dark values
        int exposed = Pixels.red(rgbAt(new ColorGradingFilter().setExposure(2).apply(uniform(1, 1, gray(50))), 0, 0));
        assertEquals(100, exposed, 1, "Exposure did not double the brightness");

        // TC02: Zero saturation gives grayscale
        BufferedImage grayscale = new ColorGradingFilter().setSaturation(0).apply(SOURCE);
        int p = rgbAt(grayscale, 5, 2);
        assertTrue(Pixels.red(p) == Pixels.green(p) && Pixels.green(p) == Pixels.blue(p), "Zero saturation left color");

        // TC03: Gamma above 1 brightens the mid-tones
        int brightened = rgbAt(new ColorGradingFilter().setGamma(2).apply(uniform(1, 1, gray(64))), 0, 0);
        assertTrue(Pixels.red(brightened) > 64, "Gamma above 1 did not brighten the mid-tones");

        // =============== Boundary Values Tests ==================

        // TC11: The default parameters leave the image unchanged
        assertTrue(samePixels(SOURCE, new ColorGradingFilter().apply(SOURCE)), "Default grading changed the image");

        // TC12: Values pushed beyond white are clamped
        int clamped = rgbAt(new ColorGradingFilter().setExposure(10).apply(uniform(1, 1, gray(200))), 0, 0);
        assertEquals(0xFFFFFF, clamped, "Over-exposed values were not clamped to white");
    }

    /**
     * Test method for {@link ColorGradingFilter#setGamma(float)}.
     */
    @Test
    void testSetGamma() {
        // =============== Boundary Values Tests ==================

        // TC11: Zero gamma
        assertThrows(IllegalArgumentException.class, () -> new ColorGradingFilter().setGamma(0), "A zero gamma was accepted");
    }
}
