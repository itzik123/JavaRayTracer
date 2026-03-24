package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.Objects;

import static primitives.Util.isZero;

/**
 * Represents an infinite tube in 3D space, defined by a radius and a central axis ray.
 * @author ityy35-bit
 */
public class Tube extends RadialGeometry {

    /**
     * The central axis of the tube, represented by a ray.
     */
    protected final Ray _axis;

    /**
     * Constructor to initialize a tube with a given radius and central axis.
     * @param radius the radius of the tube
     * @param axis   the central axis ray of the tube
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        _axis = axis;
    }

    @Override
    public Vector getNormal(Point point) {
        double distance = _axis.direction().dotProduct(point.subtract(_axis.origin()));
        if (isZero(distance)){
            return point.subtract(_axis.origin()).normalize();
        }
        return point.subtract(_axis.origin().add(_axis.direction().scale(distance))).normalize();
    }

    @Override
    public String toString() {
        return "Tube: axis: " + _axis + ", radius: " + _radius;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tube other = (Tube) obj;
        return super.equals(obj) && _axis.equals(other._axis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super._radius, _axis);
    }
}