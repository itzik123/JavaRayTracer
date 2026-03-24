package primitives;

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