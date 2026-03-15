package primitives;

/**
 * Represents a vector in 3D space, starting from the origin.
 * @author ityy35-bit
 */
public class Vector extends Point {

    /** X-axis unit vector */
    public static final Vector AXIS_X = new Vector(1, 0, 0);

    /** Y-axis unit vector */
    public static final Vector AXIS_Y = new Vector(0, 1, 0);

    /** Z-axis unit vector */
    public static final Vector AXIS_Z = new Vector(0, 0, 1);

    /**
     * Constructor to initialize a vector with individual coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @throws IllegalArgumentException if the vector is the zero vector
     */
    public Vector(double x, double y, double z) {
        super(x, y, z);
        if (_xyz.equals(Double3.ZERO)) {
            throw new IllegalArgumentException("The vector cannot be (0,0,0)");
        }
    }

    /**
     * Constructor to initialize a vector based on a Double3 object.
     * @param xyz the 3D coordinates
     * @throws IllegalArgumentException if the vector is the zero vector
     */
    public Vector(Double3 xyz) {
        super(xyz);
        if (xyz.equals(Double3.ZERO)) {
            throw new IllegalArgumentException("The vector cannot be (0,0,0)");
        }
    }

    @Override
    public Vector add(Vector other) {
        return new Vector(_xyz.add(other._xyz));
    }

    /**
     * Scales the vector by a scalar.
     * @param rhs the scaling factor
     * @return a new scaled vector
     */
    public Vector scale(double rhs) {
        return new Vector(_xyz.scale(rhs));
    }

    /**
     * Calculates the dot product of this vector and another vector.
     * @param rhs the other vector
     * @return the dot product value
     */
    public double dotProduct(Vector rhs) {
        return _xyz._d1() * rhs._xyz._d1() + _xyz._d2() * rhs._xyz._d2() + _xyz._d3() * rhs._xyz._d3();
    }

    /**
     * Calculates the cross product of this vector and another vector.
     * @param rhs the other vector
     * @return a new vector resulting from the cross product
     */
    public Vector crossProduct(Vector rhs) {
        return new Vector(
                _xyz._d2() * rhs._xyz._d3() - _xyz._d3() * rhs._xyz._d2(),
                _xyz._d3() * rhs._xyz._d1() - _xyz._d1() * rhs._xyz._d3(),
                _xyz._d1() * rhs._xyz._d2() - _xyz._d2() * rhs._xyz._d1()
        );
    }

    /**
     * Calculates the squared length of the vector.
     * @return the squared length
     */
    public double lengthSquared() {
        return dotProduct(this);
    }

    /**
     * Calculates the length of the vector.
     * @return the length
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * Normalizes the vector to length 1.
     * @return a new normalized vector
     */
    public Vector normalize() {
        return new Vector(_xyz.divide(length()));
    }

    @Override
    public String toString() {
        return "->" + super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return super.equals(obj);
    }
}