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
}
