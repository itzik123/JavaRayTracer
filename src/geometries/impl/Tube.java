package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;
import java.util.Objects;

import static geometries.api.Intersectable.Intersection;

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
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
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
        }

        double t1 = alignZero((-b - sqrt(discriminant)) / (2 * a));
        double t2 = alignZero((-b + sqrt(discriminant)) / (2 * a));

        boolean validT1 = t1 > 0 && alignZero(t1 - maxDistance) <= 0;
        boolean validT2 = t2 > 0 && alignZero(t2 - maxDistance) <= 0;

        if (validT1 && validT2) {
            return List.of(new Intersection(this, ray.getPoint(t1)), new Intersection(this, ray.getPoint(t2)));
        } else if (validT1) {
            return List.of(new Intersection(this, ray.getPoint(t1)));
        } else if (validT2) {
            return List.of(new Intersection(this, ray.getPoint(t2)));
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