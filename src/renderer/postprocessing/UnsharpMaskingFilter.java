package renderer.postprocessing;

import static renderer.postprocessing.Pixels.blue;
import static renderer.postprocessing.Pixels.green;
import static renderer.postprocessing.Pixels.luma;
import static renderer.postprocessing.Pixels.red;
import static renderer.postprocessing.Pixels.rgb;

/**
 * Sharpens an image with unsharp masking: the difference between the image and a blurred copy
 * of it isolates the fine detail, which is amplified and added back.
 * <p>
 * Pixels whose brightness differs from the blurred copy by less than a threshold are left
 * untouched, so smooth areas such as skies do not get their noise amplified.
 *
 * @author itzik123
 */
public class UnsharpMaskingFilter extends PixelFilter {

    /** The radius of the blur that defines "fine detail", in pixels. */
    private int _blurRadius = 2;
    /** How strongly the detail is amplified. */
    private float _amount = 1.0f;
    /** The minimum brightness difference (0-255) that gets sharpened. */
    private int _threshold = 10;

    /** Creates an unsharp masking filter with moderate default settings. */
    public UnsharpMaskingFilter() {
    }

    /**
     * Sets the radius of the detail-separating blur, in pixels.
     *
     * @param blurRadius the radius of the detail-separating blur, in pixels
     * @return this filter
     * @throws IllegalArgumentException if the radius is negative
     */
    public UnsharpMaskingFilter setBlurRadius(int blurRadius) {
        if (blurRadius < 0) throw new IllegalArgumentException("Blur radius must be non-negative");
        _blurRadius = blurRadius;
        return this;
    }

    /**
     * Sets how strongly the detail is amplified.
     *
     * @param amount how strongly the detail is amplified
     * @return this filter
     */
    public UnsharpMaskingFilter setAmount(float amount) {
        _amount = amount;
        return this;
    }

    /**
     * Sets the minimum brightness difference (0-255) that gets sharpened.
     *
     * @param threshold the minimum brightness difference (0-255) that gets sharpened
     * @return this filter
     */
    public UnsharpMaskingFilter setThreshold(int threshold) {
        _threshold = threshold;
        return this;
    }

    @Override
    protected int[] process(int[] pixels, int width, int height) {
        int[] blurred = Pixels.boxBlur(pixels, width, height, _blurRadius);
        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) result[i] = sharpen(pixels[i], blurred[i]);
        return result;
    }

    /**
     * Sharpens one pixel.
     *
     * @param original the original pixel
     * @param blurred  the blurred pixel
     * @return the sharpened pixel
     */
    private int sharpen(int original, int blurred) {
        if (Math.abs(luma(original) - luma(blurred)) * 255.0f < _threshold) return original;
        return rgb(amplify(red(original), red(blurred)),
                amplify(green(original), green(blurred)),
                amplify(blue(original), blue(blurred)));
    }

    /**
     * Amplifies the detail of one channel.
     *
     * @param original the original channel value
     * @param blurred  the blurred channel value
     * @return the channel value with amplified detail, unclamped
     */
    private int amplify(int original, int blurred) {
        return (int) (original + _amount * (original - blurred));
    }
}
