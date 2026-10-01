package showcase;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Averages independent renders of a frame and removes the noise that is left.
 * <p>
 * Where the renders agree, a pixel is clean and is kept as it is. Where they disagree, their
 * spread measures how noisy the pixel is, and the pixel is blended with neighbours whose
 * brightness lies within that noise. Grainy areas - out-of-focus lights, blurred backgrounds,
 * penumbras - are smoothed, while edges, which differ by far more than the noise, stay sharp.
 * This is the edge-stopping filter guided by variance that real-time path tracers use.
 *
 * @author itzik123
 */
final class SubframeDenoiser {

    /** The distance, in pixels, over which a noisy pixel is blended */
    private static final int RADIUS = 4;
    /** The spread of the spatial weights, in pixels */
    private static final double SIGMA = RADIUS / 2.0;
    /** How many noise deviations apart two brightnesses may be and still be blended */
    private static final double STRENGTH = 4;
    /** Keeps the brightness test finite for pixels without noise */
    private static final double EPSILON = 1e-4;

    /** Utility class - not instantiable. */
    private SubframeDenoiser() {}

    /**
     * Averages renders of the same frame and smooths away their noise.
     *
     * @param renders at least two independent renders of the frame, all of the same size
     * @return the averaged and denoised frame
     * @throws IllegalArgumentException if there are fewer than two renders
     */
    static BufferedImage denoise(List<BufferedImage> renders) {
        int n = renders.size();
        if (n < 2) throw new IllegalArgumentException("The noise is measured from at least two renders");
        int width = renders.getFirst().getWidth(), height = renders.getFirst().getHeight();

        // The average color and brightness of every pixel, and the variance of the average brightness
        float[][] mean = new float[3][width * height];
        float[] luminance = new float[width * height], variance = new float[width * height];
        for (int y = 0, p = 0; y < height; y++)
            for (int x = 0; x < width; x++, p++) {
                double sum = 0, sumSquares = 0;
                for (BufferedImage render : renders) {
                    int rgb = render.getRGB(x, y);
                    double r = (rgb >> 16 & 0xFF) / 255.0, g = (rgb >> 8 & 0xFF) / 255.0, b = (rgb & 0xFF) / 255.0;
                    double l = 0.2126 * r + 0.7152 * g + 0.0722 * b;
                    mean[0][p] += (float) (r / n);
                    mean[1][p] += (float) (g / n);
                    mean[2][p] += (float) (b / n);
                    sum += l;
                    sumSquares += l * l;
                }
                luminance[p] = (float) (sum / n);
                variance[p] = (float) Math.max(0, (sumSquares - sum * sum / n) / (n - 1) / n);
            }
        // A few renders estimate the variance roughly, so it is averaged over each pixel's neighbourhood
        float[] noise = boxBlur(variance, width, height);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0, p = 0; y < height; y++)
            for (int x = 0; x < width; x++, p++) {
                double tolerance = STRENGTH * Math.sqrt(noise[p]) + EPSILON;
                double weights = 0, r = 0, g = 0, b = 0;
                for (int dy = Math.max(-RADIUS, -y); dy <= Math.min(RADIUS, height - 1 - y); dy++)
                    for (int dx = Math.max(-RADIUS, -x); dx <= Math.min(RADIUS, width - 1 - x); dx++) {
                        int q = p + dy * width + dx;
                        double weight = Math.exp(-(dx * dx + dy * dy) / (2 * SIGMA * SIGMA)
                                - Math.abs(luminance[p] - luminance[q]) / tolerance);
                        weights += weight;
                        r += weight * mean[0][q];
                        g += weight * mean[1][q];
                        b += weight * mean[2][q];
                    }
                image.setRGB(x, y, channel(r / weights) << 16 | channel(g / weights) << 8 | channel(b / weights));
            }
        return image;
    }

    /**
     * Averages every value of an image-shaped array with its eight neighbours.
     *
     * @param values the values, row by row
     * @param width  the width of the image
     * @param height the height of the image
     * @return the averaged values
     */
    private static float[] boxBlur(float[] values, int width, int height) {
        float[] result = new float[values.length];
        for (int y = 0, p = 0; y < height; y++)
            for (int x = 0; x < width; x++, p++) {
                double sum = 0;
                int count = 0;
                for (int dy = Math.max(-1, -y); dy <= Math.min(1, height - 1 - y); dy++)
                    for (int dx = Math.max(-1, -x); dx <= Math.min(1, width - 1 - x); dx++) {
                        sum += values[p + dy * width + dx];
                        count++;
                    }
                result[p] = (float) (sum / count);
            }
        return result;
    }

    /**
     * Converts a color channel from [0, 1] to [0, 255].
     *
     * @param value the channel value
     * @return the 8-bit channel value
     */
    private static int channel(double value) {
        return (int) Math.round(Math.clamp(value, 0, 1) * 255);
    }
}
