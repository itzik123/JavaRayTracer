package primitives;

import java.util.List;
import static primitives.Util.isZero;

/**
 * Blackboard entity for target area sampling.
 * Generates and manages the 2D offset points for super-sampling beams.
 */
public class TargetArea {

    private double _width = 0d;
    private double _height = 0d;
    private int _resolution = 1;

    // Default pattern can be Jittered, or left null/injected by the builder
    private SamplingPattern _samplingPattern = new Jittered();

    public TargetArea setSize(double width, double height) {
        this._width = width;
        this._height = height;
        return this;
    }

    public TargetArea setResolution(int resolution) {
        this._resolution = resolution;
        return this;
    }

    public TargetArea setSamplingPattern(SamplingPattern samplingPattern) {
        this._samplingPattern = samplingPattern;
        return this;
    }

    /**
     * Retrieves the sample points based on the configured pattern.
     * @return List of 2D points
     */
    public List<Point2D> getSamplePoints() {
        if (_resolution <= 1 || isZero(_width) || isZero(_height) || _samplingPattern == null) {
            return List.of(new Point2D(0, 0));
        }
        return _samplingPattern.generatePoints(_resolution, _width, _height);
    }
}