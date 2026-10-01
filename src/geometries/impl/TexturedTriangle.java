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

    /** Dot products of the edges, precomputed for barycentric coordinates. */
    private final double _d11, _d12, _d22, _invDenominator;

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
        _d11 = _e1x * _e1x + _e1y * _e1y + _e1z * _e1z;
        _d12 = _e1x * _e2x + _e1y * _e2y + _e1z * _e2z;
        _d22 = _e2x * _e2x + _e2y * _e2y + _e2z * _e2z;
        _invDenominator = 1.0 / (_d11 * _d22 - _d12 * _d12);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Solves {@code point = v0 + w2 * e1 + w3 * e2} for the barycentric weights, interpolates the
     * vertex texture coordinates with them and samples the texture.
     */
    @Override
    public Color getEmission(Point point) {
        double px = point.getX() - _v0x, py = point.getY() - _v0y, pz = point.getZ() - _v0z;
        double dp1 = px * _e1x + py * _e1y + pz * _e1z;
        double dp2 = px * _e2x + py * _e2y + pz * _e2z;

        double w2 = (_d22 * dp1 - _d12 * dp2) * _invDenominator;
        double w3 = (_d11 * dp2 - _d12 * dp1) * _invDenominator;
        double w1 = 1 - w2 - w3;

        return _texture.getColor(
                w1 * _uv1.u() + w2 * _uv2.u() + w3 * _uv3.u(),
                w1 * _uv1.v() + w2 * _uv2.v() + w3 * _uv3.v());
    }
}
