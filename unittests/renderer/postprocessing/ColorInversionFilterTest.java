package renderer.postprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static renderer.postprocessing.TestImages.image;
import static renderer.postprocessing.TestImages.rgbAt;
import static renderer.postprocessing.TestImages.samePixels;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

/**
 * Unit tests for {@link ColorInversionFilter}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link ColorInversionFilter#apply(BufferedImage)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class ColorInversionFilterTest {

    /** Default constructor to satisfy JavaDoc generator */
    ColorInversionFilterTest() { /* to satisfy JavaDoc generator */ }

    /** The filter under test */
    private static final ColorInversionFilter FILTER = new ColorInversionFilter();

    /**
     * Test method for {@link ColorInversionFilter#apply(BufferedImage)}.
     */
    @Test
    void testApply() {
        BufferedImage source = image(4, 3, (x, y) -> Pixels.rgb(10 * x, 20 * y, 30));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Every channel is inverted
        assertEquals(Pixels.rgb(255 - 30, 255 - 40, 255 - 30), rgbAt(FILTER.apply(source), 3, 2),
                "Wrong inverted pixel");

        // TC02: Inverting twice restores the image
        assertTrue(samePixels(source, FILTER.apply(FILTER.apply(source))), "Double inversion changed the image");

        // =============== Boundary Values Tests ==================

        // TC11: Black becomes white
        assertEquals(0xFFFFFF, rgbAt(FILTER.apply(TestImages.uniform(1, 1, 0)), 0, 0), "Black did not become white");
    }
}
