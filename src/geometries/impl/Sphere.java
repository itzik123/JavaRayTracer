package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.sqrt;
import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a sphere in 3D space.
 */
public class Sphere extends RadialGeometry {

    /** The center point of the sphere */
    private final Point _center;

    /**
     * Constructor to initialize a sphere with a radius and a center point.
     * @param radius the radius of the sphere
     * @param center the center point of the sphere
     */
    public Sphere(Point center, double radius) {
        super(radius);
        _center = center;
    }

    @Override
    public Vector getNormal(Point point) {
        return point.subtract(_center).normalize();
    }

    @Override
    public List<Point> findIntersections(Ray ray){
        if (ray.origin().equals(_center)) {
            return List.of(ray.getPoint(_radius));
        }
        Vector vector = _center.subtract(ray.origin());
        double tm = vector.dotProduct(ray.direction());
        double dSquared = alignZero(vector.lengthSquared() - tm * tm);
        if (alignZero(dSquared - _radiusSquared) >= 0) {
            return null;
        }
        double th = alignZero(sqrt(_radiusSquared - dSquared));
        if (alignZero(tm - th) > 0){
            return List.of(ray.getPoint(tm - th), ray.getPoint(tm + th));
        } else if (alignZero(tm + th) > 0){
            return List.of(ray.getPoint(tm + th));
        }
        return null;
    }

    @Override
    public String toString() {
        return "Sphere: center: " + _center + ", radius: " + _radius;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sphere other = (Sphere) obj;
        return super._radius == other._radius && _center.equals(other._center);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super._radius, _center);
    }
}