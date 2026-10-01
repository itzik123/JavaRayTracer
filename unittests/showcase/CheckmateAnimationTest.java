package showcase;

import static showcase.ChessStudio.CRYSTAL;
import static showcase.ChessStudio.LIFT;
import static showcase.ChessStudio.SMOKED_GLASS;
import static showcase.ChessStudio.addPiece;
import static showcase.ChessStudio.square;

import geometries.impl.Geometries;
import geometries.impl.Lathe.ProfilePoint;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;
import renderer.Camera;
import renderer.RayTracerType;
import renderer.postprocessing.BloomFilter;
import renderer.sampling.Jittered;
import scene.Scene;
import showcase.ChessPieces.Piece;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A five-second film of the moment of checkmate: the crystal queen is carried across the board,
 * and the smoked-glass king teeters, topples and bounces to rest, while the camera drifts in and
 * the focus follows the action.
 * <p>
 * The engine renders still images, so every moment is a scene of its own. The king's fall is a
 * rigid-body simulation, with the mass and inertia of the piece integrated from the profile it is
 * turned from. Each frame averages several renders spread over the time the shutter is open, so
 * fast motion blurs as it does on film - at no extra cost, since the rays a still image spends on
 * anti-aliasing and depth of field are just spread over the sub-frames. The renders also measure
 * the noise of every pixel, which {@link SubframeDenoiser} then smooths away.
 * <p>
 * The frames are written to {@code images/checkmate}; a tool such as ffmpeg joins them into a
 * video.
 *
 * @author itzik123
 */
@Showcase
class CheckmateAnimationTest {

    /** Default constructor to satisfy JavaDoc generator */
    CheckmateAnimationTest() { /* to satisfy JavaDoc generator */ }

    /**
     * Render settings.
     *
     * @param width        the width of a frame, in pixels
     * @param height       the height of a frame, in pixels
     * @param antiAliasing samples per axis of a pixel in each sub-frame; depth of field is on
     *                     when there is more than one
     * @param subframes    the renders averaged into each frame
     */
    record Quality(int width, int height, int antiAliasing, int subframes) {
    }

    /** The settings of the film: 16 rays per pixel, spread over 4 moments */
    static final Quality FILM = new Quality(640, 360, 2, 4);
    /** Frames per second */
    static final int FPS = 10;
    /** The length of the film, in frames */
    static final int FRAMES = 50;
    /** The fraction of a frame's time the shutter stays open - a 180-degree shutter, as in cinema */
    private static final double SHUTTER = 0.5;

    /** The width of the view plane - 16:9, like a film */
    private static final double VP_WIDTH = 320;
    /** The height of the view plane */
    private static final double VP_HEIGHT = 180;
    /** The distance of the view plane from the camera */
    private static final double VP_DISTANCE = 600;
    /**
     * The radius of the lens aperture per unit of focus distance. Opening the lens as the focus
     * moves out keeps the background blurred alike in close-ups and wide shots, as a fixed
     * f-number does on a real camera.
     */
    private static final double APERTURE = 0.02;

    /** Where the camera starts: close to the queen */
    private static final Point CAMERA_FROM = new Point(505, 106, 751);
    /** Where the camera ends, taking in the whole scene */
    private static final Point CAMERA_TO = new Point(180, 64, 820);
    /** When the camera comes to rest, in seconds */
    private static final double CAMERA_STOPS = 4.8;
    /** The height at which the camera follows the queen */
    private static final double TRACKING_HEIGHT = 80;
    /** How far ahead of the queen the camera looks, to leave room for her to move into */
    private static final double TRACKING_LEAD = 70;
    /** When the camera starts to turn from the queen to the whole scene, in seconds */
    private static final double TRACKING_ENDS = 0.9;
    /** How long the camera takes to turn to the whole scene, in seconds */
    private static final double TURN = 2.4;
    /** The point the camera looks at last */
    private static final Point TARGET_TO = new Point(-85, 70, -75);

    /** The queen's move: from h3 ... */
    private static final Point QUEEN_FROM = square('h', 3);
    /** ... to e6 */
    private static final Point QUEEN_TO = square('e', 6);
    /** The horizontal direction the queen moves in */
    private static final Vector QUEEN_HEADING = QUEEN_TO.subtract(QUEEN_FROM).normalize();
    /** When the queen is lifted, in seconds */
    private static final double QUEEN_LIFTED = 0.15;
    /** When the queen lands, in seconds */
    private static final double QUEEN_LANDS = 1.55;
    /** How high the queen is carried */
    private static final double QUEEN_ARC = 60;
    /** The height of the queen */
    private static final double QUEEN_HEIGHT = 150;
    /** The radius of the queen's base */
    private static final double QUEEN_RADIUS = 36;
    /** How far the queen sways, in radians, as she is carried by the top */
    private static final double QUEEN_SWAY = Math.toRadians(8);

