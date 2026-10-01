package lighting;

import static primitives.Util.alignZero;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * A point light that shines in one direction, such as a flashlight.
 * <p>
 * On top of the point-light attenuation, the intensity is scaled by
 * {@code max(0, dir·l)^narrowBeam}: points off the axis are dimmer, points behind the light
 * receive nothing, and a larger exponent makes the beam narrower.
 *
 * @author ityy35-bit
 */
public class SpotLight extends PointLight {

    /** The unit direction of the beam axis. */
    private final Vector _direction;
    /** The beam focus exponent (1 for a wide beam). */
    private int _narrowBeam = 1;

    /**
     * Creates a spot light.
     *
     * @param intensity the intensity of the light
     * @param position  the position of the light
     * @param direction the direction of the beam axis (normalized by the constructor)
     */
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

    /**
     * Sets the beam focus exponent; larger values make the beam narrower.
     *
     * @param narrowBeam the beam focus exponent; larger values make the beam narrower
     * @return this light
     * @throws IllegalArgumentException if the exponent is less than 1
     */
    public SpotLight setNarrowBeam(int narrowBeam) {
        if (narrowBeam < 1) throw new IllegalArgumentException("Narrow beam exponent must be at least 1");
        _narrowBeam = narrowBeam;
        return this;
    }

    @Override
    public Color getIntensity(Point p) {
        // Test the sign before raising to the power, or an even exponent would light up
        // the half-space behind the spot
        double cos = alignZero(_direction.dotProduct(getL(p)));
        return cos <= 0 ? Color.BLACK : super.getIntensity(p).scale(Math.pow(cos, _narrowBeam));
    }
}
