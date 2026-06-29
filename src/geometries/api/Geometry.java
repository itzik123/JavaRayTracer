package geometries.api;

import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;

/**
 * Abstract class - A simple representation of geometry objects
 * @author ityy35-bit
 */
public abstract class Geometry extends Intersectable {

    private Color _emission = Color.BLACK;
    private Material _material = new Material();
    /**
     * @param point normal on the point
     * @return null unless there is override
     */
    public abstract Vector getNormal(Point point);

    /**
     * Getter for the emission color of the geometry.
     * @return the emission color
     */
    public Color getEmission(Point point) {
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

    /**
     * Getter for the material of the geometry[cite: 442].
     * @return the material of the geometry
     */
    public Material getMaterial() {
        return _material;
    }

    /**
     * Setter for the material of the geometry.
     * Uses builder pattern / method chaining[cite: 443, 448].
     * @param material the new material
     * @return the current Geometry object for chaining [cite: 448]
     */
    public Geometry setMaterial(Material material) {
        this._material = material;
        return this;
    }
}
