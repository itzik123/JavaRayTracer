package renderer.postprocessing;

/**
 * Helpers for packed {@code 0xRRGGBB} pixels shared by the filters.
 *
 * @author itzik123
 */
final class Pixels {

    /** Rec. 601 luma weight of the red channel. */
    static final float LUMA_R = 0.299f;
    /** Rec. 601 luma weight of the green channel. */
    static final float LUMA_G = 0.587f;
    /** Rec. 601 luma weight of the blue channel. */
    static final float LUMA_B = 0.114f;

    /** Utility class - not instantiable. */
    private Pixels() {}

    /**
     * Extracts the red channel of a pixel.
     *
     * @param rgb a packed pixel
     * @return the red channel, 0-255
     */
    static int red(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    /**
     * Extracts the green channel of a pixel.
     *
     * @param rgb a packed pixel
     * @return the green channel, 0-255
     */
    static int green(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    /**
     * Extracts the blue channel of a pixel.
     *
     * @param rgb a packed pixel
     * @return the blue channel, 0-255
     */
    static int blue(int rgb) {
        return rgb & 0xFF;
    }

    /**
     * Packs three channels into a pixel, clamping each to 0-255.
     *
     * @param r the red channel
     * @param g the green channel
     * @param b the blue channel
     * @return the packed pixel
     */
    static int rgb(int r, int g, int b) {
        return clamp(r) << 16 | clamp(g) << 8 | clamp(b);
    }

    /**
     * Clamps a channel value to the displayable range.
     *
     * @param channel a channel value
     * @return the value clamped to 0-255
     */
    static int clamp(int channel) {
        return Math.max(0, Math.min(255, channel));
    }

    /**
     * Calculates the perceived brightness of a pixel.
     *
     * @param rgb a packed pixel
     * @return the luma, 0-1
     */
    static float luma(int rgb) {
        return (red(rgb) * LUMA_R + green(rgb) * LUMA_G + blue(rgb) * LUMA_B) / 255.0f;
    }

    /**
     * Blurs an image with a separable box filter: a horizontal pass followed by a vertical pass.
     * Each output pixel is the average of the pixels within {@code radius} of it that lie inside
     * the image. A running window sum makes the cost independent of the radius.
     *
     * @param pixels the source pixels
     * @param width  the image width
     * @param height the image height
     * @param radius the blur radius, in pixels
     * @return the blurred pixels
     */
    static int[] boxBlur(int[] pixels, int width, int height, int radius) {
        int[] horizontal = new int[pixels.length];
        for (int y = 0; y < height; y++) blurLine(pixels, horizontal, y * width, 1, width, radius);

        int[] result = new int[pixels.length];
        for (int x = 0; x < width; x++) blurLine(horizontal, result, x, width, height, radius);
        return result;
    }

    /**
     * Box-blurs one row or column.
     *
     * @param src    the source pixels
     * @param dst    the destination pixels
     * @param start  the index of the first pixel of the line
     * @param stride the index distance between consecutive pixels of the line
     * @param length the number of pixels in the line
     * @param radius the blur radius
     */
    private static void blurLine(int[] src, int[] dst, int start, int stride, int length, int radius) {
        int sumR = 0, sumG = 0, sumB = 0;
        // Prime the window with the pixels to the right of the first one
        for (int i = 0; i <= Math.min(radius, length - 1); i++) {
            int p = src[start + i * stride];
            sumR += red(p);
            sumG += green(p);
            sumB += blue(p);
        }
        for (int i = 0; i < length; i++) {
            int count = Math.min(i + radius, length - 1) - Math.max(i - radius, 0) + 1;
            dst[start + i * stride] = sumR / count << 16 | sumG / count << 8 | sumB / count;

            // Slide the window one pixel forward
            int leaving = i - radius;
            if (leaving >= 0) {
                int p = src[start + leaving * stride];
                sumR -= red(p);
                sumG -= green(p);
                sumB -= blue(p);
            }
            int entering = i + radius + 1;
            if (entering < length) {
                int p = src[start + entering * stride];
                sumR += red(p);
                sumG += green(p);
                sumB += blue(p);
            }
        }
    }
}
