package geometries.impl;

import primitives.Color;
import primitives.Point;
import primitives.Texture;
import primitives.UV;

/**
 * A triangle whose emission is looked up in a texture image.
 * <p>
 * Each vertex carries a texture coordinate. For a point on the surface, the barycentric weights
 * of the three vertices are computed and used to interpolate the texture coordinate, and the
 * texel at that coordinate becomes the emission color of the point.
 *
 * @author itzik123
 */
public class TexturedTriangle extends Triangle {

    /** The texture coordinates of the three vertices. */
    private final UV _uv1, _uv2, _uv3;
    /** The texture image. */
    private final Texture _texture;

    /**
     * Creates a textured triangle.
     *
     * @param p1      the first vertex
     * @param p2      the second vertex
     * @param p3      the third vertex
     * @param uv1     the texture coordinate of the first vertex
     * @param uv2     the texture coordinate of the second vertex
     * @param uv3     the texture coordinate of the third vertex
     * @param texture the texture image
     * @throws IllegalArgumentException if the vertices coincide or are collinear
     */
    public TexturedTriangle(Point p1, Point p2, Point p3, UV uv1, UV uv2, UV uv3, Texture texture) {
        super(p1, p2, p3);
        _uv1 = uv1;
        _uv2 = uv2;
        _uv3 = uv3;
        _texture = texture;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Interpolates the vertex texture coordinates with the barycentric weights of the point and
     * samples the texture.
     */
    @Override
    public Color getEmission(Point point) {
        Barycentric w = barycentric(point);
        return _texture.getColor(
                w.w1() * _uv1.u() + w.w2() * _uv2.u() + w.w3() * _uv3.u(),
                w.w1() * _uv1.v() + w.w2() * _uv2.v() + w.w3() * _uv3.v());
    }
}
