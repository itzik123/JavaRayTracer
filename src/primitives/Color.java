package primitives;

import static java.lang.Math.abs;

/**
 * Wrapper and extension of {@link java.awt.Color}.
 * <p>
 * This class represents a color using RGB components, similar to
 * {@code java.awt.Color}, but with the following differences:
 * <ul>
 * <li>Components are stored as non-negative {@code double} values.</li>
 * <li>No upper bound is enforced (values may exceed 255, e.g., for light
 * intensity).</li>
 * <li>Additional operations are provided for color arithmetic.</li>
 * </ul>
 * <p>
 * The class is immutable.
 * <p>
 * Equality is tolerance-based using {@code DELTA}. This is intended for
 * floating-point comparisons in rendering calculations and tests.
 * Because equality is approximate, this class is not well-suited for use as a
 * key in hash-based collections that rely on strict equality semantics.
 * @author Dan Zilberstein
 */
public final class Color {
   /** Maximum value for the pixel's color component */
   private static final int    MAX   = 255;
   /**
    * Tolerance used for comparing RGB components in equality checks.
    */
   private static final double DELTA = 0.5;

   /**
    * Internal RGB components stored as non-negative double values.
    */
   private final Double3       _rgb;

   /** The black color = (0,0,0) */
   public static final Color   BLACK = new Color();

   /** Private constructor for creating the constant black color. */
   private Color() { _rgb = Double3.ZERO; }

   /**
    * Creates a color from RGB components.
    * <p>
    * Each component must be non-negative. Values greater than 255 are allowed
    * and may be useful for representing light intensities.
    * @param  r                        the red component
    * @param  g                        the green component
    * @param  b                        the blue component
    * @throws IllegalArgumentException if any component is negative
    */
   public Color(double r, double g, double b) {
      if (r < 0 || g < 0 || b < 0) throw new IllegalArgumentException("Negative color component is illegal");
      _rgb = new Double3(r, g, b);
   }

   /**
    * Creates a color from an RGB triad.
    * @param  rgb                      the RGB components
    * @throws IllegalArgumentException if any component is negative
    */
   private Color(Double3 rgb) {
      if (rgb._d1() < 0 || rgb._d2() < 0 || rgb._d3() < 0)
         throw new IllegalArgumentException("Negative color component is illegal");
      this._rgb = rgb;
   }

   /**
    * Creates a color from a {@link java.awt.Color} object.
    * @param other the source color
    */
   public Color(java.awt.Color other) { _rgb = new Double3(other.getRed(), other.getGreen(), other.getBlue()); }

   /**
    * Converts this color to a {@link java.awt.Color}.
    * <p>
    * Any component greater than 255 is clamped to 255.
    * @return a {@code java.awt.Color} representing this color
    */
   public java.awt.Color getColor() {
      int ir = (int) _rgb._d1();
      int ig = (int) _rgb._d2();
      int ib = (int) _rgb._d3();
      return new java.awt.Color(ir > MAX ? MAX : ir, ig > MAX ? MAX : ig, ib > MAX ? MAX : ib);
   }

   /**
    * Adds one or more colors to this color component-wise.
    * @param  colors the colors to add
    * @return        a new color equal to the component-wise sum
    */
   public Color add(Color... colors) {
      double rr = _rgb._d1();
      double rg = _rgb._d2();
      double rb = _rgb._d3();
      for (Color c : colors) {
         rr += c._rgb._d1();
         rg += c._rgb._d2();
         rb += c._rgb._d3();
      }
      return new Color(rr, rg, rb);
   }

   /**
    * Scales this color component-wise by the given factors.
    * @param  k                        the scale factors for the RGB components
    * @return                          a new scaled color
    * @throws IllegalArgumentException if any scale factor is negative
    */
   public Color scale(Double3 k) {
      if (k._d1() < 0.0 || k._d2() < 0.0 || k._d3() < 0.0)
         throw new IllegalArgumentException("Can't scale a color by a negative number");
      return new Color(_rgb.product(k));
   }

   /**
    * Scales this color by the given factor.
    * @param  k                        the scale factor
    * @return                          a new scaled color
    * @throws IllegalArgumentException if the scale factor is negative
    */
   public Color scale(double k) {
      if (k < 0.0) throw new IllegalArgumentException("Can't scale a color by a negative number");
      return new Color(_rgb.scale(k));
   }

   /**
    * Reduces this color by dividing each component by the given factor.
    * @param  k                        the reduction factor
    * @return                          a new reduced color
    * @throws IllegalArgumentException if {@code k} is less than 1
    */
   public Color reduce(int k) {
      if (k < 1) throw new IllegalArgumentException("Reduction factor must be at least 1");
      return new Color(_rgb.divide(k));
   }

   /**
    * Tolerant equality based on {@code DELTA} per RGB component.
    * Intended for floating-point comparisons in rendering and tests.
    * <p>
    * Note: This is approximate equality (not strictly transitive) and is not
    * suitable for use in hash-based collections.
    * @param  obj the object to compare with
    * @return     <b>{@code true}</b> if the given object is a {@code Color} whose
    *             RGB components differ from this color by less than {@code DELTA};
    *             <b>{@code false}</b> otherwise
    */
   @Override
   public boolean equals(Object obj) {
      return this == obj ||
            (obj instanceof Color other &&
                  abs(_rgb._d1() - other._rgb._d1()) < DELTA
                  && abs(_rgb._d2() - other._rgb._d2()) < DELTA
                  && abs(_rgb._d3() - other._rgb._d3()) < DELTA);
   }

   /**
    * Compares the given colors to this color using tolerant equality.
    * @param  colors colors to compare with this color
    * @return        {@code true} if all given colors are approximately equal to
    *                this color (vacuously {@code true} if no colors are provided);
    *                {@code false} otherwise
    */
   public boolean equalColors(Color... colors) {
      for (Color color : colors)
         if (!this.equals(color)) return false;
      return true;
   }

   /**
    * Returns a hash code based on DELTA-sized quantization of the RGB components.
    * <p>
    * This implementation exists mainly to satisfy the general requirement that
    * classes overriding {@link #equals(Object)} also override {@link #hashCode()}.
    * Since {@code equals} is tolerance-based, this hash code is only a best-effort
    * approximation and should not be relied upon in hash-based collections that
    * assume strict equality semantics.
    * @return a quantized hash code for this color
    */
   @Override
   public int hashCode() {
      long q1     = Math.round(_rgb._d1() / DELTA);
      long q2     = Math.round(_rgb._d2() / DELTA);
      long q3     = Math.round(_rgb._d3() / DELTA);

      int  result = Long.hashCode(q1);
      result = 31 * result + Long.hashCode(q2);
      result = 31 * result + Long.hashCode(q3);
      return result;
   }

   @Override
   public String toString() { return "rgb:" + _rgb; }
}
