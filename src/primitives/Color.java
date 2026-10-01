package primitives;

import static java.lang.Math.abs;

/**
 * Immutable RGB color with unbounded, non-negative {@code double} components.
 * <p>
 * Unlike {@link java.awt.Color}, components may exceed 255 so the same type can represent light
 * intensities; values are clamped only when converted for output by {@link #getColor()}.
 * Equality is tolerance-based (per-channel difference below {@link #DELTA}), which suits
 * rendering comparisons but makes the class unsuitable as a hash key.
 *
 * @author Dan Zilberstein
 */
public final class Color {
    /** Maximum value of an output color channel. */
    private static final int MAX = 255;
    /** Per-channel tolerance used by {@link #equals(Object)}. */
    private static final double DELTA = 0.5;

    /** The RGB components. */
    private final Double3 _rgb;

    /** Black - (0,0,0). */
    public static final Color BLACK = new Color();

    /** Creates black. */
    private Color() {
        _rgb = Double3.ZERO;
    }

    /**
     * Creates a color from RGB components.
     *
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @throws IllegalArgumentException if any component is negative
     */
    public Color(double r, double g, double b) {
        if (r < 0 || g < 0 || b < 0) throw new IllegalArgumentException("Negative color component is illegal");
        _rgb = new Double3(r, g, b);
    }

    /**
     * Creates a color from an RGB triad.
     *
     * @param rgb the RGB components
     * @throws IllegalArgumentException if any component is negative
     */
    private Color(Double3 rgb) {
        if (rgb._d1() < 0 || rgb._d2() < 0 || rgb._d3() < 0)
            throw new IllegalArgumentException("Negative color component is illegal");
        _rgb = rgb;
    }

    /**
     * Creates a color from a {@link java.awt.Color}.
     *
     * @param other the source color
     */
    public Color(java.awt.Color other) {
        _rgb = new Double3(other.getRed(), other.getGreen(), other.getBlue());
    }

    /**
     * Converts this color to a {@link java.awt.Color}, clamping every channel to 255.
     *
     * @return the displayable color
     */
    public java.awt.Color getColor() {
        int ir = (int) _rgb._d1();
        int ig = (int) _rgb._d2();
        int ib = (int) _rgb._d3();
        return new java.awt.Color(Math.min(ir, MAX), Math.min(ig, MAX), Math.min(ib, MAX));
    }

    /**
     * Adds colors to this color component-wise.
     *
     * @param colors the colors to add
     * @return the component-wise sum
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
     * Scales this color by per-channel factors.
     *
     * @param k the factors for the red, green and blue channels
     * @return the scaled color
     * @throws IllegalArgumentException if any factor is negative
     */
    public Color scale(Double3 k) {
        if (k._d1() < 0.0 || k._d2() < 0.0 || k._d3() < 0.0)
            throw new IllegalArgumentException("Can't scale a color by a negative number");
        return new Color(_rgb.product(k));
    }

    /**
     * Scales this color by a single factor.
     *
     * @param k the scale factor
     * @return the scaled color
     * @throws IllegalArgumentException if the factor is negative
     */
    public Color scale(double k) {
        if (k < 0.0) throw new IllegalArgumentException("Can't scale a color by a negative number");
        return new Color(_rgb.scale(k));
    }

    /**
     * Divides this color by an integer factor, typically to average accumulated samples.
     *
     * @param k the reduction factor
     * @return the reduced color
     * @throws IllegalArgumentException if {@code k} is less than 1
     */
    public Color reduce(int k) {
        if (k < 1) throw new IllegalArgumentException("Reduction factor must be at least 1");
        return new Color(_rgb.divide(k));
    }

    /**
     * Checks whether this color is within a tolerance of another color on every channel.
     *
     * @param other     the color to compare with
     * @param tolerance the maximum allowed difference per channel
     * @return {@code true} if the colors are similar
     */
    public boolean isSimilar(Color other, double tolerance) {
        return abs(_rgb._d1() - other._rgb._d1()) <= tolerance
                && abs(_rgb._d2() - other._rgb._d2()) <= tolerance
                && abs(_rgb._d3() - other._rgb._d3()) <= tolerance;
    }

    /**
     * Tolerant equality: every channel differs by less than {@link #DELTA}.
     * <p>
     * Approximate equality is not transitive, so colors should not be used as hash keys.
     *
     * @param obj the object to compare with
     * @return {@code true} if {@code obj} is a color approximately equal to this one
     */
    @Override
    public boolean equals(Object obj) {
        return this == obj
                || obj instanceof Color other
                && abs(_rgb._d1() - other._rgb._d1()) < DELTA
                && abs(_rgb._d2() - other._rgb._d2()) < DELTA
                && abs(_rgb._d3() - other._rgb._d3()) < DELTA;
    }

    /**
     * Checks that all the given colors are approximately equal to this color.
     *
     * @param colors the colors to compare with
     * @return {@code true} if every color equals this one (vacuously true for no colors)
     */
    public boolean equalColors(Color... colors) {
        for (Color color : colors)
            if (!this.equals(color)) return false;
        return true;
    }

    /**
     * Best-effort hash code based on {@link #DELTA}-sized quantization of the channels.
     * Provided to honor the {@code equals}/{@code hashCode} contract as far as tolerant
     * equality allows.
     *
     * @return a quantized hash code
     */
    @Override
    public int hashCode() {
        int result = Long.hashCode(Math.round(_rgb._d1() / DELTA));
        result = 31 * result + Long.hashCode(Math.round(_rgb._d2() / DELTA));
        result = 31 * result + Long.hashCode(Math.round(_rgb._d3() / DELTA));
        return result;
    }

    @Override
    public String toString() {
        return "rgb:" + _rgb;
    }
}
