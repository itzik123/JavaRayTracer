package geometries.impl;

import geometries.api.Geometry;

import java.util.Objects;

import static primitives.Util.isZero;

/**
 * Abstract class - A simple representation of geometry objects with a radius
 * @author itzik123
 */
public abstract class RadialGeometry extends Geometry {

    /** the radius of the object */
    protected final double _radius;

    /** the radius of the object squared */
    protected final double _radiusSquared;

    /**
     * constructor of a simple object with a radius
     * @param radius of the object
     */
    public RadialGeometry(double radius){
        _radius = radius;
        _radiusSquared = _radius * _radius;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RadialGeometry other = (RadialGeometry) obj;
        return isZero(_radius - other._radius);
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
