package renderer.sampling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Unit tests for {@link TargetArea}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link TargetArea#getSamplePoints()}</li>
 * <li>{@link TargetArea#setResolution(int)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class TargetAreaTest {

    /** Default constructor to satisfy JavaDoc generator */
    TargetAreaTest() { /* to satisfy JavaDoc generator */ }

    /** The single central sample returned when super-sampling is disabled */
    private static final List<Point2D> CENTER_ONLY = List.of(Point2D.ORIGIN);

    /**
     * Test method for {@link TargetArea#getSamplePoints()}.
     */
    @Test
    void testGetSamplePoints() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Super-sampling a non-empty area
        assertEquals(9, new TargetArea().setSize(1, 1).setResolution(3).getSamplePoints().size(),
                "Wrong number of samples");

        // =============== Boundary Values Tests ==================

        // TC11: Resolution 1 samples the center only
        assertEquals(CENTER_ONLY, new TargetArea().setSize(1, 1).setResolution(1).getSamplePoints(),
                "Resolution 1 did not sample the center only");

        // TC12: An empty area samples the center only
        assertEquals(CENTER_ONLY, new TargetArea().setResolution(3).getSamplePoints(),
                "An empty area did not sample the center only");

        // TC13: No sampling pattern samples the center only
        assertEquals(CENTER_ONLY, new TargetArea().setSize(1, 1).setResolution(3).setSamplingPattern(null).getSamplePoints(),
                "A missing pattern did not sample the center only");
    }

    /**
     * Test method for {@link TargetArea#setResolution(int)}.
     */
    @Test
    void testSetResolution() {
        // =============== Boundary Values Tests ==================

        // TC11: Zero resolution
        assertThrows(IllegalArgumentException.class, () -> new TargetArea().setResolution(0),
                "A zero resolution was accepted");
    }
}
