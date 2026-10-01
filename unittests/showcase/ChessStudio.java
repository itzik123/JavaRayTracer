package showcase;

import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

import java.util.Random;

/**
 * The set shared by the chess showcases: the board, the glass the pieces are made of, studio
 * softboxes and out-of-focus lights in the background.
 *
 * @author itzik123
 */
final class ChessStudio {

    /** The width of a board square */
    static final double SQUARE = 100;
    /** Pieces float this far above the board, so their bases never coincide with it */
    static final double LIFT = 0.2;
    /** The glass of the white pieces: clear crystal */
    static final Material CRYSTAL = new Material().setKs(0.9).setShininess(1200)
            .setKt(new Double3(0.94, 0.95, 0.97)).setIor(1.52);
    /** The glass of the black pieces: smoked */
    static final Material SMOKED_GLASS = new Material().setKs(0.9).setShininess(1200)
            .setKt(new Double3(0.55, 0.46, 0.37)).setIor(1.52);
    /** Warm colors of the out-of-focus lights in the background */
    private static final Color[] BOKEH_COLORS = {new Color(255, 170, 70), new Color(255, 205, 120),
            new Color(255, 230, 190), new Color(255, 140, 60)};

    /** Utility class - not instantiable. */
    private ChessStudio() {}

    /**
     * Finds the center of a board square, at the height the pieces stand on.
     *
     * @param file the file, 'a' to 'h' from left to right
     * @param rank the rank, 1 (nearest the camera) to 8
     * @return the center of the square
     */
    static Point square(char file, int rank) {
        return new Point(SQUARE * (file - 'a' - 3.5), LIFT, SQUARE * (4.5 - rank));
    }

    /**
     * Adds a standing piece.
     *
     * @param scene    the scene
     * @param piece    the piece
     * @param file     the file of its square
     * @param rank     the rank of its square
     * @param material its material
     */
    static void addPiece(Scene scene, ChessPieces.Piece piece, char file, int rank, Material material) {
        scene.geometries.add(ChessPieces.create(piece, square(file, rank), Vector.AXIS_Y, material));
    }

    /**
     * Adds the board - 64 polished squares in a walnut frame - and the table under it.
     *
     * @param scene the scene
     */
    static void addBoard(Scene scene) {
        Material light = new Material().setKd(new Double3(0.62, 0.58, 0.52)).setKs(0.35).setShininess(300).setKr(0.1);
        Material dark = new Material().setKd(new Double3(0.05, 0.045, 0.045)).setKs(0.5).setShininess(400).setKr(0.16);
        double half = 4 * SQUARE;
        for (int file = 0; file < 8; file++)
            for (int rank = 0; rank < 8; rank++) {
                double x = file * SQUARE - half, z = rank * SQUARE - half;
                scene.geometries.add(new Polygon(new Point(x, 0, z), new Point(x + SQUARE, 0, z),
                        new Point(x + SQUARE, 0, z + SQUARE), new Point(x, 0, z + SQUARE))
                        .setMaterial((file + rank) % 2 == 0 ? dark : light));
            }

        Material walnut = new Material().setKd(new Double3(0.30, 0.15, 0.07)).setKs(0.4).setShininess(200).setKr(0.08);
        double out = half + 45, bottom = -30;
        scene.geometries.add(
                // The frame around the squares
                new Polygon(new Point(-out, 0, -out), new Point(out, 0, -out), new Point(out, 0, -half),
                        new Point(-out, 0, -half)).setMaterial(walnut),
                new Polygon(new Point(-out, 0, half), new Point(out, 0, half), new Point(out, 0, out),
                        new Point(-out, 0, out)).setMaterial(walnut),
                new Polygon(new Point(-out, 0, -half), new Point(-half, 0, -half), new Point(-half, 0, half),
                        new Point(-out, 0, half)).setMaterial(walnut),
                new Polygon(new Point(half, 0, -half), new Point(out, 0, -half), new Point(out, 0, half),
                        new Point(half, 0, half)).setMaterial(walnut),
                // The sides of the board that face the camera
                new Polygon(new Point(-out, 0, out), new Point(out, 0, out), new Point(out, bottom, out),
                        new Point(-out, bottom, out)).setMaterial(walnut),
                new Polygon(new Point(out, 0, -out), new Point(out, 0, out), new Point(out, bottom, out),
                        new Point(out, bottom, -out)).setMaterial(walnut),
                new Polygon(new Point(-out, 0, -out), new Point(-out, 0, out), new Point(-out, bottom, out),
                        new Point(-out, bottom, -out)).setMaterial(walnut),
                // A dark, polished table
                new Plane(new Point(0, bottom, 0), Vector.AXIS_Y).setEmission(new Color(3, 3, 3))
                        .setMaterial(new Material().setKd(0.06).setKs(0.3).setShininess(100).setKr(0.1)));
    }

    /**
     * Adds a glowing panel behind a light, facing the board, so that glass and polish reflect
     * the light as a studio softbox.
     *
     * @param scene  the scene
     * @param light  the position of the light
     * @param width  the width of the panel
     * @param height the height of the panel
     * @param glow   the emission of the panel
     */
    static void addSoftbox(Scene scene, Point light, double width, double height, Color glow) {
        Vector toBoard = Point.ZERO.subtract(light).normalize();
        Point center = light.add(toBoard.scale(-25));
        Vector right = toBoard.crossProduct(Vector.AXIS_Y).normalize().scale(width / 2);
        Vector up = right.crossProduct(toBoard).normalize().scale(height / 2);
        scene.geometries.add(new Polygon(center.add(right).add(up), center.add(right.scale(-1)).add(up),
                center.add(right.scale(-1)).add(up.scale(-1)), center.add(right).add(up.scale(-1)))
                .setEmission(glow).setMaterial(new Material().setKa(0)));
    }

    /**
     * Scatters small warm lights far behind the board, inside the camera's view, where depth of
     * field turns them into soft bokeh.
     *
     * @param scene      the scene
     * @param camera     the camera location
     * @param vTo        the unit viewing direction
     * @param viewWidth  the width of the view at unit distance
     * @param viewHeight the height of the view at unit distance
     * @param count      the number of lights to try placing; those that would sink below the
     *                   table are skipped
     */
    static void addBokehLights(Scene scene, Point camera, Vector vTo, double viewWidth, double viewHeight,
                               int count) {
        Vector right = vTo.crossProduct(Vector.AXIS_Y).normalize();
        Vector up = right.crossProduct(vTo);
        Material glow = new Material().setKa(0);
        Random random = new Random(64);
        for (int i = 0; i < count; i++) {
            double distance = 2700 + 1600 * random.nextDouble();
            double x = (random.nextDouble() - 0.5) * 1.3, y = -0.05 + 0.6 * random.nextDouble();
            Point center = camera.add(vTo.scale(distance)).add(right.scale(x * viewWidth * distance))
                    .add(up.scale(y * viewHeight * distance));
            // Lights below the table would be hidden anyway
            if (center.getY() < 40) continue;
            double strength = 0.8 + 1.4 * random.nextDouble();
            double radius = 25 + 30 * random.nextDouble();
            Color color = BOKEH_COLORS[random.nextInt(BOKEH_COLORS.length)];
            scene.geometries.add(new Sphere(center, radius).setEmission(color.scale(strength)).setMaterial(glow));
        }
    }
}
