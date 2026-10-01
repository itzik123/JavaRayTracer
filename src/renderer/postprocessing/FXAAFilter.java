package renderer.postprocessing;

import static renderer.postprocessing.Pixels.blue;
import static renderer.postprocessing.Pixels.green;
import static renderer.postprocessing.Pixels.luma;
import static renderer.postprocessing.Pixels.red;

import java.util.Random;

/**
 * Fast Approximate Anti-Aliasing: smooths jagged edges on the final image instead of tracing more
 * rays.
 * <p>
 * For every pixel on a high-contrast edge, the filter decides whether the edge is horizontal or
 * vertical, walks along it in both directions to find where it ends, and blends the pixel with
 * its neighbor across the edge - more strongly near the ends of a staircase step. A small random
 * jitter on the blend factor, independent per channel, hides the regular banding a fixed blend
 * can leave on long, shallow edges.
 * <p>
 * The one-pixel border of the image is left unchanged.
 *
 * @author itzik123
 */
public class FXAAFilter extends PixelFilter {

    /** Minimum absolute local contrast that counts as an edge, so dark noise is ignored. */
    private static final float DARK_THRESHOLD = 0.0312f;
    /** Upper bound of the blend factor before jitter. */
    private static final float MAX_BLEND = 0.6f;
    /** Upper bound of the blend factor after jitter. */
    private static final float MAX_JITTERED_BLEND = 0.8f;

    /** Minimum local contrast, relative to the brightest neighbor, that counts as an edge. */
    private float _edgeThreshold = 0.125f;
    /** How far, in pixels, to follow an edge in each direction. */
    private int _maxSearchSteps = 24;
    /** The amplitude of the random blend jitter (0 disables it). */
    private float _jitterStrength = 0.08f;
    /** The source of the blend jitter. */
    private final Random _random = new Random();

    /** Creates an FXAA filter with default settings. */
    public FXAAFilter() {
    }

    /**
     * Sets the minimum local contrast, relative to the brightest neighbor, that counts as an edge;
     * lower values smooth more edges.
     *
     * @param edgeThreshold the minimum local contrast, relative to the brightest neighbor, that
     *                      counts as an edge; lower values smooth more edges
     * @return this filter
     */
    public FXAAFilter setEdgeThreshold(float edgeThreshold) {
        _edgeThreshold = edgeThreshold;
        return this;
    }

    /**
     * Sets how far, in pixels, to follow an edge in each direction.
     *
     * @param maxSearchSteps how far, in pixels, to follow an edge in each direction
     * @return this filter
     */
    public FXAAFilter setMaxSearchSteps(int maxSearchSteps) {
        _maxSearchSteps = maxSearchSteps;
        return this;
    }

    /**
     * Sets the amplitude of the random blend jitter; 0 gives classic FXAA.
     *
     * @param jitterStrength the amplitude of the random blend jitter; 0 gives classic FXAA
     * @return this filter
     */
    public FXAAFilter setJitterStrength(float jitterStrength) {
        _jitterStrength = jitterStrength;
        return this;
    }

    @Override
    protected int[] process(int[] pixels, int width, int height) {
        float[] lumas = new float[pixels.length];
        for (int i = 0; i < pixels.length; i++) lumas[i] = luma(pixels[i]);

        int[] result = pixels.clone();
        for (int y = 1; y < height - 1; y++)
            for (int x = 1; x < width - 1; x++)
                result[y * width + x] = smooth(pixels, lumas, x, y, width, height);
        return result;
    }

