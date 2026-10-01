package geometries.impl;

import primitives.Point;
import primitives.Vector;

/**
 * A triangle with a normal at each vertex, blended across its surface (Phong shading).
 * <p>
 * A mesh of flat triangles shows its facets in every highlight and reflection. Interpolating
 * normals that were taken from the smooth surface the mesh approximates makes shading,
 * reflection and refraction behave as if that surface itself were rendered.
 *
 * @author itzik123
 */
public class SmoothTriangle extends Triangle {

    /** The unit normals at the three vertices. */
    private final Vector _n1, _n2, _n3;

    /**
     * Creates a smooth triangle.
     *
     * @param p1 the first vertex
     * @param p2 the second vertex
     * @param p3 the third vertex
     * @param n1 the surface normal at the first vertex
     * @param n2 the surface normal at the second vertex
     * @param n3 the surface normal at the third vertex
     * @throws IllegalArgumentException if the vertices coincide or are collinear
     */
    public SmoothTriangle(Point p1, Point p2, Point p3, Vector n1, Vector n2, Vector n3) {
        super(p1, p2, p3);
        _n1 = n1.normalize();
        _n2 = n2.normalize();
        _n3 = n3.normalize();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Blends the vertex normals with the barycentric weights of the point.
     */
    @Override
    public Vector getNormal(Point point) {
        Barycentric w = barycentric(point);
        return new Vector(
                w.w1() * _n1.getX() + w.w2() * _n2.getX() + w.w3() * _n3.getX(),
                w.w1() * _n1.getY() + w.w2() * _n2.getY() + w.w3() * _n3.getY(),
                w.w1() * _n1.getZ() + w.w2() * _n2.getZ() + w.w3() * _n3.getZ()).normalize();
    }
}
