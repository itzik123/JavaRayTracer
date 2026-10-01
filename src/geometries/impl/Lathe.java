package geometries.impl;

import static primitives.Util.isZero;

import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;

import java.util.List;

/**
 * Builds surfaces of revolution - vases, goblets, chess pieces - by spinning a profile curve
 * around an axis, as on a lathe.
 * <p>
 * The profile is a polyline of (radius, height) points. It becomes a mesh of
 * {@link SmoothTriangle}s whose normals are taken from the profile itself, so curved parts render
 * perfectly smooth, while corners sharper than {@value #CREASE_ANGLE} degrees stay crisp.
 *
 * @author itzik123
 */
public class Lathe {

    /**
     * A point of the profile.
     *
     * @param radius the distance from the axis
     * @param height the height along the axis
     */
    public record ProfilePoint(double radius, double height) {
    }

    /** Profile segments meeting at a sharper angle than this, in degrees, form a crease. */
    private static final double CREASE_ANGLE = 40;
    /** The cosine of {@link #CREASE_ANGLE}. */
    private static final double CREASE_COS = Math.cos(Math.toRadians(CREASE_ANGLE));

    /** The profile, from one end of the surface to the other. */
    private final List<ProfilePoint> _profile;
    /** The number of segments around the axis. */
    private int _segments = 64;
    /** The emission of the surface. */
    private Color _emission = Color.BLACK;
    /** The material of the surface. */
    private Material _material = new Material();

    /**
     * Creates a lathe for a profile. For outward-facing normals, list the points so that the
     * surface lies to their right when the axis points up - e.g. from the bottom up along the
     * outside of a vase.
     *
     * @param profile the profile points
     * @throws IllegalArgumentException if there are fewer than two points, a radius is negative
     *                                  or two consecutive points coincide
     */
    public Lathe(List<ProfilePoint> profile) {
        if (profile.size() < 2) throw new IllegalArgumentException("A profile needs at least two points");
        for (int i = 0; i < profile.size(); i++) {
            if (profile.get(i).radius() < 0) throw new IllegalArgumentException("Profile radii must be non-negative");
            if (i > 0 && profile.get(i).equals(profile.get(i - 1)))
                throw new IllegalArgumentException("Consecutive profile points must differ");
        }
        _profile = List.copyOf(profile);
    }

    /**
     * Sets the number of segments around the axis.
     *
     * @param segments the number of segments around the axis
     * @return this lathe
     * @throws IllegalArgumentException if there are fewer than three segments
     */
    public Lathe setSegments(int segments) {
        if (segments < 3) throw new IllegalArgumentException("At least three segments are required");
        _segments = segments;
        return this;
    }

    /**
     * Sets the emission of the surface.
     *
     * @param emission the emission of the surface
     * @return this lathe
     */
    public Lathe setEmission(Color emission) {
        _emission = emission;
        return this;
    }

    /**
     * Sets the material of the surface.
     *
     * @param material the material of the surface
     * @return this lathe
     */
    public Lathe setMaterial(Material material) {
        _material = material;
        return this;
    }

    /**
     * Spins the profile around an axis.
     *
     * @param base the point of the axis at profile height 0
     * @param axis the direction of the axis - the "up" of the shape
     * @return the surface, as a mesh of smooth triangles
     */
    public Geometries revolve(Point base, Vector axis) {
        Frame frame = Frame.of(base, axis.normalize());
        int n = _profile.size();

        // The 2D normal of each profile segment, pointing to its right: (dh, -dr)
        double[][] normals = new double[n - 1][];
        for (int i = 0; i < n - 1; i++) {
            double dr = _profile.get(i + 1).radius() - _profile.get(i).radius();
            double dh = _profile.get(i + 1).height() - _profile.get(i).height();
            double length = Math.hypot(dr, dh);
            normals[i] = new double[]{dh / length, -dr / length};
        }

        Geometries mesh = new Geometries();
        for (int i = 0; i < n - 1; i++) {
            ProfilePoint a = _profile.get(i), b = _profile.get(i + 1);
            boolean aOnAxis = isZero(a.radius()), bOnAxis = isZero(b.radius());
            if (aOnAxis && bOnAxis) continue;
            // Each end shares its normal with the neighbouring segment, unless the corner is a crease
            double[] normalA = i > 0 ? blend(normals[i], normals[i - 1]) : normals[i];
            double[] normalB = i < n - 2 ? blend(normals[i], normals[i + 1]) : normals[i];
            for (int j = 0; j < _segments; j++) {
                double angle1 = 2 * Math.PI * j / _segments, angle2 = 2 * Math.PI * (j + 1) / _segments;
                Point a1 = frame.point(a, angle1), a2 = frame.point(a, angle2);
                Point b1 = frame.point(b, angle1), b2 = frame.point(b, angle2);
                Vector na1 = frame.normal(normalA, angle1), na2 = frame.normal(normalA, angle2);
                Vector nb1 = frame.normal(normalB, angle1), nb2 = frame.normal(normalB, angle2);
                // A profile point on the axis collapses its edge of the quad, leaving one triangle
                if (!aOnAxis) mesh.add(face(a1, a2, b2, na1, na2, nb2));
                if (!bOnAxis) mesh.add(face(a1, b2, b1, na1, nb2, nb1));
            }
        }
        return mesh;
    }

