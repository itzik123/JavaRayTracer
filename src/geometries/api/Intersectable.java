package geometries.api;

import primitives.Point;
import primitives.Ray;

import java.util.List;

/**
 * Abstract class - A simple demand of findIntersections function on all the geometry objects
 * @author itzik123
 */
public abstract class Intersectable {
    /**
     * Helper method to calculate intersections, implemented by subclasses (NVI pattern).
     * @param ray the ray intersecting the geometry
     * @return list of intersections (geometry and point), or null if there are no intersections
     */
    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray);

    /**
     * Calculates intersections of a ray with the geometry.
     * This is the public non-virtual interface method.
     * @param ray the intersecting ray
     * @return list of intersections, or null if there are no intersections
     */
    public final List<Intersection> calcIntersections(Ray ray) {
        return calcIntersectionsHelper(ray);
    }

    /**
     * Finds intersection points of a ray with the geometry.
     * Maintained for backward compatibility.
     * @param ray the ray that interacts with the object
     * @return list of the intersection points, or null if there are no intersections
     */
    public final List<Point> findIntersections(Ray ray) {
        var intersections = calcIntersections(ray);
        return intersections == null ? null
                : intersections.stream()
                .map(intersection -> intersection.point)
                .toList();
    }

    /**
     * Represents an intersection point on a specific geometry.
     * Acts as a Passive Data Structure (PDS).
     */
    public static final class Intersection {

        /** The geometry that was intersected */
        public final Geometry geometry;

        /** The point of intersection */
        public final Point point;

        /**
         * Constructor to initialize an intersection.
         * * @param geometry the intersected geometry
         * @param point    the intersection point
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof Intersection other) {
                return this.geometry == other.geometry && this.point.equals(other.point);
            }
            return false;
        }

        @Override
        public String toString() {
            return "Intersection{" +
                    "geometry=" + geometry +
                    ", point=" + point +
                    '}';
        }
    }
}
