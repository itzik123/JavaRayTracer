package primitives;

/**
 * An immutable, non-zero vector in 3D space.
 * <p>
 * The zero vector is rejected at construction, so every operation that could produce it (adding
 * opposite vectors, crossing parallel vectors, scaling by zero) throws
 * {@link IllegalArgumentException}.
 *
 * @author ityy35-bit
 */
public class Vector extends Point {

    /** Unit vector along the X axis. */
    public static final Vector AXIS_X = new Vector(1, 0, 0);

    /** Unit vector along the Y axis. */
    public static final Vector AXIS_Y = new Vector(0, 1, 0);

    /** Unit vector along the Z axis. */
    public static final Vector AXIS_Z = new Vector(0, 0, 1);

    /** Beyond this |cos| with the X axis, the X axis is too close to use for building a normal. */
    private static final double AXIS_ALIGNMENT_LIMIT = 0.9;

    /**
     * Creates a vector from its components.
     *
     * @param x the X component
     * @param y the Y component
     * @param z the Z component
     * @throws IllegalArgumentException if all components are zero
     */
    public Vector(double x, double y, double z) {
        this(new Double3(x, y, z));
    }

    /**
     * Creates a vector from a components triad.
     *
     * @param xyz the components
     * @throws IllegalArgumentException if the triad is zero
     */
    public Vector(Double3 xyz) {
        super(xyz);
        if (xyz.equals(Double3.ZERO)) throw new IllegalArgumentException("The vector cannot be (0,0,0)");
    }

    @Override
    public Vector add(Vector other) {
        return new Vector(_xyz.add(other._xyz));
    }

    /**
     * Multiplies the vector by a scalar.
     *
     * @param rhs the scale factor
     * @return the scaled vector
     * @throws IllegalArgumentException if the factor is zero
     */
    public Vector scale(double rhs) {
        return new Vector(_xyz.scale(rhs));
    }

    /**
     * Calculates the dot product with another vector.
     *
     * @param rhs the other vector
     * @return the dot product
     */
    public double dotProduct(Vector rhs) {
        return _xyz._d1() * rhs._xyz._d1() + _xyz._d2() * rhs._xyz._d2() + _xyz._d3() * rhs._xyz._d3();
    }

    /**
     * Calculates the cross product with another vector.
     *
     * @param rhs the other vector
     * @return the vector orthogonal to both operands
     * @throws IllegalArgumentException if the vectors are parallel
     */
    public Vector crossProduct(Vector rhs) {
        return new Vector(
                _xyz._d2() * rhs._xyz._d3() - _xyz._d3() * rhs._xyz._d2(),
                _xyz._d3() * rhs._xyz._d1() - _xyz._d1() * rhs._xyz._d3(),
                _xyz._d1() * rhs._xyz._d2() - _xyz._d2() * rhs._xyz._d1());
    }

    /**
     * Returns the squared length of the vector.
     *
     * @return the squared length of the vector
     */
    public double lengthSquared() {
        return dotProduct(this);
    }

    /**
     * Returns the length of the vector.
     *
     * @return the length of the vector
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * Returns a unit vector with the same direction.
     *
     * @return a unit vector with the same direction
     */
    public Vector normalize() {
        return new Vector(_xyz.divide(length()));
    }

    /**
     * Builds an arbitrary unit vector orthogonal to this one. Together with
     * {@code this.crossProduct(orthogonal())} it completes an orthonormal basis, which is used to
     * spread sample rays around a central direction.
     *
     * @return a unit vector orthogonal to this vector
     */
    public Vector orthogonal() {
        // Cross with the coordinate axis that is least aligned with this vector
        Vector axis = Math.abs(getX()) > AXIS_ALIGNMENT_LIMIT * length() ? AXIS_Y : AXIS_X;
        return crossProduct(axis).normalize();
    }

    @Override
    public String toString() {
        return "->" + super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vector && super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
