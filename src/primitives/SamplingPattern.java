package primitives;

import java.util.List;

/**
 * Interface for generating a pattern of 2D sample points.
 */
public interface SamplingPattern {
    List<Point2D> generatePoints(int resolution, double width, double height);
}