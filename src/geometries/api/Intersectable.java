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
     * @param ray the ray that interacts with the object
     * @return list of the intersection points
     */
    public abstract List<Point> findIntersections(Ray ray);

    /**
     * Represents an intersection point on a specific geometry.
     * Acts as a Passive Data Structure (PDS).
     */
    public final static class Intersection {

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
