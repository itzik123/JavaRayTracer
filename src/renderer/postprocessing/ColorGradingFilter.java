package renderer.postprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * A post-processing filter for Color Grading, optimized for LDR images (0-255).
 * Features Exposure, Contrast, Gamma Correction, and Saturation adjustments.
 */
public class ColorGradingFilter implements PostProcessFilter {

    /** חשיפה (מכפיל ליניארי). 1.0 = ללא שינוי, 1.2 = בהיר יותר, 0.8 = חשוך יותר */
    private float _exposure = 1.0f;

    /** תיקון גמא. 1.0 = ללא שינוי. ערכים גדולים מ-1.0 מבהירים את גווני הביניים, קטנים מ-1.0 מחשיכים אותם */
    private float _gamma = 1.0f;

    /** ניגודיות. 1.0 = ללא שינוי */
    private float _contrast = 1.0f;

    /** רווית צבע. 1.0 = ללא שינוי */
    private float _saturation = 1.0f;

    // --- Setters ---

    public ColorGradingFilter setExposure(float exposure) { this._exposure = exposure; return this; }
    public ColorGradingFilter setGamma(float gamma) { this._gamma = gamma; return this; }
    public ColorGradingFilter setContrast(float contrast) { this._contrast = contrast; return this; }
    public ColorGradingFilter setSaturation(float saturation) { this._saturation = saturation; return this; }

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        int[] resultPixels = new int[pixels.length];

        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];

            // המרה לטווח 0.0 - 1.0
            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (rgb & 0xFF) / 255.0f;

            // 1. Exposure (חשיפה ליניארית)
            r *= _exposure;
            g *= _exposure;
            b *= _exposure;

            // 2. Contrast (סביב נקודת האמצע 0.5)
            r = (r - 0.5f) * _contrast + 0.5f;
            g = (g - 0.5f) * _contrast + 0.5f;
            b = (b - 0.5f) * _contrast + 0.5f;

            // חיתוך לפני גמא כדי למנוע ערכים שליליים (NaN)
            r = Math.max(0.0f, Math.min(1.0f, r));
            g = Math.max(0.0f, Math.min(1.0f, g));
            b = Math.max(0.0f, Math.min(1.0f, b));

            // 3. Gamma Correction (תיקון גמא לגווני הביניים)
            if (_gamma != 1.0f) {
                float invGamma = 1.0f / _gamma;
                r = (float) Math.pow(r, invGamma);
                g = (float) Math.pow(g, invGamma);
                b = (float) Math.pow(b, invGamma);
            }

            // 4. Saturation (רוויה)
            float luma = 0.299f * r + 0.587f * g + 0.114f * b;
            r = luma + _saturation * (r - luma);
            g = luma + _saturation * (g - luma);
            b = luma + _saturation * (b - luma);

            // 5. חיתוך סופי
            r = Math.max(0.0f, Math.min(1.0f, r));
            g = Math.max(0.0f, Math.min(1.0f, g));
            b = Math.max(0.0f, Math.min(1.0f, b));

            resultPixels[i] = new Color((int) Math.round(r * 255), (int) Math.round(g * 255), (int) Math.round(b * 255)).getRGB();
        }

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, width, height, resultPixels, 0, width);
        return result;
    }
}