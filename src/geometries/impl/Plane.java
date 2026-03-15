package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Vector;

import java.util.Objects;

/**
 * Plane class represents the plane witch we will use in polygons
 * @author ityy35-bit
 */
public class Plane extends Geometry {
    /**
     * point on the plane
     */
    private final Point _point;

    /**
     * normal vector to represent the plane
     */
    private final Vector _normal;

    /**
     * Constructor to initialize a plane with a point and a normal vector.
     * @param p1 first point on the plane
     * @param p2 second point on the plane
     * @param p3 third point on the plane
     */
    public Plane(Point p1, Point p2, Point p3){
        _point = p1;
        _normal = null;//will be con...
    }

    /**
     * Constructor to initialize a plane with a point and a normal vector.
     * @param point set the point
     * @param normal set the normal vector
     */
     public Plane(Point point, Vector normal){
        _point = point;
        _normal = normal.normalize();
     }

    /**
     * @param point normal on the point
     * @return the normal
     */
    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }


    @Override
    public String toString() {
        return "Plane: point: " + _point.toString() + " normal: " + _normal.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return _normal.equals(((Plane) obj)._normal) && _point.equals(((Plane) obj)._point);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_point, _normal);
    }


}
