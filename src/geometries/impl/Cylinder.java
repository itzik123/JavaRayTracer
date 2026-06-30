package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static geometries.api.Intersectable.Intersection;

import static primitives.Util.alignZero;
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
     * The plane containing the bottom base of the cylinder.
     */
    private final Plane _bottomBase;

    /**
     * The plane containing the top base of the cylinder.
     */
    private final Plane _topBase;

    /**
     * Constructor to initialize a cylinder with a radius, central axis ray, and height.
     * @param radius the radius of the cylinder
     * @param axis   the central axis ray of the cylinder
     * @param height the height of the cylinder
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        _height = height;

        Vector v = axis.direction();
        Point p0 = axis.origin();

        _bottomBase = new Plane(p0, v);

        Point p1 = axis.getPoint(height);
        _topBase = new Plane(p1, v);
    }

    @Override
    public Vector getNormal(Point point) {
        if (point.equals(_axis.origin())){
            return _axis.direction().scale(-1);
        } else if (point.equals(_axis.getPoint(_height))) {
            return _axis.direction();
        } else if (isZero(point.subtract(_axis.origin()).dotProduct(_axis.direction()))) {
            return _axis.direction().scale(-1);
        } else if (isZero(point.subtract(_axis.getPoint(_height)).dotProduct(_axis.direction()))) {
            return _axis.direction();
        }
        return super.getNormal(point);
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> intersections = null;
        List<Intersection> tubeIntersections = super.calcIntersectionsHelper(ray, maxDistance);

        if (tubeIntersections != null) {
            for (Intersection inter : tubeIntersections) {
                Point p = inter.point;
                double t = alignZero(_axis.direction().dotProduct(p.subtract(_axis.origin())));
                if (t > 0 && t < _height) {
                    if (intersections == null) intersections = new ArrayList<>();
                    intersections.add(inter);
                }
                else if (isZero(t) || isZero(t - _height)) {
                    if (!isZero(ray.direction().dotProduct(_axis.direction()))) {
                        if (intersections == null) intersections = new ArrayList<>();
                        intersections.add(inter);
                    }
                }
            }
        }

        List<Point> bottomBaseIntersection = _bottomBase.findIntersections(ray);
        if (bottomBaseIntersection != null) {
            Point p = bottomBaseIntersection.getFirst();
            if (alignZero(p.distanceSquared(_axis.origin()) - _radius * _radius) <= 0 &&
                    alignZero(p.distance(ray.origin()) - maxDistance) <= 0) {
                if (intersections == null) {
                    intersections = new ArrayList<>();
                }
                intersections.add(new Intersection(this, p));
            }
        }

        List<Point> topBaseIntersection = _topBase.findIntersections(ray);
        if (topBaseIntersection != null) {
            Point p = topBaseIntersection.getFirst();
            Point topCenter = _axis.getPoint(_height);
            if (alignZero(p.distanceSquared(topCenter) - _radius * _radius) <= 0 &&
                    alignZero(p.distance(ray.origin()) - maxDistance) <= 0) {
                if (intersections == null) {
                    intersections = new ArrayList<>();
                }
                intersections.add(new Intersection(this, p));
            }
        }

        if (intersections == null) {
            return null;
        }

        if (intersections.size() == 2) {
            Intersection i1 = intersections.get(0);
            Intersection i2 = intersections.get(1);
            if (i1.point.equals(i2.point)){
                return List.of(intersections.getFirst());
            }
            return (alignZero(i1.point.distanceSquared(ray.origin()) - i2.point.distanceSquared(ray.origin())) > 0) ? List.of(i2, i1) : List.of(i1, i2);
        }

        return List.of(intersections.get(0));
    }

    @Override
    protected primitives.AABB createBoundingBox() {
        Point p0 = _axis.origin();
        Point p1 = _axis.getPoint(_height);
        double r = _radius;

        // מציאת ערכי המינימום והמקסימום בין שתי נקודות המרכז, והרחבה ברדיוס
        double minX = Math.min(p0.getX(), p1.getX()) - r;
        double minY = Math.min(p0.getY(), p1.getY()) - r;
        double minZ = Math.min(p0.getZ(), p1.getZ()) - r;

        double maxX = Math.max(p0.getX(), p1.getX()) + r;
        double maxY = Math.max(p0.getY(), p1.getY()) + r;
        double maxZ = Math.max(p0.getZ(), p1.getZ()) + r;

        return new primitives.AABB(new Point(minX, minY, minZ), new Point(maxX, maxY, maxZ));
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