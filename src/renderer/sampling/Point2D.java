package renderer.sampling;

/**
 * A 2D offset on a sampling area.
 *
 * @param x the horizontal offset
 * @param y the vertical offset
 * @author itzik123
 */
public record Point2D(double x, double y) {

    /** The area center. */
    public static final Point2D ORIGIN = new Point2D(0, 0);
}
