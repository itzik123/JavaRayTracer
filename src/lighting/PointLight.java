package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * An omnidirectional light at a position, such as a light bulb.
 * <p>
 * The intensity falls off with the distance {@code d} as {@code 1 / (kC + kL·d + kQ·d²)}.
 *
 * @author ityy35-bit
 */
public class PointLight extends Light implements LightSource {

    /** The position of the light. */
    protected final Point _position;
    /** Constant attenuation factor. */
    private double _kC = 1;
    /** Linear attenuation factor. */
    private double _kL = 0;
    /** Quadratic attenuation factor. */
    private double _kQ = 0;

    /**
     * Creates a point light without attenuation.
     *
     * @param intensity the intensity of the light
     * @param position  the position of the light
     */
    public PointLight(Color intensity, Point position) {
        super(intensity);
        _position = position;
    }

    /**
     * Sets the constant attenuation factor.
     *
     * @param kC the constant attenuation factor
     * @return this light
     */
    public PointLight setKc(double kC) {
        _kC = kC;
        return this;
    }

    /**
     * Sets the linear attenuation factor.
     *
     * @param kL the linear attenuation factor
     * @return this light
     */
    public PointLight setKl(double kL) {
        _kL = kL;
        return this;
    }

    /**
     * Sets the quadratic attenuation factor.
     *
     * @param kQ the quadratic attenuation factor
     * @return this light
     */
    public PointLight setKq(double kQ) {
        _kQ = kQ;
        return this;
    }

    @Override
    public Color getIntensity(Point p) {
        double distanceSquared = _position.distanceSquared(p);
        return _intensity.scale(1 / (_kC + _kL * Math.sqrt(distanceSquared) + _kQ * distanceSquared));
    }

    @Override
    public Vector getL(Point p) {
        return p.subtract(_position).normalize();
    }

    @Override
    public double getDistance(Point p) {
        return _position.distance(p);
    }
}
