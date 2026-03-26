package geometries.api;

import primitives.Point;
import primitives.Vector;

/**
 * Abstract class - A simple representation of geometry objects
 * @author ityy35-bit
 */
public abstract class Geometry extends Intersectable {
    /**
     * @param point normal on the point
     * @return null unless there is override
     */
    public abstract Vector getNormal(Point point);

}
