package showcase;

import geometries.impl.Geometries;
import geometries.impl.Lathe;
import geometries.impl.Lathe.ProfilePoint;
import geometries.impl.Polygon;
import primitives.Material;
import primitives.Point;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Staunton chess pieces, turned on a {@link Lathe} from hand-designed profiles, in units where a
 * board square is 100 wide. The knight, the one piece that is not round, is left out.
 *
 * @author itzik123
 */
final class ChessPieces {

    /** Mesh segments around the axis of every piece */
    private static final int SEGMENTS = 96;
    /** The height of the king's body, where the cross stands */
    private static final double KING_HEIGHT = 155;

    /** Utility class - not instantiable. */
    private ChessPieces() {}

    /** The pieces and their profiles. */
    enum Piece {
        /** The king - its cross is added separately */
        KING(new Profile().base(38)
                .spline(21, 34, 18, 52, 16, 70, 15.5, 84)
                .arc(15.5, 86.5, 2.5, -90, 90).to(21, 89.5)
                .arc(21, 91.5, 2, -90, 90).to(15, 93.5)
                .arc(15, 95.5, 2, -90, 90)
                .spline(14, 104, 14.8, 114, 17, 124, 20.5, 132)
                .arc(20.5, 134, 2, -90, 90).to(17, 136.5)
                .spline(16.5, 140, 14, 145.5, 9.5, 149.5, 4.5, 151.5)
                .to(4.5, KING_HEIGHT).to(0, KING_HEIGHT)),
        /** The queen, with a coronet around a ball */
        QUEEN(new Profile().base(36)
                .spline(20, 32, 16.5, 50, 14.5, 66, 14, 76)
                .arc(14, 78.5, 2.5, -90, 90).to(19.5, 81.5)
                .arc(19.5, 83.5, 2, -90, 90).to(14, 85.5)
                .arc(14, 87.5, 2, -90, 90)
                .spline(13, 95, 14, 106, 17, 115, 20, 121)
                .arc(20, 123, 2, -90, 90)
                .spline(21, 127, 22.5, 132)
                .arc(21.5, 132.5, 1.2, -30, 150)
                .spline(17.5, 129, 15, 128.5)
                .spline(12, 131, 8, 136, 4.5, 139)
                .arc(0, 144, 6, -55, 90)),
        /** The bishop, with its mitre */
        BISHOP(new Profile().base(33)
                .spline(17.5, 30, 14.5, 46, 12.5, 60, 12, 68)
                .arc(12, 70.5, 2.5, -90, 90).to(17, 73.5)
                .arc(17, 75.5, 2, -90, 90).to(11, 77.5)
                .spline(12.5, 82, 15, 90, 15.5, 97, 13.5, 105, 9, 112, 3.5, 117)
                .to(2.8, 118.5)
                .arc(0, 122.5, 4.6, -60, 90)),
        /** The rook, a tower with a hollow top */
        ROOK(new Profile().base(35)
                .spline(21.5, 30, 19.5, 45, 18.5, 60, 18.5, 66)
                .arc(18.5, 68.5, 2.5, -90, 90).to(22, 71.5)
                .spline(22.5, 80, 23, 88, 23, 92)
                .arc(21.5, 92, 1.5, 0, 90).to(17, 93.5).to(17, 87).to(0, 87)),
        /** The pawn */
        PAWN(new Profile().base(27)
                .spline(14, 25, 11.5, 34, 9.5, 42)
                .arc(9.5, 44, 2, -90, 90).to(14, 46.5)
                .arc(14, 48, 1.5, -90, 90).to(7, 49.5)
                .spline(6.5, 52, 6.6, 54)
                .arc(0, 63, 11, -55, 90));

        /** The profile, from the center of the base, out and up to the top */
        final List<ProfilePoint> _profile;

        /**
         * Creates a piece.
         *
         * @param profile its profile
         */
        Piece(Profile profile) {
            _profile = profile.points();
        }
    }

