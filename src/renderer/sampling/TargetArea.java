package renderer.sampling;

import static primitives.Util.isZero;

import java.util.List;

/**
 * A rectangular sampling area (such as a pixel) together with the pattern and density used to
 * sample it.
 *
 * @author itzik123
 */
public class TargetArea {

    /** The width of the area. */
    private double _width = 0;
    /** The height of the area. */
    private double _height = 0;
    /** The number of samples along each axis. */
    private int _resolution = 1;
    /** The distribution of the samples; {@code null} disables super-sampling. */
    private SamplingPattern _samplingPattern = new Jittered();

    /** Creates an empty target area with a jittered pattern and no super-sampling. */
    public TargetArea() {
    }

    /**
     * Sets the size of the area.
     *
     * @param width  the width of the area
     * @param height the height of the area
     * @return this target area
     */
    public TargetArea setSize(double width, double height) {
        _width = width;
        _height = height;
        return this;
    }

    /**
     * Sets the number of samples along each axis.
     *
     * @param resolution the number of samples along each axis
     * @return this target area
     * @throws IllegalArgumentException if the resolution is not positive
     */
    public TargetArea setResolution(int resolution) {
        if (resolution < 1) throw new IllegalArgumentException("Sampling resolution must be positive");
        _resolution = resolution;
        return this;
    }

    /**
     * Sets the sample distribution, or {@code null} to sample only the center.
     *
     * @param samplingPattern the sample distribution, or {@code null} to sample only the center
     * @return this target area
     */
    public TargetArea setSamplingPattern(SamplingPattern samplingPattern) {
        _samplingPattern = samplingPattern;
        return this;
    }

    /**
     * Generates the sample offsets for one area. Super-sampling is disabled - and only the center
     * is returned - when the resolution is 1, the area is empty or no pattern is set.
     *
     * @return the sample offsets relative to the area center
     */
    public List<Point2D> getSamplePoints() {
        if (_resolution <= 1 || isZero(_width) || isZero(_height) || _samplingPattern == null)
            return List.of(Point2D.ORIGIN);
        return _samplingPattern.generatePoints(_resolution, _width, _height);
    }
}
