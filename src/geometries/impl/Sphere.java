package geometries.impl;

import primitives.Point;
import primitives.Vector;
import java.util.Objects;

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