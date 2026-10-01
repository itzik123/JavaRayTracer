package geometries.impl;

import static primitives.Util.alignZero;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * A triangle - the workhorse primitive of every mesh model.
 * <p>
 * Intersections use the Möller-Trumbore algorithm on raw coordinates: it finds the hit distance
 * and the barycentric coordinates in one pass, without computing the supporting plane and without
 * allocating intermediate vectors, which matters for models with hundreds of thousands of faces.
 *
 * @author itzik123
 */
public class Triangle extends Polygon {

    /** The first vertex, from which both edges start. */
    protected final double _v0x, _v0y, _v0z;
    /** The edge from the first to the second vertex. */
    protected final double _e1x, _e1y, _e1z;
    /** The edge from the first to the third vertex. */
    protected final double _e2x, _e2y, _e2z;

    /**
     * Creates a triangle.
     *
     * @param p1 the first vertex
     * @param p2 the second vertex
     * @param p3 the third vertex
     * @throws IllegalArgumentException if the vertices coincide or are collinear
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
        _v0x = p1.getX();
        _v0y = p1.getY();
        _v0z = p1.getZ();
        _e1x = p2.getX() - _v0x;
        _e1y = p2.getY() - _v0y;
        _e1z = p2.getZ() - _v0z;
        _e2x = p3.getX() - _v0x;
        _e2y = p3.getY() - _v0y;
        _e2z = p3.getZ() - _v0z;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Hits on an edge or a vertex (a barycentric coordinate equal to 0) are not intersections.
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        Vector dir = ray.direction();
        double dx = dir.getX(), dy = dir.getY(), dz = dir.getZ();

        // p = dir x e2; det = 0 means the ray is parallel to the triangle plane
        double px = dy * _e2z - dz * _e2y;
        double py = dz * _e2x - dx * _e2z;
        double pz = dx * _e2y - dy * _e2x;
        double det = alignZero(_e1x * px + _e1y * py + _e1z * pz);
        if (det == 0) return null;

        // s = origin - v0; u = barycentric weight of the second vertex
        Point origin = ray.origin();
        double sx = origin.getX() - _v0x, sy = origin.getY() - _v0y, sz = origin.getZ() - _v0z;
        double u = alignZero((sx * px + sy * py + sz * pz) / det);
        if (u <= 0 || u >= 1) return null;

        // q = s x e1; v = barycentric weight of the third vertex
        double qx = sy * _e1z - sz * _e1y;
        double qy = sz * _e1x - sx * _e1z;
        double qz = sx * _e1y - sy * _e1x;
        double v = alignZero((dx * qx + dy * qy + dz * qz) / det);
        if (v <= 0 || u + v >= 1) return null;

        double t = alignZero((_e2x * qx + _e2y * qy + _e2z * qz) / det);
        return t > 0 && alignZero(t - maxDistance) <= 0 ? List.of(new Intersection(this, ray.getPoint(t))) : null;
    }

    /**
     * The barycentric coordinates of a point: the weights of the three vertices that blend into
     * the point, used to interpolate per-vertex data across the triangle.
     *
     * @param w1 the weight of the first vertex
     * @param w2 the weight of the second vertex
     * @param w3 the weight of the third vertex
     */
    protected record Barycentric(double w1, double w2, double w3) {
    }

    /**
     * Calculates the barycentric coordinates of a point on the triangle, by solving
     * {@code point = v0 + w2·e1 + w3·e2} in the least-squares sense.
     *
     * @param point a point on the triangle
     * @return the barycentric coordinates of the point
     */
    protected Barycentric barycentric(Point point) {
        double px = point.getX() - _v0x, py = point.getY() - _v0y, pz = point.getZ() - _v0z;
        double d11 = _e1x * _e1x + _e1y * _e1y + _e1z * _e1z;
        double d12 = _e1x * _e2x + _e1y * _e2y + _e1z * _e2z;
        double d22 = _e2x * _e2x + _e2y * _e2y + _e2z * _e2z;
        double dp1 = px * _e1x + py * _e1y + pz * _e1z;
        double dp2 = px * _e2x + py * _e2y + pz * _e2z;
        double invDenominator = 1 / (d11 * d22 - d12 * d12);
        double w2 = (d22 * dp1 - d12 * dp2) * invDenominator;
        double w3 = (d11 * dp2 - d12 * dp1) * invDenominator;
        return new Barycentric(1 - w2 - w3, w2, w3);
    }
}
