package renderer.postprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static renderer.postprocessing.TestImages.gray;
import static renderer.postprocessing.TestImages.rgbAt;
import static renderer.postprocessing.TestImages.uniform;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

/**
 * Unit tests for {@link PostProcessingEngine}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link PostProcessingEngine#process(BufferedImage)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class PostProcessingEngineTest {

    /** Default constructor to satisfy JavaDoc generator */
    PostProcessingEngineTest() { /* to satisfy JavaDoc generator */ }

    /** A mid-gray test image */
    private static final BufferedImage SOURCE = uniform(2, 2, gray(100));
    /** A filter that turns every pixel black */
    private static final PostProcessFilter BLACKEN = new ColorGradingFilter().setExposure(0);

    /**
     * Test method for {@link PostProcessingEngine#process(BufferedImage)}.
     */
    @Test
    void testProcess() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The filters are applied in order - blacken, then invert, gives white
        PostProcessingEngine blackenThenInvert = new PostProcessingEngine().addFilter(BLACKEN).addFilter(new ColorInversionFilter());
        assertEquals(0xFFFFFF, rgbAt(blackenThenInvert.process(SOURCE), 0, 0), "The filters were not applied in order");

        // TC02: The reverse order gives black
        PostProcessingEngine invertThenBlacken = new PostProcessingEngine().addFilter(new ColorInversionFilter()).addFilter(BLACKEN);
        assertEquals(0, rgbAt(invertThenBlacken.process(SOURCE), 0, 0), "The filters were not applied in order");

        // =============== Boundary Values Tests ==================

        // TC11: An empty pipeline returns the image itself
        assertSame(SOURCE, new PostProcessingEngine().process(SOURCE), "An empty pipeline changed the image");
    }
}
