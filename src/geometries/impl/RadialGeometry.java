package geometries.impl;

import geometries.api.Geometry;

/**
 * Abstract class - A simple representation of geometry objects with a radius
 * @author itzik123
 */
public abstract class RadialGeometry extends Geometry {

    /** the radius of the object */
    protected double _radius;

    /** the radius of the object squared */
    protected double _radiusSquared;

    /**
     * constructor of a simple object with a radius
     * @param radius of the object
     */
    public RadialGeometry(double radius){
        _radius = radius;
        _radiusSquared = _radius * _radius;
    }
}
