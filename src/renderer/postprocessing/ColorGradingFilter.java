package renderer.postprocessing;

import static renderer.postprocessing.Pixels.LUMA_B;
import static renderer.postprocessing.Pixels.LUMA_G;
import static renderer.postprocessing.Pixels.LUMA_R;
import static renderer.postprocessing.Pixels.blue;
import static renderer.postprocessing.Pixels.green;
import static renderer.postprocessing.Pixels.red;

/**
 * Color grading for the final look of the image: exposure, contrast, gamma and saturation, applied
 * in that order. Every parameter defaults to 1, which leaves the image unchanged.
 *
 * @author itzik123
 */
public class ColorGradingFilter extends PixelFilter {

    /** Linear brightness multiplier. */
    private float _exposure = 1.0f;
    /** Gamma correction; values above 1 brighten the mid-tones. */
    private float _gamma = 1.0f;
    /** Contrast around mid-gray. */
    private float _contrast = 1.0f;
    /** Color saturation; 0 gives grayscale. */
    private float _saturation = 1.0f;

    /** Creates a color grading filter that leaves the image unchanged until configured. */
    public ColorGradingFilter() {
    }

    /**
     * Sets the linear brightness multiplier.
     *
     * @param exposure the linear brightness multiplier
     * @return this filter
     */
    public ColorGradingFilter setExposure(float exposure) {
        _exposure = exposure;
        return this;
    }

    /**
     * Sets the gamma correction; values above 1 brighten the mid-tones.
     *
     * @param gamma the gamma correction; values above 1 brighten the mid-tones
     * @return this filter
     * @throws IllegalArgumentException if gamma is not positive
     */
    public ColorGradingFilter setGamma(float gamma) {
        if (gamma <= 0) throw new IllegalArgumentException("Gamma must be positive");
        _gamma = gamma;
        return this;
    }

    /**
     * Sets the contrast around mid-gray.
     *
     * @param contrast the contrast around mid-gray
     * @return this filter
     */
    public ColorGradingFilter setContrast(float contrast) {
        _contrast = contrast;
        return this;
    }

    /**
     * Sets the color saturation; 0 gives grayscale.
     *
     * @param saturation the color saturation; 0 gives grayscale
     * @return this filter
     */
    public ColorGradingFilter setSaturation(float saturation) {
        _saturation = saturation;
        return this;
    }

    @Override
    protected int[] process(int[] pixels, int width, int height) {
        float invGamma = 1.0f / _gamma;
        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            float r = grade(red(pixels[i]) / 255.0f, invGamma);
            float g = grade(green(pixels[i]) / 255.0f, invGamma);
            float b = grade(blue(pixels[i]) / 255.0f, invGamma);

            float luma = LUMA_R * r + LUMA_G * g + LUMA_B * b;
            r = clamp(luma + _saturation * (r - luma));
            g = clamp(luma + _saturation * (g - luma));
            b = clamp(luma + _saturation * (b - luma));

            // Round rather than truncate, or float error would darken every pixel by one level
            result[i] = Math.round(r * 255) << 16 | Math.round(g * 255) << 8 | Math.round(b * 255);
        }
        return result;
    }

    /**
     * Applies exposure, contrast and gamma to one channel.
     *
     * @param value    the channel value, 0-1
     * @param invGamma the reciprocal of the gamma
     * @return the graded value, 0-1
     */
    private float grade(float value, float invGamma) {
        // Clamp before the gamma curve, which is undefined for negative values
        value = clamp((value * _exposure - 0.5f) * _contrast + 0.5f);
        return _gamma == 1.0f ? value : (float) Math.pow(value, invGamma);
    }

    /**
     * Clamps a channel value to the displayable range.
     *
     * @param value a channel value
     * @return the value clamped to [0, 1]
     */
    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
