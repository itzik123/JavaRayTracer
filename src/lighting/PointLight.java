package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a point light source - omnidirectional light from a specific point.
 * @outhor ityy35-bit
 */
public class PointLight extends Light implements LightSource {

    protected final Point _position;

    private double _kC = 1.0;
    private double _kL = 0.0;
    private double _kQ = 0.0;

    public PointLight(Color intensity, Point position) {
        super(intensity);
        _position = position;
    }

    // Setters in Builder pattern style
    public PointLight setKc(double kC) {
        _kC = kC;
        return this;
    }

    public PointLight setKl(double kL) {
        _kL = kL;
        return this;
    }

    public PointLight setKq(double kQ) {
        _kQ = kQ;
        return this;
    }

    @Override
    public Color getIntensity(Point p) {
        double distance = _position.distance(p);
        double distanceSquared = _position.distanceSquared(p);

        double attenuation = 1.0 / (_kC + _kL * distance + _kQ * distanceSquared);
        return super.getIntensity().scale(attenuation);
    }

    @Override
    public Vector getL(Point p) {
        return p.subtract(_position).normalize();
    }

    @Override
    public double getDistance(Point p){
        return p.distance(_position);
    }
}