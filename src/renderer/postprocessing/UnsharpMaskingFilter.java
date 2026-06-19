package renderer.postprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Advanced Unsharp Masking (USM) Filter.
 * Now includes a Threshold parameter to protect smooth areas (like skin or skies) from noise amplification.
 */
public class UnsharpMaskingFilter implements PostProcessFilter {

    private int _blurRadius = 2;
    private float _amount = 1.0f;

    /** * סף האבחנה (0 עד 255).
     * ערכים קטנים (כמו 5-15) ימנעו חידוד של רעש בעור חלק וישאירו רק את קצוות הריסים.
     */
    private int _threshold = 10;

    public UnsharpMaskingFilter setBlurRadius(int blurRadius) { this._blurRadius = blurRadius; return this; }
    public UnsharpMaskingFilter setAmount(float amount) { this._amount = amount; return this; }
    public UnsharpMaskingFilter setThreshold(int threshold) { this._threshold = threshold; return this; }

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] originalPixels = image.getRGB(0, 0, width, height, null, 0, width);
        int[] blurredHorizontally = applyHorizontalBlur(originalPixels, width, height, _blurRadius);
        int[] blurredPixels = applyVerticalBlur(blurredHorizontally, width, height, _blurRadius);

        int[] resultPixels = new int[originalPixels.length];
        for (int i = 0; i < originalPixels.length; i++) {
            resultPixels[i] = sharpenPixel(originalPixels[i], blurredPixels[i], _amount, _threshold);
        }

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, width, height, resultPixels, 0, width);
        return result;
    }

    private int sharpenPixel(int orig, int blur, float amount, int threshold) {
        float lumaOrig = getLuma(orig);
        float lumaBlur = getLuma(blur);

        // --- מנגנון ה-Threshold (הקסם החדש!) ---
        // בודקים האם ההבדל בבהירות גדול מהסף שהגדרנו.
        // אם ההבדל קטן (למשל בעור חלק), נחזיר את הפיקסל המקורי ללא חידוד!
        if (Math.abs(lumaOrig - lumaBlur) * 255.0f < threshold) {
            return orig;
        }

        int oR = (orig >> 16) & 0xFF;
        int oG = (orig >> 8) & 0xFF;
        int oB = orig & 0xFF;

        int bR = (blur >> 16) & 0xFF;
        int bG = (blur >> 8) & 0xFF;
        int bB = blur & 0xFF;

        int r = (int) (oR + amount * (oR - bR));
        int g = (int) (oG + amount * (oG - bG));
        int b = (int) (oB + amount * (oB - bB));

        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));

        return new Color(r, g, b).getRGB();
    }

    private float getLuma(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;
    }

    // ... מתודות הטשטוש (applyHorizontalBlur, applyVerticalBlur, computeAverage) נשארות זהות לחלוטין לקוד הקודם ...
    private int[] applyHorizontalBlur(int[] pixels, int width, int height, int radius) {
        int[] result = new int[pixels.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result[y * width + x] = computeAverage(pixels, x, y, width, height, radius, true);
            }
        }
        return result;
    }

    private int[] applyVerticalBlur(int[] pixels, int width, int height, int radius) {
        int[] result = new int[pixels.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result[y * width + x] = computeAverage(pixels, x, y, width, height, radius, false);
            }
        }
        return result;
    }

    private int computeAverage(int[] pixels, int x, int y, int width, int height, int radius, boolean horizontal) {
        int sumR = 0, sumG = 0, sumB = 0;
        int count = 0;

        for (int i = -radius; i <= radius; i++) {
            int currentX = horizontal ? x + i : x;
            int currentY = horizontal ? y : y + i;

            if (currentX >= 0 && currentX < width && currentY >= 0 && currentY < height) {
                int color = pixels[currentY * width + currentX];
                sumR += (color >> 16) & 0xFF;
                sumG += (color >> 8) & 0xFF;
                sumB += color & 0xFF;
                count++;
            }
        }

        if (count == 0) return 0;
        return new Color(sumR / count, sumG / count, sumB / count).getRGB();
    }
}