    /** The square the black king stands on */
    private static final Point KING_SQUARE = square('d', 5);
    /** The horizontal direction the king falls to */
    private static final Vector KING_FALLS_TO = new Vector(-1, 0, 0.35).normalize();
    /** When the king is pushed over, in seconds - just after the queen lands */
    private static final double KING_PUSHED = 1.6;
    /** The fall of the king */
    private static final Topple TOPPLE = new Topple(Piece.KING._profile);

    /** When the focus starts to move from the queen to the king, in seconds */
    private static final double FOCUS_TO_KING = 1.6;
    /** When the focus starts to return to the queen, in seconds */
    private static final double FOCUS_TO_QUEEN = 3.3;
    /** How long each pull of focus takes, in seconds */
    private static final double FOCUS_PULL = 0.8;

    /** The position of the key light */
    private static final Point KEY_LIGHT = new Point(-560, 620, 380);
    /** The position of the rim light */
    private static final Point RIM_LIGHT = new Point(560, 420, -760);
    /** Everything that stands still: the board, the other pieces, softboxes and bokeh */
    private static final Geometries SET = createSet();

    /**
     * The pose of a piece.
     *
     * @param base the center of its base
     * @param axis the direction of its axis
     * @param side the direction its king's cross arms point to
     */
    private record Pose(Point base, Vector axis, Vector side) {
    }

    /**
     * Produce the film: frames {@code images/checkmate/frame_000.png} onwards.
     */
    @Test
    void testCheckmateAnimation() {
        for (int frame = 0; frame < FRAMES; frame++) {
            renderSubframes(frame, FILM, "checkmate");
            assembleFrame(frame, FILM, "checkmate");
        }
    }

