package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;
import java.util.Objects;

import static java.lang.Math.sqrt;
import static primitives.Util.alignZero;
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
        return point.subtract(_axis.getPoint(distance)).normalize();
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        Vector v = ray.direction();
        Vector vA = _axis.direction();

        double vVa = alignZero(v.dotProduct(vA));

        double a = alignZero(1 - vVa * vVa);

        if (isZero(a)) {
            return null;
        }

        double b = 0;
        double c = 0;

        if (ray.origin().equals(_axis.origin())) {
            b = 0;
            c = alignZero(-_radius * _radius);
        } else {
            Vector deltaP0 = ray.origin().subtract(_axis.origin());

            double deltaP0Va = alignZero(deltaP0.dotProduct(vA));
            double vDeltaP0 = alignZero(v.dotProduct(deltaP0));

            b = alignZero(2 * (vDeltaP0 - vVa * deltaP0Va));
            c = alignZero(deltaP0.lengthSquared() - deltaP0Va * deltaP0Va - _radius * _radius);
        }
        double discriminant = alignZero(b * b - 4 * a * c);
        if (discriminant <= 0) {
            return null;
        } else if (discriminant > 0 && alignZero((- b - sqrt(discriminant)) / (2 * a)) > 0) {
            return List.of(ray.getPoint((0 - b - sqrt(discriminant)) / (2 * a)), ray.getPoint((sqrt(discriminant)- b) / (2 * a)));
        } else if (discriminant > 0 && alignZero((- b + sqrt(discriminant)) / (2 * a)) > 0) {
            return List.of(ray.getPoint((sqrt(discriminant) - b) / (2 * a)));
        }
        return null;
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