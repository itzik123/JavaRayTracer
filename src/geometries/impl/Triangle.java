package geometries.impl;

import primitives.Point;

/**
 * Represents a triangle in 3D space, which is a specific type of polygon with 3 vertices.
 * @author itzik123
 */
public class Triangle extends Polygon {

    /**
     * Constructor to initialize a triangle with 3 points.
     * @param p1 the first point
     * @param p2 the second point
     * @param p3 the third point
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
    }
}