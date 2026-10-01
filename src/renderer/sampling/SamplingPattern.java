package renderer.sampling;

import java.util.List;

/**
 * Strategy for distributing sample points over a rectangular area, used for super-sampling.
 *
 * @author itzik123
 */
@FunctionalInterface
public interface SamplingPattern {
    /**
     * Generates {@code resolution x resolution} sample offsets over an area centered at the origin.
     *
     * @param resolution the number of samples along each axis
     * @param width      the width of the area
     * @param height     the height of the area
     * @return the sample offsets relative to the area center
     */
    List<Point2D> generatePoints(int resolution, double width, double height);
}
