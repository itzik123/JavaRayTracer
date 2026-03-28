package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;
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
        if (point.equals(_axis.origin())){
            return _axis.direction().scale(-1);
        } else if (point.equals(_axis.origin().add(_axis.direction().scale(_height)))) {
            return _axis.direction();
        } else if (isZero(point.subtract(_axis.origin()).dotProduct(_axis.direction()))) {
            return _axis.direction().scale(-1);
        } else if (isZero(point.subtract(_axis.origin().add(_axis.direction().scale(_height))).dotProduct(_axis.direction()))) {
            return _axis.direction();
        }
        return super.getNormal(point);
    }

    @Override
    public List<Point> findIntersections(Ray ray){
        return null;
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