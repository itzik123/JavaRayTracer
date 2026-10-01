package primitives;

import static primitives.Util.isZero;

import geometries.api.Intersectable.Intersection;

import java.util.List;
import java.util.Objects;

/**
 * An immutable ray: an origin point and a unit direction vector.
 *
 * @author itzik123
 */
public class Ray {
    /**
     * Distance by which secondary rays are pushed off their surface to avoid hitting it again
     * because of floating-point error ("shadow acne").
     */
    private static final double DELTA = 0.1;

    /** The origin of the ray. */
    private final Point _origin;

    /** The unit direction of the ray. */
    private final Vector _direction;

    /**
     * Creates a ray; the direction is normalized.
     *
     * @param origin    the origin of the ray
     * @param direction the direction of the ray
     */
    public Ray(Point origin, Vector direction) {
        _origin = origin;
        _direction = direction.normalize();
    }

    /**
     * Creates a secondary ray leaving a surface. The origin is offset by {@link #DELTA} along the
     * surface normal, towards the side the ray is heading to, so the ray cannot re-hit the
     * surface it starts from.
     *
     * @param head      the point on the surface
     * @param direction the direction of the ray
     * @param normal    the surface normal at {@code head}
     */
    public Ray(Point head, Vector direction, Vector normal) {
        _origin = head.add(normal.scale(normal.dotProduct(direction) > 0 ? DELTA : -DELTA));
        _direction = direction.normalize();
    }

    /**
     * Returns the origin of the ray.
     *
     * @return the origin of the ray
     */
    public Point origin() {
        return _origin;
    }

    /**
     * Returns the unit direction of the ray.
     *
     * @return the unit direction of the ray
     */
    public Vector direction() {
        return _direction;
    }

    /**
     * Calculates the point at a given distance along the ray.
     *
     * @param t the distance from the origin (may be negative)
     * @return {@code origin + t * direction}
     */
    public Point getPoint(double t) {
        return isZero(t) ? _origin : _origin.add(_direction.scale(t));
    }

    /**
     * Finds the point closest to the ray origin.
     *
     * @param points the candidate points
     * @return the closest point, or {@code null} if the list is {@code null} or empty
     */
    public Point findClosestPoint(List<Point> points) {
        return points == null || points.isEmpty() ? null
                : findClosestIntersection(points.stream().map(p -> new Intersection(null, p)).toList()).point;
    }

    /**
     * Finds the intersection closest to the ray origin.
     *
     * @param intersections the candidate intersections
     * @return the closest intersection, or {@code null} if the list is {@code null} or empty
     */
    public Intersection findClosestIntersection(List<Intersection> intersections) {
        if (intersections == null) return null;

        Intersection closest = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for (Intersection intersection : intersections) {
            double distance = intersection.point.distanceSquared(_origin);
            if (distance < minDistance) {
                minDistance = distance;
                closest = intersection;
            }
        }
        return closest;
    }

    @Override
    public String toString() {
        return "Ray: " + _origin + _direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof Ray other && _origin.equals(other._origin) && _direction.equals(other._direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_origin, _direction);
    }
}
