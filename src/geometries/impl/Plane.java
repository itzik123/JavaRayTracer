package geometries.impl;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

import geometries.api.Geometry;
import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;
import java.util.Objects;

/**
 * An infinite plane, defined by a point on it and a unit normal.
 *
 * @author ityy35-bit
 */
public class Plane extends Geometry {

    /** A reference point on the plane. */
    private final Point _point;

    /** The unit normal of the plane. */
    private final Vector _normal;

    /**
     * Creates the plane through three points.
     *
     * @param p1 the first point, used as the reference point
     * @param p2 the second point
     * @param p3 the third point
     * @throws IllegalArgumentException if two points coincide or all three are collinear
     */
    public Plane(Point p1, Point p2, Point p3) {
        _point = p1;
        _normal = p2.subtract(p1).crossProduct(p3.subtract(p1)).normalize();
    }

    /**
     * Creates a plane from a point and a normal.
     *
     * @param point  a point on the plane
     * @param normal a normal to the plane (normalized by the constructor)
     */
    public Plane(Point point, Vector normal) {
        _point = point;
        _normal = normal.normalize();
    }

    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        // A ray starting on the reference point, or parallel to the plane, cannot cross it
        if (_point.equals(ray.origin())) return null;
        double nv = _normal.dotProduct(ray.direction());
        if (isZero(nv)) return null;

        double t = alignZero(_normal.dotProduct(_point.subtract(ray.origin())) / nv);
        return t <= 0 || alignZero(t - maxDistance) > 0 ? null : List.of(new Intersection(this, ray.getPoint(t)));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code null} - a plane is unbounded
     */
    @Override
    protected AABB createBoundingBox() {
        return null;
    }

    @Override
    public String toString() {
        return "Plane: point: " + _point + " normal: " + _normal;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof Plane other && _normal.equals(other._normal) && _point.equals(other._point);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_point, _normal);
    }
}
