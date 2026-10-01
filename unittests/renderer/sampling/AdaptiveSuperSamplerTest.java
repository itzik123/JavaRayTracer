package renderer.sampling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import java.util.function.Function;

/**
 * Unit tests for {@link AdaptiveSuperSampler}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link AdaptiveSuperSampler#sample(Point, double, double, Vector, Vector, int, double, Function)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 * <p>
 * The sampled area is the unit square around the origin on the XY plane, and the color
 * functions count how many samples the sampler takes.
 *
 * @author itzik123
 */
class AdaptiveSuperSamplerTest {

    /** Default constructor to satisfy JavaDoc generator */
    AdaptiveSuperSamplerTest() { /* to satisfy JavaDoc generator */ }

    /** The maximum subdivision depth used in the tests */
    private static final int MAX_DEPTH = 4;
    /** The color tolerance used in the tests */
    private static final double TOLERANCE = 1;
    /** A white color */
    private static final Color WHITE = new Color(255, 255, 255);

    /**
     * Samples the unit square with a color function, counting the samples.
     *
     * @param colorFunction the color of each point
     * @param samples       receives the number of samples taken
     * @return the average color
     */
    private static Color sample(Function<Point, Color> colorFunction, int[] samples) {
        return AdaptiveSuperSampler.sample(Point.ZERO, 1, 1, Vector.AXIS_X, Vector.AXIS_Y, MAX_DEPTH, TOLERANCE,
                p -> {
                    samples[0]++;
                    return colorFunction.apply(p);
                });
    }

    /**
     * Test method for
     * {@link AdaptiveSuperSampler#sample(Point, double, double, Vector, Vector, int, double, Function)}.
     */
    @Test
    void testSample() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A uniform area takes the corners and the center only
        int[] uniformSamples = {0};
        assertEquals(WHITE, sample(p -> WHITE, uniformSamples), "Wrong color of a uniform area");
        assertEquals(5, uniformSamples[0], "A uniform area was subdivided");

        // TC02: An area split by an edge is refined, and its color is a blend of both sides
        int[] edgeSamples = {0};
        Color half = sample(p -> p.getX() > 0.1 ? WHITE : Color.BLACK, edgeSamples);
        assertTrue(edgeSamples[0] > 5, "An area with an edge was not subdivided");
        assertTrue(half.isSimilar(new Color(102, 102, 102), 40), "Wrong blend of an area with an edge: " + half);

        // =============== Boundary Values Tests ==================

        // TC11: Maximum depth 1 never subdivides
        int[] shallowSamples = {0};
        AdaptiveSuperSampler.sample(Point.ZERO, 1, 1, Vector.AXIS_X, Vector.AXIS_Y, 1, TOLERANCE, p -> {
            shallowSamples[0]++;
            return p.getX() > 0 ? WHITE : Color.BLACK;
        });
        assertEquals(5, shallowSamples[0], "Depth 1 subdivided the area");
    }
}
