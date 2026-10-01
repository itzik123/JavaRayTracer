package geometries.impl;

import static java.lang.Math.sqrt;
import static primitives.Util.alignZero;
import static primitives.Util.isZero;

import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;
import java.util.Objects;

/**
 * An infinite tube, defined by a radius around a central axis.
 *
 * @author ityy35-bit
 */
public class Tube extends RadialGeometry {

    /** The central axis of the tube. */
    protected final Ray _axis;

    /**
     * Creates a tube.
     *
     * @param radius the radius of the tube
     * @param axis   the central axis of the tube
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        _axis = axis;
    }

    /**
     * Calculates the signed distance of a point's projection on the axis from the axis origin.
     *
     * @param point the point
     * @return the axial coordinate of the point
     */
    protected double axialDistance(Point point) {
        return point.equals(_axis.origin()) ? 0 : _axis.direction().dotProduct(point.subtract(_axis.origin()));
    }

    @Override
    public Vector getNormal(Point point) {
        // The normal points from the closest point on the axis to the given point
        return point.subtract(_axis.getPoint(axialDistance(point))).normalize();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Removing the axial component from the ray reduces the problem to a circle in the plane
     * orthogonal to the axis, which gives the quadratic {@code a·t² + b·t + c = 0}. Rays parallel
     * to the axis ({@code a = 0}) and tangent rays are not intersections.
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        Vector v = ray.direction();
        Vector va = _axis.direction();
        double vVa = alignZero(v.dotProduct(va));
        double a = alignZero(1 - vVa * vVa);
        if (isZero(a)) return null;

        double b;
        double c;
        if (ray.origin().equals(_axis.origin())) {
            b = 0;
            c = -_radiusSquared;
        } else {
            Vector deltaP = ray.origin().subtract(_axis.origin());
            double deltaPVa = alignZero(deltaP.dotProduct(va));
            b = alignZero(2 * (alignZero(v.dotProduct(deltaP)) - vVa * deltaPVa));
            c = alignZero(deltaP.lengthSquared() - deltaPVa * deltaPVa - _radiusSquared);
        }

        double discriminant = alignZero(b * b - 4 * a * c);
        if (discriminant <= 0) return null;

        double root = sqrt(discriminant);
        double t1 = alignZero((-b - root) / (2 * a));
        double t2 = alignZero((-b + root) / (2 * a));
        boolean valid1 = t1 > 0 && alignZero(t1 - maxDistance) <= 0;
        boolean valid2 = t2 > 0 && alignZero(t2 - maxDistance) <= 0;

        if (valid1 && valid2) return List.of(new Intersection(this, ray.getPoint(t1)), new Intersection(this, ray.getPoint(t2)));
        if (valid1) return List.of(new Intersection(this, ray.getPoint(t1)));
        if (valid2) return List.of(new Intersection(this, ray.getPoint(t2)));
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code null} - a tube is unbounded
     */
    @Override
    protected AABB createBoundingBox() {
        return null;
    }

    @Override
    public String toString() {
        return "Tube: axis: " + _axis + ", radius: " + _radius;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && _axis.equals(((Tube) obj)._axis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_radius, _axis);
    }
}
