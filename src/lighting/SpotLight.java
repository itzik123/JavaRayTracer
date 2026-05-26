package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static primitives.Util.alignZero;

/**
 * Represents a spotlight - a point light source with a specific direction.
 * @outhor ityy35-bit
 */
public class SpotLight extends PointLight {

    private final Vector _direction;

    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        _direction = direction.normalize();
    }

    @Override
    public SpotLight setKc(double kC) {
        super.setKc(kC);
        return this;
    }

    @Override
    public SpotLight setKl(double kL) {
        super.setKl(kL);
        return this;
    }

    @Override
    public SpotLight setKq(double kQ) {
        super.setKq(kQ);
        return this;
    }

    @Override
    public Color getIntensity(Point p) {
        Color pointIntensity = super.getIntensity(p);

        Vector l = getL(p);
        double dirDotL = alignZero(_direction.dotProduct(l));

        if (dirDotL <= 0) {
            return Color.BLACK;
        }

        return pointIntensity.scale(dirDotL);
    }
}