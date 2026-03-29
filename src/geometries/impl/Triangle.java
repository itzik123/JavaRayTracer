package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

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

    @Override
    public List<Point> findIntersections(Ray ray) {
        Vector edge1 = _vertices.get(1).subtract(_vertices.get(0));
        Vector edge2 = _vertices.get(2).subtract(_vertices.get(0));

        try {
            Vector pvec = ray.direction().crossProduct(edge2);
            double det = alignZero(edge1.dotProduct(pvec));
            if (isZero(det)) {
                return null;
            }
            Vector tvec = ray.origin().subtract(_vertices.get(0));
            double u = alignZero(tvec.dotProduct(pvec) / det);
            if (u <= 0 || u >= 1) {
                return null;
            }
            Vector qvec = tvec.crossProduct(edge1);
            double v = alignZero(ray.direction().dotProduct(qvec) / det); // <-- כאן תוקן הבאג
            if (v <= 0 || u + v >= 1) {
                return null;
            }
            double t = alignZero(edge2.dotProduct(qvec) / det);
            return (alignZero(t) > 0) ? List.of(ray.getPoint(t)) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
