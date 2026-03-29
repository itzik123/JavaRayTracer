package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public List<Point> findIntersections(Ray ray) {
        List<Point> intersections = null;
        List<Point> tubeIntersections = super.findIntersections(ray);
        if (tubeIntersections != null) {
            for (Point p : tubeIntersections) {
                double t = alignZero(_axis.direction().dotProduct(p.subtract(_axis.origin())));
                if (t > 0 && t < _height) {
                    if (intersections == null) intersections = new ArrayList<>();
                    intersections.add(p);
                }
                else if (isZero(t) || isZero(t - _height)) {
                    if (!isZero(ray.direction().dotProduct(_axis.direction()))) {
                        if (intersections == null) intersections = new ArrayList<>();
                        intersections.add(p);
                    }
                }
            }
        }

        List<Point> bottomBaseIntersection = _bottomBase.findIntersections(ray);
        if (bottomBaseIntersection != null) {
            Point p = bottomBaseIntersection.getFirst();
            if (alignZero(p.distanceSquared(_axis.origin()) - _radius * _radius) <= 0) {
                if (intersections == null) {
                    intersections = new ArrayList<>();
                }
                intersections.add(p);
            }
        }

        List<Point> topBaseIntersection = _topBase.findIntersections(ray);
        if (topBaseIntersection != null) {
            Point p = topBaseIntersection.getFirst();
            Point topCenter = _axis.getPoint(_height);
            if (alignZero(p.distanceSquared(topCenter) - _radius * _radius) <= 0) {
                if (intersections == null) {
                    intersections = new ArrayList<>();
                }
                intersections.add(p);
            }
        }

        if (intersections == null) {
            return null;
        }

        if (intersections.size() == 2) {
            Point p1 = intersections.get(0);
            Point p2 = intersections.get(1);
            if (p1.equals(p2)){
                return List.of(intersections.getFirst());
            }
            return (alignZero(p1.distanceSquared(ray.origin()) - p2.distanceSquared(ray.origin())) > 0) ? List.of(p2, p1) : List.of(p1, p2);
        }
        return List.of(intersections.get(0));
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