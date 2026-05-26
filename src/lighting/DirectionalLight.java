package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Represents directional light (like the sun) - has direction but no specific position.
 * @outhor itty35-bit
 */
public class DirectionalLight extends Light implements LightSource {

    private final Vector _direction;

    public DirectionalLight(Color intensity, Vector direction) {
        super(intensity);
        _direction = direction.normalize();
    }

    @Override
    public Color getIntensity(Point p) {
        return super.getIntensity();
    }

    @Override
    public Vector getL(Point p) {
        return _direction;
    }
}