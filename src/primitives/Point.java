package primitives;

/**
 * An immutable point in 3D Cartesian space.
 *
 * @author itzik123
 */
public class Point {

    /** The origin (0,0,0). */
    public static final Point ZERO = new Point(Double3.ZERO);

    /** The coordinates of the point. */
    protected final Double3 _xyz;

    /**
     * Creates a point from a coordinates triad.
     *
     * @param xyz the coordinates
     */
    public Point(Double3 xyz) {
        _xyz = xyz;
    }

    /**
     * Creates a point from its coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public Point(double x, double y, double z) {
        _xyz = new Double3(x, y, z);
    }

    /**
     * Returns the X coordinate.
     *
     * @return the X coordinate
     */
    public double getX() {
        return _xyz._d1();
    }

    /**
     * Returns the Y coordinate.
     *
     * @return the Y coordinate
     */
    public double getY() {
        return _xyz._d2();
    }

    /**
     * Returns the Z coordinate.
     *
     * @return the Z coordinate
     */
    public double getZ() {
        return _xyz._d3();
    }

    /**
     * Creates the vector pointing from another point to this point.
     *
     * @param other the start point of the vector
     * @return the vector {@code this - other}
     * @throws IllegalArgumentException if the points coincide (zero vector)
     */
    public Vector subtract(Point other) {
        return new Vector(_xyz.subtract(other._xyz));
    }

    /**
     * Moves this point by a vector.
     *
     * @param vector the displacement
     * @return the displaced point
     */
    public Point add(Vector vector) {
        return new Point(_xyz.add(vector._xyz));
    }

    /**
     * Calculates the squared distance to another point - cheaper than {@link #distance(Point)}
     * when only comparisons are needed.
     *
     * @param other the other point
     * @return the squared distance
     */
    public double distanceSquared(Point other) {
        double dx = _xyz._d1() - other._xyz._d1();
        double dy = _xyz._d2() - other._xyz._d2();
        double dz = _xyz._d3() - other._xyz._d3();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Calculates the distance to another point.
     *
     * @param other the other point
     * @return the distance
     */
    public double distance(Point other) {
        return Math.sqrt(distanceSquared(other));
    }

    @Override
    public String toString() {
        return String.valueOf(_xyz);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return _xyz.equals(((Point) obj)._xyz);
    }

    @Override
    public int hashCode() {
        return _xyz.hashCode();
    }
}
