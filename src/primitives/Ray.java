package primitives;

import geometries.api.Intersectable.Intersection;

import java.util.List;
import java.util.Objects;

/**
 * Represents a ray in 3D space, defined by an origin point and a direction vector.
 * @author itzik123
 */
public class Ray {
    /**
     * The origin point of the ray.
     */
    private final Point _origin;

    /**
     * The direction vector of the ray.
     */
    private final Vector _direction;

    /** Constant for shifting the ray origin to prevent self-shadowing/reflections */
    private static final double DELTA = 0.1;

    /**
     * Constructor for a Ray with a slight displacement along the normal
     * to avoid self-intersections (Shadow Acne).
     * * @param head      original starting point of the ray
     * @param direction direction of the ray
     * @param normal    normal vector at the starting point
     */
    public Ray(Point head, Vector direction, Vector normal) {
        Vector epsVector = normal.scale(normal.dotProduct(direction) > 0 ? DELTA : -DELTA);
        this._origin = head.add(epsVector);
        this._direction = direction.normalize();
    }
    /**
     * Constructor to initialize a ray with an origin point and a direction vector.
     * The direction vector is automatically normalized.
     * @param origin    the origin point of the ray
     * @param direction the direction vector of the ray
     */
    public Ray(Point origin, Vector direction) {
        _origin = origin;
        _direction = direction.normalize();
    }

    /**
     * Returns the origin point of the ray.
     * @return the origin point
     */
    public Point origin() {
        return _origin;
    }

    /**
     * Returns the direction vector of the ray.
     * @return the direction vector
     */
    public Vector direction() {
        return _direction;
    }

    /**
     * @param t te scale we add to the origin
     * @return the point in distance of t from the origin in the direction of the ray
     */
    public Point getPoint(double t) {
        try {
            return _origin.add(_direction.scale(t));
        } catch (IllegalArgumentException _) {
            return _origin;
        }
    }

    /**
     * Finds the closest point to the ray origin among a list of points.
     * Maintained for backward compatibility.
     * @param points a list of points
     * @return the closest point, or null if the list is empty/null
     */
    public Point findClosestPoint(List<Point> points) {
        return points == null || points.isEmpty() ? null
                : findClosestIntersection(points.stream()
                .map(p -> new geometries.api.Intersectable.Intersection(null, p))
                .toList()).point;
    }

    /**
     * Finds the closest intersection to the ray origin among a list of intersections.
     * @param intersections a list of intersections
     * @return the closest intersection, or null if the list is empty/null
     */
    public Intersection findClosestIntersection(List<Intersection> intersections) {
        if (intersections == null || intersections.isEmpty()) {
            return null;
        }

        geometries.api.Intersectable.Intersection closest = null;
        double minDistance = Double.MAX_VALUE;

        for (geometries.api.Intersectable.Intersection intersection : intersections) {
            double distance = intersection.point.distanceSquared(this.origin()); // הנחתי שפונקציית הגישה לראש הקרן היא origin()
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
        if (obj == null || getClass() != obj.getClass()) return false;
        Ray other = (Ray) obj;
        return _origin.equals(other._origin) && _direction.equals(other._direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_origin, _direction);
    }
}