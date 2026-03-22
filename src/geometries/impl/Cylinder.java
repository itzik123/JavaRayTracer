package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.Objects;

import static primitives.Util.isZero;

/**
 * Represents a finite cylinder in 3D space, which is a tube with a specific height.
 * @author ityy35-bit
 */
public class Cylinder extends Tube {

    /**
     * The height of the cylinder.
     */
    private final double _height;

    /**
     * Constructor to initialize a cylinder with a radius, central axis ray, and height.
     * @param radius the radius of the cylinder
     * @param axis   the central axis ray of the cylinder
     * @param height the height of the cylinder
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        _height = height;
    }

    @Override
    public Vector getNormal(Point point) {
        if (point == _axis.getOrigin()){
            return _axis.getDirection().scale(-1);
        } else if (point == _axis.getOrigin().add(_axis.getDirection().scale(_height))) {
            return _axis.getDirection();
        } else if (isZero(point.subtract(_axis.getOrigin()).dotProduct(_axis.getDirection()))) {
            return _axis.getDirection().scale(-1);
        } else if (isZero(point.subtract(_axis.getOrigin().add(_axis.getDirection().scale(_height))).dotProduct(_axis.getDirection()))) {
            return _axis.getDirection();
        }
        return super.getNormal(point);
    }

    @Override
    public String toString() {
        return "Cylinder: height " + _height + " " + super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cylinder other = (Cylinder) obj;
        return super.equals(other) && isZero(_height - other._height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _height);
    }
}