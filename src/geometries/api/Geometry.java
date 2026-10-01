package geometries.api;

import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;

/**
 * A single renderable shape with a surface normal, an emission color and a material.
 *
 * @author ityy35-bit
 */
public abstract class Geometry extends Intersectable {

    /** The light emitted by the surface itself. */
    private Color _emission = Color.BLACK;
    /** The optical properties of the surface. */
    private Material _material = new Material();

    /** Creates a geometry with no emission and a default material. */
    protected Geometry() {
    }

    /**
     * Calculates the unit normal to the surface at a point.
     *
     * @param point a point on the surface
     * @return the unit normal at the point
     */
    public abstract Vector getNormal(Point point);

    /**
     * Returns the color the surface emits at a point. Uniform by default; textured geometries
     * override it to look the color up in their texture.
     *
     * @param point a point on the surface
     * @return the emission color at the point
     */
    public Color getEmission(Point point) {
        return _emission;
    }

    /**
     * Sets the uniform emission color.
     *
     * @param emission the uniform emission color
     * @return this geometry
     */
    public Geometry setEmission(Color emission) {
        _emission = emission;
        return this;
    }

    /**
     * Returns the material of the geometry.
     *
     * @return the material of the geometry
     */
    public Material getMaterial() {
        return _material;
    }

    /**
     * Sets the material of the geometry.
     *
     * @param material the material of the geometry
     * @return this geometry
     */
    public Geometry setMaterial(Material material) {
        _material = material;
        return this;
    }
}
