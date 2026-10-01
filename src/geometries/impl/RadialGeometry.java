package geometries.impl;

import static primitives.Util.isZero;

import geometries.api.Geometry;

import java.util.Objects;

/**
 * Base class of geometries defined by a radius.
 *
 * @author itzik123
 */
public abstract class RadialGeometry extends Geometry {

    /** The radius. */
    protected final double _radius;

    /** The radius squared, cached for intersection calculations. */
    protected final double _radiusSquared;

    /**
     * Creates a radial geometry.
     *
     * @param radius the radius
     * @throws IllegalArgumentException if the radius is not positive
     */
    protected RadialGeometry(double radius) {
        if (radius <= 0) throw new IllegalArgumentException("Radius must be positive");
        _radius = radius;
        _radiusSquared = radius * radius;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj != null && getClass() == obj.getClass() && isZero(_radius - ((RadialGeometry) obj)._radius);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_radius);
    }

    @Override
    public String toString() {
        return "radius: " + _radius;
    }
}
