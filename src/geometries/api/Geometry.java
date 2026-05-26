package geometries.api;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Abstract class - A simple representation of geometry objects
 * @author ityy35-bit
 */
public abstract class Geometry extends Intersectable {

    private Color _emission = Color.BLACK;

    /**
     * @param point normal on the point
     * @return null unless there is override
     */
    public abstract Vector getNormal(Point point);

    /**
     * Getter for the emission color of the geometry.
     * @return the emission color
     */
    public Color getEmission() {
        return _emission;
    }

    /**
     * Setter for the emission color of the geometry.
     * Uses builder pattern / method chaining.
     * @param emission the new emission color
     * @return the current Geometry object
     */
    public Geometry setEmission(Color emission) {
        this._emission = emission;
        return this;
    }
}
