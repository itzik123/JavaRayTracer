package renderer.postprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * A realistic post-processing filter that simulates the optical effect of "Bloom" or Glow.
 * Fixed version: Prevents color washout and accurately extracts only highly luminous points.
 */
public class BloomFilter implements PostProcessFilter {

    /** * הסף שממנו פיקסל נחשב "בוהק".
     * הועלה משמעותית כדי לחלץ רק כתמי אור (Specular) חזקים ולא את האובייקטים עצמם.
     */
    private float _threshold = 0.90f;

    private int _blurRadius = 10;

    /** עוצמת השילוב העדינה */
    private float _intensity = 0.5f;

    public BloomFilter setThreshold(float threshold) {
        this._threshold = threshold;
        return this;
    }

    public BloomFilter setBlurRadius(int blurRadius) {
        this._blurRadius = blurRadius;
        return this;
    }

    public BloomFilter setIntensity(float intensity) {
        this._intensity = intensity;
        return this;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] originalPixels = image.getRGB(0, 0, width, height, null, 0, width);
        int[] brightPixels = new int[originalPixels.length];

        // 1. שלב החילוץ (Threshold): סינון קפדני מאוד! רק פיקסלים סופר-בוהקים
        for (int i = 0; i < originalPixels.length; i++) {
            Color c = new Color(originalPixels[i]);
            // דרך בטוחה יותר לחלץ אורות חזקים - בדיקה אם כל הערוצים גבוהים, או Luma קיצוני
            float luma = getLuma(originalPixels[i]);
            if (luma > _threshold || (c.getRed() > 240 && c.getGreen() > 240 && c.getBlue() > 240)) {
                // חילוץ הפיקסל, אך החסרת הסף כדי לשמור רק על האור ש"זולג" מעליו
                float factor = (luma - _threshold) / (1.0f - _threshold);
                int r = (int)(c.getRed() * factor);
                int g = (int)(c.getGreen() * factor);
                int b = (int)(c.getBlue() * factor);
                brightPixels[i] = new Color(r, g, b).getRGB();
            } else {
                brightPixels[i] = 0; // שחור
            }
        }

        // 2. שלב הטשטוש (Gaussian/Box Blur)
        int[] blurredHorizontally = applyHorizontalBlur(brightPixels, width, height, _blurRadius);
        int[] fullyBlurred = applyVerticalBlur(blurredHorizontally, width, height, _blurRadius);

        // 3. שלב החיבור החכם (Screen Blending) כדי למנוע שריפה
        int[] resultPixels = new int[originalPixels.length];
        for (int i = 0; i < originalPixels.length; i++) {
            resultPixels[i] = screenBlend(originalPixels[i], fullyBlurred[i], _intensity);
        }

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, width, height, resultPixels, 0, width);
        return result;
    }

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

    /**
     * פונקציית Screen Blend מונעת שריפת צבעים (לוקחת בחשבון את הצבע המקורי).
     * הנוסחה: 1 - (1 - a) * (1 - b)
     */
    private int screenBlend(int originalColor, int bloomColor, float intensity) {
        int oR = (originalColor >> 16) & 0xFF;
        int oG = (originalColor >> 8) & 0xFF;
        int oB = originalColor & 0xFF;

        int bR = (bloomColor >> 16) & 0xFF;
        int bG = (bloomColor >> 8) & 0xFF;
        int bB = bloomColor & 0xFF;

        // החלת העוצמה על ההילה
        bR = (int)(bR * intensity);
        bG = (int)(bG * intensity);
        bB = (int)(bB * intensity);

        // Screen Blend Formula
        int r = 255 - (((255 - oR) * (255 - bR)) / 255);
        int g = 255 - (((255 - oG) * (255 - bG)) / 255);
        int b = 255 - (((255 - oB) * (255 - bB)) / 255);

        return new Color(r, g, b).getRGB();
    }

    private float getLuma(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;
    }
}