    /**
     * Computes the anti-aliased color of one interior pixel.
     *
     * @param pixels the source pixels
     * @param lumas  the luma of every source pixel
     * @param x      the pixel column
     * @param y      the pixel row
     * @param width  the image width
     * @param height the image height
     * @return the smoothed pixel
     */
    private int smooth(int[] pixels, float[] lumas, int x, int y, int width, int height) {
        int index = y * width + x;
        float lumaM = lumas[index];
        float lumaN = lumas[index - width];
        float lumaS = lumas[index + width];
        float lumaE = lumas[index + 1];
        float lumaW = lumas[index - 1];

        // 1. Skip pixels that are not on a visible edge
        float minLuma = Math.min(lumaM, Math.min(Math.min(lumaN, lumaS), Math.min(lumaE, lumaW)));
        float maxLuma = Math.max(lumaM, Math.max(Math.max(lumaN, lumaS), Math.max(lumaE, lumaW)));
        float contrast = maxLuma - minLuma;
        if (contrast < Math.max(DARK_THRESHOLD, maxLuma * _edgeThreshold)) return pixels[index];

        // 2. Classify the edge as horizontal or vertical from the 3x3 neighborhood
        float lumaNW = lumas[index - width - 1];
        float lumaNE = lumas[index - width + 1];
        float lumaSW = lumas[index + width - 1];
        float lumaSE = lumas[index + width + 1];
        float edgeHorizontal = Math.abs(0.25f * lumaNW - 0.5f * lumaN + 0.25f * lumaNE)
                + Math.abs(0.50f * lumaW - lumaM + 0.50f * lumaE)
                + Math.abs(0.25f * lumaSW - 0.5f * lumaS + 0.25f * lumaSE);
        float edgeVertical = Math.abs(0.25f * lumaNW - 0.5f * lumaW + 0.25f * lumaSW)
                + Math.abs(0.50f * lumaN - lumaM + 0.50f * lumaS)
                + Math.abs(0.25f * lumaNE - 0.5f * lumaE + 0.25f * lumaSE);
        boolean horizontal = edgeHorizontal >= edgeVertical;

        // 3. Pick the neighbor across the edge - on the side with the steeper gradient
        boolean positive = horizontal
                ? Math.abs(lumaS - lumaM) >= Math.abs(lumaN - lumaM)
                : Math.abs(lumaE - lumaM) >= Math.abs(lumaW - lumaM);
        int stepX = horizontal ? 0 : positive ? 1 : -1;
        int stepY = horizontal ? positive ? 1 : -1 : 0;
        int neighborX = x + stepX;
        int neighborY = y + stepY;
        float lumaNeighbor = lumaAt(lumas, neighborX, neighborY, width, height);

        // 4. Walk along the edge in both directions until the edge luma changes
        float edgeLuma = (lumaM + lumaNeighbor) * 0.5f;
        float gradientThreshold = Math.abs(lumaM - lumaNeighbor) * 0.25f;
        int alongX = horizontal ? 1 : 0;
        int alongY = horizontal ? 0 : 1;
        float[] endLuma = new float[2];
        int distanceNegative = searchEdgeEnd(lumas, x, y, -alongX, -alongY, stepX, stepY, width, height,
                edgeLuma, gradientThreshold, endLuma, 0);
        int distancePositive = searchEdgeEnd(lumas, x, y, alongX, alongY, stepX, stepY, width, height,
                edgeLuma, gradientThreshold, endLuma, 1);

        // 5. Blend more near the closer end of the step, if that end actually crosses the edge
        boolean negativeCloser = distanceNegative < distancePositive;
        float lumaEnd = negativeCloser ? endLuma[0] : endLuma[1];
        float spanBlend = (lumaM >= edgeLuma) != (lumaEnd >= edgeLuma)
                ? 0.5f - (float) Math.min(distanceNegative, distancePositive) / (distanceNegative + distancePositive + 1)
                : 0.0f;

        // 6. Sub-pixel blending for thin features that differ from their surroundings
        float localAverage = (lumaN + lumaS + lumaE + lumaW) * 0.25f;
        float subPixelOffset = Math.max(0.0f, Math.min(1.0f, Math.abs(localAverage - lumaM) / contrast));
        float blend = Math.max(0.0f, Math.min(MAX_BLEND, Math.max(spanBlend, subPixelOffset * subPixelOffset)));

        // 7. Blend with the neighbor, jittering the factor per channel
        int center = pixels[index];
        int neighbor = pixels[clampIndex(neighborX, neighborY, width, height)];
        return mix(red(center), red(neighbor), jitter(blend)) << 16
                | mix(green(center), green(neighbor), jitter(blend)) << 8
                | mix(blue(center), blue(neighbor), jitter(blend));
    }

    /**
     * Follows an edge in one direction until the average luma across it departs from the edge
     * luma, or the search limit is reached.
     *
     * @param lumas             the luma of every pixel
     * @param x                 the start column
     * @param y                 the start row
     * @param dirX              the column step along the edge
     * @param dirY              the row step along the edge
     * @param acrossX           the column step across the edge
     * @param acrossY           the row step across the edge
     * @param width             the image width
     * @param height            the image height
     * @param edgeLuma          the luma of the edge at the start pixel
     * @param gradientThreshold the luma change that marks the end of the edge
     * @param endLuma           receives the luma at the end of the search
     * @param endSlot           the slot of {@code endLuma} to write
     * @return the number of steps taken
     */
    private int searchEdgeEnd(float[] lumas, int x, int y, int dirX, int dirY, int acrossX, int acrossY,
                              int width, int height, float edgeLuma, float gradientThreshold,
                              float[] endLuma, int endSlot) {
        int px = x + dirX;
        int py = y + dirY;
        int distance = 0;
        while (distance < _maxSearchSteps) {
            float luma = (lumaAt(lumas, px, py, width, height)
                    + lumaAt(lumas, px + acrossX, py + acrossY, width, height)) * 0.5f;
            endLuma[endSlot] = luma;
            if (Math.abs(luma - edgeLuma) >= gradientThreshold) break;
            px += dirX;
            py += dirY;
            distance++;
        }
        return distance;
    }

    /**
     * Adds random jitter to a blend factor.
     *
     * @param blend the blend factor
     * @return the factor with random jitter, clamped
     */
    private float jitter(float blend) {
        return Math.max(0.0f, Math.min(MAX_JITTERED_BLEND, blend + (_random.nextFloat() - 0.5f) * _jitterStrength));
    }

    /**
     * Interpolates linearly between two channel values.
     *
     * @param a the first value
     * @param b the second value
     * @param t the weight of {@code b}
     * @return the linear interpolation between the values
     */
    private static int mix(int a, int b, float t) {
        return (int) (a * (1 - t) + b * t);
    }

    /**
     * Looks up the luma of a pixel, clamping the position to the image.
     *
     * @param lumas  the luma of every pixel
     * @param x      a column, clamped to the image
     * @param y      a row, clamped to the image
     * @param width  the image width
     * @param height the image height
     * @return the luma of the pixel
     */
    private static float lumaAt(float[] lumas, int x, int y, int width, int height) {
        return lumas[clampIndex(x, y, width, height)];
    }

    /**
     * Converts a pixel position, clamped to the image, to an array index.
     *
     * @param x      a column, clamped to the image
     * @param y      a row, clamped to the image
     * @param width  the image width
     * @param height the image height
     * @return the array index of the pixel
     */
    private static int clampIndex(int x, int y, int width, int height) {
        return Math.max(0, Math.min(y, height - 1)) * width + Math.max(0, Math.min(x, width - 1));
    }
}
