package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * A light at infinity, such as the sun: parallel rays and no attenuation.
 *
 * @author ityy35-bit
 */
public class DirectionalLight extends Light implements LightSource {

    /** The unit direction of the light rays. */
    private final Vector _direction;

    /**
     * Creates a directional light.
     *
     * @param intensity the intensity of the light
     * @param direction the direction of the light rays (normalized by the constructor)
     */
    public DirectionalLight(Color intensity, Vector direction) {
        super(intensity);
        _direction = direction.normalize();
    }

    @Override
    public Color getIntensity(Point p) {
        return _intensity;
    }

    @Override
    public Vector getL(Point p) {
        return _direction;
    }

    @Override
    public double getDistance(Point p) {
        return Double.POSITIVE_INFINITY;
    }
}