    /**
     * Creates a piece.
     *
     * @param piece    the piece
     * @param base     the center of its base
     * @param up       the direction of its axis
     * @param material its material
     * @return the piece
     */
    static Geometries create(Piece piece, Point base, Vector up, Material material) {
        return create(piece, base, up, up.orthogonal(), material);
    }

    /**
     * Creates a piece turned to a given orientation around its axis, so that a piece that moves
     * from frame to frame keeps its orientation.
     *
     * @param piece    the piece
     * @param base     the center of its base
     * @param up       the direction of its axis
     * @param side     a direction orthogonal to the axis, along which the arms of the king's cross
     *                 point
     * @param material its material
     * @return the piece
     */
    static Geometries create(Piece piece, Point base, Vector up, Vector side, Material material) {
        Geometries mesh = new Lathe(piece._profile).setSegments(SEGMENTS).setMaterial(material).revolve(base, up);
        if (piece == Piece.KING) mesh.add(cross(base.add(up.normalize().scale(KING_HEIGHT)), up, side, material));
        return mesh;
    }

    /**
     * Creates the king's cross: a solid plus-shaped prism, as a closed set of outward-facing
     * polygons so that glass refracts correctly through it.
     *
     * @param base     the center of its bottom
     * @param up       the direction of its upright
     * @param side     the direction its arms point to, orthogonal to the upright
     * @param material its material
     * @return the cross
     */
    private static Geometries cross(Point base, Vector up, Vector side, Material material) {
        Vector u = up.normalize();
        Vector right = side.normalize();
        Vector forward = right.crossProduct(u).normalize();
        double w = 3.5, a = 11, t = 3.5, armBottom = 10, armTop = 17, top = 27;
        // The outline in (right, up), counterclockwise when seen from the front
        double[][] outline = {{-w, 0}, {w, 0}, {w, armBottom}, {a, armBottom}, {a, armTop}, {w, armTop},
                {w, top}, {-w, top}, {-w, armTop}, {-a, armTop}, {-a, armBottom}, {-w, armBottom}};
        Frame frame = new Frame(base, right, u, forward);
        Geometries cross = new Geometries();
        for (int i = 0; i < outline.length; i++) {
            double[] p = outline[i], q = outline[(i + 1) % outline.length];
            cross.add(new Polygon(frame.at(p[0], p[1], -t), frame.at(q[0], q[1], -t),
                    frame.at(q[0], q[1], t), frame.at(p[0], p[1], t)).setMaterial(material));
        }
        // The front and back faces, each split into three convex rectangles
        double[][][] faces = {{{-w, 0}, {w, 0}, {w, top}, {-w, top}},
                {{-a, armBottom}, {-w, armBottom}, {-w, armTop}, {-a, armTop}},
                {{w, armBottom}, {a, armBottom}, {a, armTop}, {w, armTop}}};
        for (double[][] face : faces)
            for (double z : new double[]{-t, t}) {
                Point[] corners = new Point[4];
                // The back face lists its corners in reverse, so its normal points out too
                for (int i = 0; i < 4; i++) {
                    double[] c = face[z > 0 ? i : 3 - i];
                    corners[i] = frame.at(c[0], c[1], z);
                }
                cross.add(new Polygon(corners).setMaterial(material));
            }
        return cross;
    }

    /**
     * A local coordinate frame.
     *
     * @param origin  the origin
     * @param x       the first axis
     * @param y       the second axis
     * @param z       the third axis
     */
    private record Frame(Point origin, Vector x, Vector y, Vector z) {

        /**
         * Maps local coordinates to space.
         *
         * @param a the coordinate along {@code x}
         * @param b the coordinate along {@code y}
         * @param c the coordinate along {@code z}
         * @return the point in space
         */
        Point at(double a, double b, double c) {
            return new Point(
                    origin.getX() + x.getX() * a + y.getX() * b + z.getX() * c,
                    origin.getY() + x.getY() * a + y.getY() * b + z.getY() * c,
                    origin.getZ() + x.getZ() * a + y.getZ() * b + z.getZ() * c);
        }
    }

    /**
     * Builds a lathe profile from straight runs, circular arcs and smooth splines.
     */
    private static final class Profile {