    /**
     * Averages the normal of a profile segment with that of its neighbour, unless they meet at a
     * crease.
     *
     * @param own       the normal of the segment
     * @param neighbour the normal of the adjacent segment
     * @return the shared normal, or the segment's own normal at a crease
     */
    private static double[] blend(double[] own, double[] neighbour) {
        if (own[0] * neighbour[0] + own[1] * neighbour[1] < CREASE_COS) return own;
        double r = own[0] + neighbour[0], h = own[1] + neighbour[1];
        double length = Math.hypot(r, h);
        return new double[]{r / length, h / length};
    }

    /**
     * Creates one triangle of the surface.
     *
     * @param p1 the first vertex
     * @param p2 the second vertex
     * @param p3 the third vertex
     * @param n1 the normal at the first vertex
     * @param n2 the normal at the second vertex
     * @param n3 the normal at the third vertex
     * @return the triangle, with the surface emission and material
     */
    private SmoothTriangle face(Point p1, Point p2, Point p3, Vector n1, Vector n2, Vector n3) {
        SmoothTriangle triangle = new SmoothTriangle(p1, p2, p3, n1, n2, n3);
        triangle.setEmission(_emission).setMaterial(_material);
        return triangle;
    }

    /**
     * The coordinate frame of the axis: maps profile coordinates and angles around the axis to
     * space.
     *
     * @param base the point of the axis at height 0
     * @param up   the unit axis
     * @param u    a unit vector orthogonal to the axis, at angle 0
     * @param w    a unit vector orthogonal to both, at angle 90 degrees
     */
    private record Frame(Point base, Vector up, Vector u, Vector w) {

        /**
         * Creates the frame of an axis.
         *
         * @param base the point of the axis at height 0
         * @param up   the unit axis
         * @return the frame
         */
        static Frame of(Point base, Vector up) {
            Vector u = up.orthogonal();
            return new Frame(base, up, u, up.crossProduct(u).normalize());
        }

        /**
         * Maps a profile point, turned by an angle around the axis, to space.
         *
         * @param p     the profile point
         * @param angle the angle around the axis
         * @return the point in space
         */
        Point point(ProfilePoint p, double angle) {
            double c = Math.cos(angle) * p.radius(), s = Math.sin(angle) * p.radius();
            return new Point(
                    base.getX() + up.getX() * p.height() + u.getX() * c + w.getX() * s,
                    base.getY() + up.getY() * p.height() + u.getY() * c + w.getY() * s,
                    base.getZ() + up.getZ() * p.height() + u.getZ() * c + w.getZ() * s);
        }

        /**
         * Maps a 2D profile normal, turned by an angle around the axis, to space.
         *
         * @param normal the profile normal: radial and axial components
         * @param angle  the angle around the axis
         * @return the unit normal in space
         */
        Vector normal(double[] normal, double angle) {
            double c = Math.cos(angle) * normal[0], s = Math.sin(angle) * normal[0];
            return new Vector(
                    up.getX() * normal[1] + u.getX() * c + w.getX() * s,
                    up.getY() * normal[1] + u.getY() * c + w.getY() * s,
                    up.getZ() * normal[1] + u.getZ() * c + w.getZ() * s);
        }
    }
}
