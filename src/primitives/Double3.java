package primitives;

import static primitives.Util.isZero;

import java.util.Objects;

/**
 * Immutable triad of doubles - the numeric building block for coordinates, RGB values and
 * per-channel attenuation coefficients.
 * <p>
 * The class is on the hot path of every computation, so it performs no argument validation.
 * Equality is tolerance-based (see {@link Util#isZero(double)}).
 *
 * @param _d1 the first component
 * @param _d2 the second component
 * @param _d3 the third component
 * @author Dan Zilberstein
 */
public record Double3(double _d1, double _d2, double _d3) {

    /** The triad (0,0,0). */
    public static final Double3 ZERO = new Double3(0, 0, 0);

    /** The triad (1,1,1). */
    public static final Double3 ONE = new Double3(1, 1, 1);

    /**
     * Creates a triad with the same value in all three components.
     *
     * @param value the value of every component
     */
    public Double3(double value) {
        this(value, value, value);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
                || obj instanceof Double3(double od1, double od2, double od3)
                && isZero(_d1 - od1)
                && isZero(_d2 - od2)
                && isZero(_d3 - od3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_d1, _d2, _d3);
    }

    @Override
    public String toString() {
        return "(" + _d1 + "," + _d2 + "," + _d3 + ")";
    }

    /**
     * Adds another triad component-wise.
     *
     * @param rhs the triad to add
     * @return the component-wise sum
     */
    public Double3 add(Double3 rhs) {
        return new Double3(_d1 + rhs._d1, _d2 + rhs._d2, _d3 + rhs._d3);
    }

    /**
     * Subtracts another triad component-wise.
     *
     * @param rhs the triad to subtract
     * @return the component-wise difference
     */
    public Double3 subtract(Double3 rhs) {
        return new Double3(_d1 - rhs._d1, _d2 - rhs._d2, _d3 - rhs._d3);
    }

    /**
     * Multiplies every component by a scalar.
     *
     * @param rhs the scale factor
     * @return the scaled triad
     */
    public Double3 scale(double rhs) {
        return new Double3(_d1 * rhs, _d2 * rhs, _d3 * rhs);
    }

    /**
     * Divides every component by a scalar.
     *
     * @param rhs the divisor
     * @return the reduced triad
     */
    public Double3 divide(double rhs) {
        return new Double3(_d1 / rhs, _d2 / rhs, _d3 / rhs);
    }

    /**
     * Multiplies another triad component-wise.
     *
     * @param rhs the triad to multiply by
     * @return the component-wise product
     */
    public Double3 product(Double3 rhs) {
        return new Double3(_d1 * rhs._d1, _d2 * rhs._d2, _d3 * rhs._d3);
    }

    /**
     * Checks whether every component is smaller than a value.
     *
     * @param k the value to compare against
     * @return {@code true} if all three components are smaller than {@code k}
     */
    public boolean isLowerThan(double k) {
        return _d1 < k && _d2 < k && _d3 < k;
    }

    /**
     * Checks whether every component is smaller than the matching component of another triad.
     *
     * @param other the triad to compare against
     * @return {@code true} if each component is smaller than its counterpart
     */
    public boolean isLowerThan(Double3 other) {
        return _d1 < other._d1 && _d2 < other._d2 && _d3 < other._d3;
    }

    /**
     * Checks whether at least one component reaches a value - the logical negation of
     * {@link #isLowerThan(double)}.
     *
     * @param k the value to compare against
     * @return {@code true} if any component is greater than or equal to {@code k}
     */
    public boolean isGreaterThan(double k) {
        return !isLowerThan(k);
    }

    /**
     * Checks whether at least one component reaches its counterpart - the logical negation of
     * {@link #isLowerThan(Double3)}.
     *
     * @param other the triad to compare against
     * @return {@code true} if any component is greater than or equal to its counterpart
     */
    public boolean isGreaterThan(Double3 other) {
        return !isLowerThan(other);
    }
}
