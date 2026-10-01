package geometries.impl;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A finite, capped cylinder: a tube cut to a height, closed by two disk bases.
 *
 * @author ityy35-bit
 */
public class Cylinder extends Tube {

    /** The height of the cylinder along its axis. */
    private final double _height;
    /** The plane of the bottom base (through the axis origin). */
    private final Plane _bottomBase;
    /** The plane of the top base. */
    private final Plane _topBase;
    /** The center of the top base. */
    private final Point _topCenter;
    /** The outward normal of the bottom base. */
    private final Vector _bottomNormal;

    /**
     * Creates a cylinder.
     *
     * @param radius the radius of the cylinder
     * @param axis   the axis; its origin is the center of the bottom base
     * @param height the height of the cylinder
     * @throws IllegalArgumentException if the height is not positive
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        if (height <= 0) throw new IllegalArgumentException("Cylinder height must be positive");
        _height = height;
        _topCenter = axis.getPoint(height);
        _bottomBase = new Plane(axis.origin(), axis.direction());
        _topBase = new Plane(_topCenter, axis.direction());
        _bottomNormal = axis.direction().scale(-1);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Points on a base, including its rim, get the base normal; all other points get the side
     * normal of the tube.
     */
    @Override
    public Vector getNormal(Point point) {
        double axial = axialDistance(point);
        if (isZero(axial)) return _bottomNormal;
        if (isZero(axial - _height)) return _axis.direction();
        return super.getNormal(point);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Combines three surfaces: the side of the tube strictly between the bases, and the two
     * disks including their rims (so a hit on a rim is reported once). The result is sorted by
     * distance from the ray origin.
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> intersections = new ArrayList<>(2);

        var sideIntersections = super.calcIntersectionsHelper(ray, maxDistance);
        if (sideIntersections != null)
            for (Intersection intersection : sideIntersections) {
                double axial = alignZero(axialDistance(intersection.point));
                if (axial > 0 && alignZero(axial - _height) < 0) intersections.add(intersection);
            }

        addBaseIntersection(_bottomBase, _axis.origin(), ray, maxDistance, intersections);
        addBaseIntersection(_topBase, _topCenter, ray, maxDistance, intersections);

        if (intersections.isEmpty()) return null;
        Point origin = ray.origin();
        intersections.sort(Comparator.comparingDouble(i -> i.point.distanceSquared(origin)));
        return List.copyOf(intersections);
    }

    /**
     * Adds the intersection of a ray with one base disk, if there is one.
     *
     * @param base          the plane of the base
     * @param center        the center of the base
     * @param ray           the ray
     * @param maxDistance   the maximum distance of an intersection from the ray origin
     * @param intersections the list to add the intersection to
     */
    private void addBaseIntersection(Plane base, Point center, Ray ray, double maxDistance,
                                     List<Intersection> intersections) {
        var planeIntersections = base.calcIntersections(ray, maxDistance);
        if (planeIntersections == null) return;
        Point point = planeIntersections.getFirst().point;
        if (alignZero(point.distanceSquared(center) - _radiusSquared) <= 0)
            intersections.add(new Intersection(this, point));
    }

    /**
     * {@inheritDoc}
     * <p>
     * The two base disks are bounded exactly: along each coordinate axis, a disk with unit normal
     * {@code d} extends {@code r * sqrt(1 - d_i²)} from its center.
     */
    @Override
    protected AABB createBoundingBox() {
        Vector d = _axis.direction();
        Point bottom = _axis.origin();
        double ex = _radius * Math.sqrt(Math.max(0, 1 - d.getX() * d.getX()));
        double ey = _radius * Math.sqrt(Math.max(0, 1 - d.getY() * d.getY()));
        double ez = _radius * Math.sqrt(Math.max(0, 1 - d.getZ() * d.getZ()));
        return new AABB(
                new Point(Math.min(bottom.getX(), _topCenter.getX()) - ex,
                        Math.min(bottom.getY(), _topCenter.getY()) - ey,
                        Math.min(bottom.getZ(), _topCenter.getZ()) - ez),
                new Point(Math.max(bottom.getX(), _topCenter.getX()) + ex,
                        Math.max(bottom.getY(), _topCenter.getY()) + ey,
                        Math.max(bottom.getZ(), _topCenter.getZ()) + ez));
    }

    @Override
    public String toString() {
        return "Cylinder: height " + _height + " " + super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && isZero(_height - ((Cylinder) obj)._height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _height);
    }
}
