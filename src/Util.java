package primitives;

/**
 * Collection of low-level utility methods used throughout the primitives
 * package.
 * <p>
 * The class provides operations related to floating-point accuracy control,
 * sign comparison, and simple numeric utilities.
 * </p>
 * <p>
 * The implementation is optimized for performance and therefore avoids
 * expensive floating-point operations where possible, relying instead on
 * direct manipulation of the IEEE-754 {@code double} representation.
 * </p>
 * This class cannot be instantiated.
 * @author Dan Zilberstein
 */
public final class Util {
   /**
    * Accuracy threshold expressed in exponent units.
    * Values whose exponent is smaller than this threshold are considered zero.
    * Roughly corresponds to about 1e-12.
    */
   private static final int  ACCURACY       = -40;
   /** Bias used in the IEEE-754 double exponent representation (1023). */
   private static final int  EXPONENT_BIAS  = 1023;
   /** Number of mantissa bits in a double (used to extract the exponent). */
   private static final int  EXPONENT_SHIFT = 52;
   /** Bit mask used to isolate the exponent field. */
   private static final long EXPONENT_MASK  = 0x7FFL;

   /** Don't let anyone instantiate this class. */
   private Util() {}

   /**
    * Extracts the unbiased exponent of a {@code double}.
    * <p>
    * IEEE-754 double format:
    * 
    * <pre>
    * sign | exponent (11 bits) | mantissa (52 bits)
    * </pre>
    * 
    * The actual number represented is:
    * 
    * <pre>
    * value = mantissa * 2 ^ exponent
    * </pre>
    * 
    * where the stored exponent is biased by 1023.
    * </p>
    * @param  num the number
    * @return     the unbiased exponent
    */
   private static int getExp(double num) {
      // Extract the unbiased exponent from the IEEE-754 double representation:
      // 1. Convert to raw bits
      // 2. Shift out the mantissa (52 bits)
      // 3. Mask the exponent field (0x7FF)
      // 4. Remove the exponent bias (1023)
      return (int) ((Double.doubleToRawLongBits(num) >> EXPONENT_SHIFT) & EXPONENT_MASK) - EXPONENT_BIAS;
   }

   /**
    * Checks whether a number is effectively zero, within the configured accuracy.
    * @param  number the number to check
    * @return        {@code true} if the number is considered zero
    */
   public static boolean isZero(double number) { return getExp(number) < ACCURACY; }

   /**
    * Returns zero if the given value is numerically close to zero.
    * <p>
    * This helps eliminate floating-point noise that may accumulate
    * in geometric calculations.
    * </p>
    * @param  number the number to align
    * @return        0.0 if the value is considered zero, otherwise the original
    *                value
    */
   public static double alignZero(double number) { return getExp(number) < ACCURACY ? 0.0 : number; }

   /**
    * Checks whether two numbers have the same sign.
    * <p>
    * Zero is considered neither positive nor negative.
    * </p>
    * @param  n1 first number
    * @param  n2 second number
    * @return    {@code true} if both numbers are positive or both are negative
    */
   public static boolean compareSign(double n1, double n2) { return (n1 > 0 && n2 > 0) || (n1 < 0 && n2 < 0); }

   /**
    * Returns a random double in the range {@code [min, max)}.
    * @param  min lower bound (inclusive)
    * @param  max upper bound (exclusive)
    * @return     random value in the specified range
    */
   public static double random(double min, double max) { return Math.random() * (max - min) + min; }

}
