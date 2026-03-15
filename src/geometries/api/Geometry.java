package geometries.api;

import primitives.Point;
import primitives.Vector;

/**
 * Abstract class - simple representation of geometry objects
 * @author ityy35-bit
 */
public abstract class Geometry {
    /**
     * @param point normal on the point
     * @return null unless there is override
     */
    public Vector getNormal(Point point){
        return null;
    }
}