    /**
     * Renders the moments that make up one frame, spread over the time the shutter is open, as
     * {@code images/<folder>/subframes/frame_NNN_S.png}. They are kept, so that the frames can be
     * assembled again without rendering again.
     *
     * @param frame   the frame number
     * @param quality the render settings
     * @param folder  the folder under {@code images} to save to
     */
    static void renderSubframes(int frame, Quality quality, String folder) {
        try {
            Files.createDirectories(Path.of(System.getProperty("user.dir"), "images", folder, "subframes"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        int n = quality.subframes();
        for (int s = 0; s < n; s++) {
            double time = (frame + SHUTTER * ((s + 0.5) / n - 0.5)) / FPS;
            camera(time, quality).renderImage().writeToImage(subframeName(folder, frame, s));
        }
    }

    /**
     * Assembles one frame of the film from its moments: averages and denoises them, adds bloom and
     * saves the result as {@code images/<folder>/frame_NNN.png}.
     *
     * @param frame   the frame number
     * @param quality the render settings
     * @param folder  the folder under {@code images} the sub-frames were saved to
     */
    static void assembleFrame(int frame, Quality quality, String folder) {
        Path images = Path.of(System.getProperty("user.dir"), "images");
        try {
            List<BufferedImage> renders = new ArrayList<>(quality.subframes());
            for (int s = 0; s < quality.subframes(); s++)
                renders.add(ImageIO.read(images.resolve(subframeName(folder, frame, s) + ".png").toFile()));
            BufferedImage image = new BloomFilter().setThreshold(0.8f).setBlurRadius(quality.width() / 70)
                    .setIntensity(0.35f).apply(SubframeDenoiser.denoise(renders));
            ImageIO.write(image, "png", images.resolve(folder).resolve("frame_%03d.png".formatted(frame)).toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Names the image of a sub-frame, relative to the {@code images} folder and without extension.
     *
     * @param folder   the folder of the film
     * @param frame    the frame number
     * @param subframe the sub-frame number
     * @return the name of the image
     */
    private static String subframeName(String folder, int frame, int subframe) {
        return "%s/subframes/frame_%03d_%d".formatted(folder, frame, subframe);
    }

    /**
     * Builds the camera, and the scene it films, at a moment of the film.
     *
     * @param time    the time, in seconds
     * @param quality the render settings
     * @return the camera
     */
    private static Camera camera(double time, Quality quality) {
        Pose queen = queenPose(time), king = TOPPLE.pose(time - KING_PUSHED);
        Point location = lerp(CAMERA_FROM, CAMERA_TO, smootherstep(time / CAMERA_STOPS));
        // The camera follows the queen, looking a little ahead of her, then turns to the whole scene
        Point tracking = new Point(queen.base().getX() + QUEEN_HEADING.getX() * TRACKING_LEAD, TRACKING_HEIGHT,
                queen.base().getZ() + QUEEN_HEADING.getZ() * TRACKING_LEAD);
        Point target = lerp(tracking, TARGET_TO, smootherstep((time - TRACKING_ENDS) / TURN));
        Vector vTo = target.subtract(location).normalize();

        // The focus rests on the middle of the queen, moves to the king as he falls, and returns
        double toKing = smootherstep((time - FOCUS_TO_KING) / FOCUS_PULL)
                - smootherstep((time - FOCUS_TO_QUEEN) / FOCUS_PULL);
        Point focus = lerp(queen.base().add(queen.axis().scale(QUEEN_HEIGHT / 2)),
                TOPPLE.centerOfMass(time - KING_PUSHED), toKing);

        Scene scene = new Scene("Checkmate")
                .setBackground(new Color(4, 4, 6))
                .setAmbientLight(new AmbientLight(new Color(10, 10, 12)));
        scene.geometries.add(SET,
                ChessPieces.create(Piece.QUEEN, queen.base(), queen.axis(), queen.side(), CRYSTAL),
                ChessPieces.create(Piece.KING, king.base(), king.axis(), king.side(), SMOKED_GLASS));
        scene.lights.add(new SpotLight(new Color(1100, 980, 830), KEY_LIGHT,
                new Point(0, 40, -100).subtract(KEY_LIGHT)).setNarrowBeam(16).setSoftShadows(70, 2));
        scene.lights.add(new PointLight(new Color(260, 290, 380), RIM_LIGHT).setSoftShadows(60, 2));

        Camera.Builder builder = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.ADAPTIVE_GRID)
                .setAdaptiveGridParameters(8, 11)
                .setLocation(location).setDirection(target)
                .setVpSize(VP_WIDTH, VP_HEIGHT).setVpDistance(VP_DISTANCE)
                .setResolution(quality.width(), quality.height())
                .setAntiAliasing(quality.antiAliasing(), new Jittered())
                .setMultithreading(-2);
        if (quality.antiAliasing() > 1) {
            double focusDistance = focus.subtract(location).dotProduct(vTo);
            builder.setDepthOfField(APERTURE * focusDistance, focusDistance);
        }
        return builder.build();
    }

    /**
     * Finds the queen's pose: lifted, carried in an arc and set down. Held by the top, she sways
     * - her base lags behind as she speeds up and swings ahead as she slows down.
     *
     * @param time the time, in seconds
     * @return the pose of the queen
     */
    private static Pose queenPose(double time) {
        double u = clamp((time - QUEEN_LIFTED) / (QUEEN_LANDS - QUEEN_LIFTED));
        double s = smootherstep(u);
        double lift = QUEEN_ARC * Math.sin(Math.PI * s);
        Point top = lerp(QUEEN_FROM, QUEEN_TO, s).add(Vector.AXIS_Y.scale(lift + QUEEN_HEIGHT));

        // The sway follows the acceleration, scaled to its peak of 10/sqrt(3), and is limited so
        // that the rim of the base never dips into the board
        double acceleration = 60 * u * (1 - u) * (1 - 2 * u) / (10 / Math.sqrt(3));
        double limit = Math.asin(Math.min(1, lift / QUEEN_RADIUS));
        double sway = Math.clamp(QUEEN_SWAY * acceleration, -limit, limit);
        double sin = Math.sin(sway), cos = Math.cos(sway);
        Vector axis = new Vector(QUEEN_HEADING.getX() * sin, cos, QUEEN_HEADING.getZ() * sin);
        Point base = new Point(top.getX() - axis.getX() * QUEEN_HEIGHT, top.getY() - axis.getY() * QUEEN_HEIGHT,
                top.getZ() - axis.getZ() * QUEEN_HEIGHT);
        return new Pose(base, axis, QUEEN_HEADING);
    }

    /**
     * Builds everything that stands still: the board, the rest of the game, the softboxes and the
     * out-of-focus lights.
     *
     * @return the still part of the scene
     */
    private static Geometries createSet() {
        Scene set = new Scene("Set");
        ChessStudio.addBoard(set);
        addPiece(set, Piece.KING, 'c', 8, CRYSTAL);
        addPiece(set, Piece.ROOK, 'h', 6, CRYSTAL);
        addPiece(set, Piece.PAWN, 'b', 3, CRYSTAL);
        addPiece(set, Piece.PAWN, 'f', 7, SMOKED_GLASS);
        addPiece(set, Piece.PAWN, 'g', 7, SMOKED_GLASS);
        addPiece(set, Piece.PAWN, 'a', 7, SMOKED_GLASS);
        addPiece(set, Piece.BISHOP, 'b', 7, SMOKED_GLASS);
        addPiece(set, Piece.ROOK, 'f', 8, SMOKED_GLASS);
        ChessStudio.addSoftbox(set, KEY_LIGHT, 360, 220, new Color(255, 240, 220));
        ChessStudio.addSoftbox(set, RIM_LIGHT, 120, 420, new Color(200, 220, 255));
        // Spread a little wider than the final view, to fill the frame as the camera moves
        ChessStudio.addBokehLights(set, CAMERA_TO, TARGET_TO.subtract(CAMERA_TO).normalize(),
                1.25 * VP_WIDTH / VP_DISTANCE, VP_HEIGHT / VP_DISTANCE, 48);
        return set.geometries;
    }

    /**
     * Clamps a value to [0, 1].
     *
     * @param u the value
     * @return the clamped value
     */
    private static double clamp(double u) {
        return Math.clamp(u, 0, 1);
    }

    /**
     * Eases from 0 to 1 with zero velocity and acceleration at both ends.
     *
     * @param u the progress, clamped to [0, 1]
     * @return the eased progress
     */
    private static double smootherstep(double u) {
        u = clamp(u);
        return u * u * u * (u * (6 * u - 15) + 10);
    }

    /**
     * Interpolates linearly between two points.
     *
     * @param a the point at 0
     * @param b the point at 1
     * @param s the position between them
     * @return the interpolated point
     */
    private static Point lerp(Point a, Point b, double s) {
        return new Point(a.getX() + (b.getX() - a.getX()) * s, a.getY() + (b.getY() - a.getY()) * s,
                a.getZ() + (b.getZ() - a.getZ()) * s);
    }

    /**
     * The fall of the king, simulated as a rigid body turning in the vertical plane of its fall.
     * Pushed over the edge of its base, the king teeters past the balance point, rolls onto the
     * rim of the base and drops until its crown strikes the board; it bounces and settles on the
     * two rims. Gravity is slowed down, as if filmed in slow motion.
     * <p>
     * Points of the piece are given in its profile coordinates: the distance from the axis
     * towards the fall, and the height along the axis.
     */
    private static final class Topple {

        /** Gravity, in units per second squared: a 5 cm board square is 100 units */
        private static final double GRAVITY = 9.81 * 2000;
        /** How much slower than real time the fall plays */
        private static final double SLOW_MOTION = 4;
        /** The edge of the base the king first turns over */
        private static final double[] EDGE = {38, 0};
        /** The rim at the top of the plinth, which meets the board once the king is horizontal */
        private static final double[] RIM = {38, 5};
        /** The rim of the crown, which strikes the board */
        private static final double[] CROWN = {22.5, 134};
        /** The angle at which the king lies on the board */
        private static final double LYING = Math.PI / 2 + Math.atan2(RIM[0] - CROWN[0], CROWN[1] - RIM[1]);
        /** The push, as a multiple of the one that would just balance the king on its edge */
        private static final double PUSH = 1.02;
        /** The share of the speed the crown keeps when it bounces off the board */
        private static final double RESTITUTION = 0.3;
        /** Below this angular speed, in radians per second, a bounce ends the motion */
        private static final double SETTLED = 0.05;
        /** The integration step, in seconds */
        private static final double STEP = 1e-5;
        /** The interval between stored angles, in seconds */
        private static final double SAMPLE = 1e-3;
        /** The simulated time, in seconds */
        private static final double DURATION = 4;

        /** The height of the center of mass */
        private final double _centerOfMass;
        /** The tilt of the king every {@link #SAMPLE} seconds after the push */
        private final double[] _angles;

        /**
         * Simulates the fall of a piece.
         *
         * @param profile the profile the piece is turned from
         */
        Topple(List<ProfilePoint> profile) {
            // Mass, first and second moments of the solid of revolution, by integrating over the
            // profile area: a point at radius r stands for a ring of mass 2πr
            double mass = 0, moment = 0, inertia = 0, cell = 0.25;
            for (double r = cell / 2; r < EDGE[0]; r += cell)
                for (double h = cell / 2; h < 160; h += cell)
                    if (inside(profile, r, h)) {
                        mass += r;
                        moment += r * h;
                        // About a transverse axis through the base center: h² + r²/2, averaged around the ring
                        inertia += r * (h * h + r * r / 2);
                    }
            _centerOfMass = moment / mass;
            double gyration = inertia / mass - _centerOfMass * _centerOfMass;

            double gravity = GRAVITY / (SLOW_MOTION * SLOW_MOTION);
            // Just enough spin to lift the center of mass over the edge, plus a little
            double a = -EDGE[0], b = _centerOfMass - EDGE[1];
            double rise = Math.hypot(a, b) - b;
            double omega = PUSH * Math.sqrt(2 * gravity * rise / (gyration + a * a + b * b));

            _angles = new double[(int) (DURATION / SAMPLE) + 1];
            double theta = 0;
            boolean settled = false;
            int steps = (int) Math.round(SAMPLE / STEP);
            for (int i = 0; i < _angles.length; i++) {
                _angles[i] = theta;
                for (int k = 0; k < steps && !settled; k++) {
                    double[] pivot = pivot(theta);
                    a = -pivot[0];
                    b = _centerOfMass - pivot[1];
                    // The torque of gravity is the weight times the horizontal reach of the center of mass
                    double reach = a * Math.cos(theta) + b * Math.sin(theta);
                    omega += gravity * reach / (gyration + a * a + b * b) * STEP;
                    theta += omega * STEP;
                    if (theta >= LYING) {
                        theta = LYING;
                        omega = -RESTITUTION * omega;
                        settled = Math.abs(omega) < SETTLED;
                    }
                }
            }
        }

        /**
         * Finds the pose of the king.
         *
         * @param time the time since the push, in seconds
         * @return the pose of the king
         */
        Pose pose(double time) {
            double theta = angle(time);
            double sin = Math.sin(theta), cos = Math.cos(theta);
            Vector axis = new Vector(KING_FALLS_TO.getX() * sin, cos, KING_FALLS_TO.getZ() * sin);
            Vector side = new Vector(KING_FALLS_TO.getX() * cos, -sin, KING_FALLS_TO.getZ() * cos);
            return new Pose(locate(0, 0, theta), axis, side);
        }

        /**
         * Finds the center of mass of the king.
         *
         * @param time the time since the push, in seconds
         * @return the center of mass
         */
        Point centerOfMass(double time) {
            return locate(0, _centerOfMass, angle(time));
        }

        /**
         * Finds the tilt of the king.
         *
         * @param time the time since the push, in seconds
         * @return the tilt, from 0 (standing) to {@link #LYING}
         */
        private double angle(double time) {
            double index = Math.clamp(time / SAMPLE, 0, _angles.length - 1);
            int i = (int) Math.min(index, _angles.length - 2);
            return _angles[i] + (_angles[i + 1] - _angles[i]) * (index - i);
        }

        /**
         * Finds where a point of the king is when it is tilted. The king turns around the point it
         * rests on, which lies on the board where the base has rolled to.
         *
         * @param r     the distance of the point from the axis, towards the fall
         * @param h     the height of the point along the axis
         * @param theta the tilt
         * @return the point in space
         */
        private static Point locate(double r, double h, double theta) {
            double[] pivot = pivot(theta);
            double rolled = pivot == EDGE ? EDGE[0] : EDGE[0] + RIM[1] - EDGE[1];
            double dr = r - pivot[0], dh = h - pivot[1];
            double ahead = rolled + dr * Math.cos(theta) + dh * Math.sin(theta);
            double up = -dr * Math.sin(theta) + dh * Math.cos(theta);
            return new Point(KING_SQUARE.getX() + KING_FALLS_TO.getX() * ahead, LIFT + up,
                    KING_SQUARE.getZ() + KING_FALLS_TO.getZ() * ahead);
        }

        /**
         * Finds the point of the profile the king rests on at a tilt.
         *
         * @param theta the tilt
         * @return the edge of the base until the king is horizontal, then the rim
         */
        private static double[] pivot(double theta) {
            return theta <= Math.PI / 2 ? EDGE : RIM;
        }

        /**
         * Checks whether a point lies inside the area between a profile and its axis.
         *
         * @param profile the profile, from the axis at the bottom to the axis at the top
         * @param r       the distance from the axis
         * @param h       the height
         * @return whether the point is inside
         */
        private static boolean inside(List<ProfilePoint> profile, double r, double h) {
            boolean inside = false;
            for (int i = 0, j = profile.size() - 1; i < profile.size(); j = i++) {
                ProfilePoint p = profile.get(i), q = profile.get(j);
                if ((p.height() > h) != (q.height() > h)
                        && r < p.radius() + (h - p.height()) * (q.radius() - p.radius()) / (q.height() - p.height()))
                    inside = !inside;
            }
            return inside;
        }
    }
}
