package renderer.postprocessing;

import static renderer.postprocessing.Pixels.blue;
import static renderer.postprocessing.Pixels.green;
import static renderer.postprocessing.Pixels.luma;
import static renderer.postprocessing.Pixels.red;

/**
 * Simulates the glow ("bloom") that bright light produces in a camera lens.
 * <p>
 * Pixels brighter than a threshold are extracted, blurred into a halo and combined with the
 * original image using screen blending, {@code 1 - (1 - a)(1 - b)}, which brightens without ever
 * exceeding white.
 *
 * @author itzik123
 */
public class BloomFilter extends PixelFilter {

    /** The luma above which a pixel contributes to the glow. */
    private float _threshold = 0.90f;
    /** The radius of the glow, in pixels. */
    private int _blurRadius = 10;
    /** The strength of the glow when blended back. */
    private float _intensity = 0.5f;

    /** Creates a bloom filter with moderate default settings. */
    public BloomFilter() {
    }

    /**
     * Sets the luma above which a pixel glows, in [0, 1).
     *
     * @param threshold the luma above which a pixel glows, in [0, 1)
     * @return this filter
     * @throws IllegalArgumentException if the threshold is out of range
     */
    public BloomFilter setThreshold(float threshold) {
        if (threshold < 0 || threshold >= 1) throw new IllegalArgumentException("Threshold must be in [0, 1)");
        _threshold = threshold;
        return this;
    }

    /**
     * Sets the radius of the glow, in pixels.
     *
     * @param blurRadius the radius of the glow, in pixels
     * @return this filter
     * @throws IllegalArgumentException if the radius is negative
     */
    public BloomFilter setBlurRadius(int blurRadius) {
        if (blurRadius < 0) throw new IllegalArgumentException("Blur radius must be non-negative");
        _blurRadius = blurRadius;
        return this;
    }

    /**
     * Sets the strength of the glow, in [0, 1].
     *
     * @param intensity the strength of the glow, in [0, 1]
     * @return this filter
     */
    public BloomFilter setIntensity(float intensity) {
        _intensity = intensity;
        return this;
    }

    @Override
    protected int[] process(int[] pixels, int width, int height) {
        int[] bright = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            float luma = luma(pixels[i]);
            if (luma > _threshold) {
                // Keep only the part of the light above the threshold, so the halo fades in smoothly
                float factor = (luma - _threshold) / (1.0f - _threshold);
                int p = pixels[i];
                bright[i] = (int) (red(p) * factor) << 16 | (int) (green(p) * factor) << 8 | (int) (blue(p) * factor);
            }
        }

        int[] glow = Pixels.boxBlur(bright, width, height, _blurRadius);
        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) result[i] = screenBlend(pixels[i], glow[i]);
        return result;
    }

    /**
     * Blends the glow into a pixel with the screen formula.
     *
     * @param original the original pixel
     * @param glow     the glow pixel
     * @return the blended pixel
     */
    private int screenBlend(int original, int glow) {
        return screen(red(original), (int) (red(glow) * _intensity)) << 16
                | screen(green(original), (int) (green(glow) * _intensity)) << 8
                | screen(blue(original), (int) (blue(glow) * _intensity));
    }

    /**
     * Screen-blends two channel values.
     *
     * @param a the first channel value
     * @param b the second channel value
     * @return the screen blend of the two values
     */
    private static int screen(int a, int b) {
        return 255 - (255 - a) * (255 - b) / 255;
    }
}
