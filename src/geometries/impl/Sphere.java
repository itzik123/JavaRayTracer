package geometries.impl;

import static java.lang.Math.sqrt;
import static primitives.Util.alignZero;

import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;
import java.util.Objects;

/**
 * A sphere, defined by its center and radius.
 *
 * @author itzik123
 */
public class Sphere extends RadialGeometry {

    /** The center of the sphere. */
    private final Point _center;

    /**
     * Creates a sphere.
     *
     * @param center the center of the sphere
     * @param radius the radius of the sphere
     */
    public Sphere(Point center, double radius) {
        super(radius);
        _center = center;
    }

    @Override
    public Vector getNormal(Point point) {
        return point.subtract(_center).normalize();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Projects the center onto the ray ({@code tm}) and uses the distance {@code d} between the
     * center and the ray line: the line hits the sphere at {@code tm ± sqrt(r² - d²)}. Tangent
     * rays are not intersections.
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        // From the center, every direction exits the sphere exactly at distance r
        if (ray.origin().equals(_center))
            return alignZero(_radius - maxDistance) > 0 ? null : List.of(new Intersection(this, ray.getPoint(_radius)));

        Vector u = _center.subtract(ray.origin());
        double tm = alignZero(u.dotProduct(ray.direction()));
        double dSquared = alignZero(u.lengthSquared() - tm * tm);
        double thSquared = alignZero(_radiusSquared - dSquared);
        if (thSquared <= 0) return null;

        double th = alignZero(sqrt(thSquared));
        double t1 = alignZero(tm - th);
        double t2 = alignZero(tm + th);
        boolean valid1 = t1 > 0 && alignZero(t1 - maxDistance) <= 0;
        boolean valid2 = t2 > 0 && alignZero(t2 - maxDistance) <= 0;

        if (valid1 && valid2) return List.of(new Intersection(this, ray.getPoint(t1)), new Intersection(this, ray.getPoint(t2)));
        if (valid1) return List.of(new Intersection(this, ray.getPoint(t1)));
        if (valid2) return List.of(new Intersection(this, ray.getPoint(t2)));
        return null;
    }

    @Override
    protected AABB createBoundingBox() {
        return new AABB(
                new Point(_center.getX() - _radius, _center.getY() - _radius, _center.getZ() - _radius),
                new Point(_center.getX() + _radius, _center.getY() + _radius, _center.getZ() + _radius));
    }

    @Override
    public String toString() {
        return "Sphere: center: " + _center + ", radius: " + _radius;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && _center.equals(((Sphere) obj)._center);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_radius, _center);
    }
}