        /** Steps per spline span */
        private static final int SPLINE_STEPS = 8;
        /** The largest angle, in degrees, between consecutive points of an arc */
        private static final double ARC_STEP = 7.5;
        /** Points closer than this to the previous one are dropped */
        private static final double MIN_STEP = 1e-6;

        /** The profile points so far */
        private final List<ProfilePoint> _points = new ArrayList<>();

        /** Starts an empty profile. */
        Profile() {
        }

        /**
         * Continues the profile in a straight line to a point.
         *
         * @param r the radius of the point
         * @param h the height of the point
         * @return this profile
         */
        Profile to(double r, double h) {
            ProfilePoint p = new ProfilePoint(Math.max(0, r), h);
            // Arcs and splines start where the profile already is, up to rounding - skip the repeat
            if (_points.isEmpty() || Math.hypot(p.radius() - _points.getLast().radius(),
                    p.height() - _points.getLast().height()) > MIN_STEP)
                _points.add(p);
            return this;
        }

        /**
         * Continues the profile along a circular arc.
         *
         * @param cr     the radius of the arc's center
         * @param ch     the height of the arc's center
         * @param radius the radius of the arc
         * @param from   the start angle in degrees (0 points away from the axis, 90 points up)
         * @param to     the end angle in degrees
         * @return this profile
         */
        Profile arc(double cr, double ch, double radius, double from, double to) {
            int steps = Math.max(4, (int) Math.ceil(Math.abs(to - from) / ARC_STEP));
            for (int i = 0; i <= steps; i++) {
                double angle = Math.toRadians(from + (to - from) * i / steps);
                to(cr + radius * Math.cos(angle), ch + radius * Math.sin(angle));
            }
            return this;
        }

        /**
         * Continues the profile along a smooth Catmull-Rom spline through points.
         *
         * @param rh the (radius, height) pairs of the points
         * @return this profile
         */
        Profile spline(double... rh) {
            List<double[]> knots = new ArrayList<>();
            knots.add(new double[]{_points.getLast().radius(), _points.getLast().height()});
            for (int i = 0; i < rh.length; i += 2) knots.add(new double[]{rh[i], rh[i + 1]});
            int n = knots.size();
            for (int i = 0; i < n - 1; i++) {
                double[] p0 = knots.get(Math.max(0, i - 1)), p1 = knots.get(i), p2 = knots.get(i + 1);
                double[] p3 = knots.get(Math.min(n - 1, i + 2));
                for (int s = 1; s <= SPLINE_STEPS; s++) {
                    double t = (double) s / SPLINE_STEPS;
                    to(catmullRom(p0[0], p1[0], p2[0], p3[0], t), catmullRom(p0[1], p1[1], p2[1], p3[1], t));
                }
            }
            return this;
        }

        /**
         * Starts the profile with the base all pieces share - a plinth, a bead and a cove -
         * scaled to a radius.
         *
         * @param radius the radius of the base
         * @return this profile
         */
        Profile base(double radius) {
            double k = radius / 38;
            return to(0, 0).to(38 * k, 0).to(38 * k, 5 * k)
                    .arc(32 * k, 5 * k, 6 * k, 0, 90).to(30 * k, 11 * k)
                    .arc(30 * k, 14 * k, 3 * k, -90, 90)
                    .arc(30 * k, 23 * k, 6 * k, -90, -180);
        }

        /**
         * Returns the profile points.
         *
         * @return the profile points
         */
        List<ProfilePoint> points() {
            return List.copyOf(_points);
        }

        /**
         * Interpolates one coordinate of a Catmull-Rom spline span.
         *
         * @param p0 the knot before the span
         * @param p1 the start of the span
         * @param p2 the end of the span
         * @param p3 the knot after the span
         * @param t  the position along the span, from 0 to 1
         * @return the interpolated coordinate
         */
        private static double catmullRom(double p0, double p1, double p2, double p3, double t) {
            double t2 = t * t;
            return 0.5 * (2 * p1 + (p2 - p0) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2
                    + (3 * p1 - p0 - 3 * p2 + p3) * t2 * t);
        }
    }
}
