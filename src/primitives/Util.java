package primitives;

/**
 * Low-level numeric utilities shared by the whole engine.
 * <p>
 * Zero checks work directly on the IEEE-754 exponent of a {@code double}, which is both fast and
 * scale-aware: any value whose binary exponent is below {@link #ACCURACY} (roughly 1e-12) is
 * treated as zero. This absorbs the floating-point noise that accumulates in geometric
 * calculations.
 *
 * @author Dan Zilberstein
 */
public final class Util {
    /** Exponent threshold below which a value is considered zero (about 1e-12). */
    private static final int ACCURACY = -40;
    /** Bias of the IEEE-754 double exponent. */
    private static final int EXPONENT_BIAS = 1023;
    /** Number of mantissa bits in a double. */
    private static final int EXPONENT_SHIFT = 52;
    /** Mask isolating the 11 exponent bits. */
    private static final long EXPONENT_MASK = 0x7FFL;

    /** Utility class - not instantiable. */
    private Util() {}

    /**
     * Extracts the unbiased binary exponent of a number.
     *
     * @param num the number
     * @return the unbiased exponent
     */
    private static int getExp(double num) {
        return (int) ((Double.doubleToRawLongBits(num) >> EXPONENT_SHIFT) & EXPONENT_MASK) - EXPONENT_BIAS;
    }

    /**
     * Checks whether a number is zero within the engine accuracy.
     *
     * @param number the number to check
     * @return {@code true} if the number is considered zero
     */
    public static boolean isZero(double number) {
        return getExp(number) < ACCURACY;
    }

    /**
     * Snaps a number that is numerically close to zero to exactly zero.
     *
     * @param number the number to align
     * @return 0.0 if the number is considered zero, otherwise the number itself
     */
    public static double alignZero(double number) {
        return getExp(number) < ACCURACY ? 0.0 : number;
    }

    /**
     * Checks whether two numbers have the same sign. Zero has no sign.
     *
     * @param n1 the first number
     * @param n2 the second number
     * @return {@code true} if both numbers are positive or both are negative
     */
    public static boolean compareSign(double n1, double n2) {
        return (n1 > 0 && n2 > 0) || (n1 < 0 && n2 < 0);
    }

    /**
     * Returns a random number in {@code [min, max)}.
     *
     * @param min the lower bound (inclusive)
     * @param max the upper bound (exclusive)
     * @return a uniformly distributed random number
     */
    public static double random(double min, double max) {
        return Math.random() * (max - min) + min;
    }
}
