package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static geometries.api.Intersectable.Intersection;

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
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance){
        if (ray.origin().equals(_center)) {
            return List.of(new Intersection(this, ray.getPoint(_radius)));
        }

        Vector u = _center.subtract(ray.origin());
        double tm = alignZero(u.dotProduct(ray.direction()));
        double dSquared = alignZero(u.lengthSquared() - tm * tm);

        if (alignZero(dSquared - _radiusSquared) >= 0) {
            return null;
        }

        double th = alignZero(sqrt(_radiusSquared - dSquared));

        double t1 = alignZero(tm - th);
        double t2 = alignZero(tm + th);

        boolean validT1 = t1 > 0 && alignZero(t1 - maxDistance) <= 0;
        boolean validT2 = t2 > 0 && alignZero(t2 - maxDistance) <= 0;

        if (validT1 && validT2) {
            return List.of(new Intersection(this, ray.getPoint(t1)), new Intersection(this, ray.getPoint(t2)));
        }
        if (validT1) {
            return List.of(new Intersection(this, ray.getPoint(t1)));
        }
        if (validT2) {
            return List.of(new Intersection(this, ray.getPoint(t2)));
        }
        return null;
    }
    @Override
    protected primitives.AABB createBoundingBox() {
        double r = _radius;
        Point center = _center;

        Point min = new Point(center.getX() - r, center.getY() - r, center.getZ() - r);
        Point max = new Point(center.getX() + r, center.getY() + r, center.getZ() + r);

        return new primitives.AABB(min, max);
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