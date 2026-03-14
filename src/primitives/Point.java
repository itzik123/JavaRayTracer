package primitives;

import java.util.Objects;

/**
 * Represents a point in 3D space.
 * @author itzik123
 */
public class Point {

    /**
     * The 3D coordinates of the point.
     */
    protected final Double3 _xyz;

    /**
     * The origin point (0,0,0).
     */
    public static final Point ZERO = new Point(Double3.ZERO);

    /**
     * Constructor to initialize a Point based on a Double3 object.
     * * @param xyz the 3D coordinates
     */
    public Point(Double3 xyz) {
        _xyz = xyz;
    }

    /**
     * Constructor to initialize a Point with individual coordinates.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public Point(double x, double y, double z) {
        _xyz = new Double3(x, y, z);
    }

    /**
     * Subtracts a point from this point to create a vector.
     * @param other the point to subtract
     * @return a new vector from the other point to this point
     */
    public Vector subtract(Point other) {
        return new Vector(_xyz.subtract(other._xyz));
    }

    /**
     * Adds a vector to this point to create a new point.
     * @param vector the vector to add
     * @return a new point resulting from the addition
     */
    public Point add(Vector vector) {
        return new Point(_xyz.add(vector._xyz));
    }

    /**
     * Calculates the squared distance between this point and another point.
     * @param other the other point to calculate the distance to
     * @return the squared distance between the two points
     */
    public double distanceSquared(Point other) {
        Double3 vec = _xyz.subtract(other._xyz);
        Double3 squared = vec.product(vec);
        return squared._d1() + squared._d2() + squared._d3();
    }

    /**
     * Calculates the distance between this point and another point.
     * @param other the other point to calculate the distance to
     * @return the distance between the two points
     */
    public double distance(Point other) {
        return Math.sqrt(this.distanceSquared(other));
    }

    @Override
    public String toString() {
        return "" + _xyz;
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