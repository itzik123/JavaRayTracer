package renderer.postprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Advanced FXAA (Fast Approximate Anti-Aliasing) Filter.
 * Features Edge Tracking, Sub-pixel smoothing, and Stochastic (Randomized) Blending.
 */
public class FXAAFilter implements PostProcessFilter {

    private float _edgeThreshold = 0.125f;
    private float _darkThreshold = 0.0312f;
    private int _maxSearchSteps = 24; // הוגדל משמעותית כדי לחפש מדרגות ארוכות בספירות

    /** * כוח הרעש האקראי (Jitter) לשילוב הצבעים.
     * 0.0 = ללא רעש (FXAA רגיל), 0.15 = רעש חזק המדמה גרעיניות מצלמה.
     */
    private float _jitterStrength = 0.08f;

    private static final Random RANDOM = new Random();

    private float _lumaR = 0.299f;
    private float _lumaG = 0.587f;
    private float _lumaB = 0.114f;

    // --- Setters ---

    public FXAAFilter setEdgeThreshold(float edgeThreshold) {
        this._edgeThreshold = edgeThreshold;
        return this;
    }

    public FXAAFilter setMaxSearchSteps(int maxSearchSteps) {
        this._maxSearchSteps = maxSearchSteps;
        return this;
    }

    /**
     * מגדיר את עוצמת האקראיות בשילוב הצבעים (הרעיון שלך!).
     */
    public FXAAFilter setJitterStrength(float jitterStrength) {
        this._jitterStrength = jitterStrength;
        return this;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        int[] resultPixels = new int[pixels.length];

        float[] lumaArray = new float[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            lumaArray[i] = getLuma(pixels[i]);
        }

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int index = y * width + x;

                float lumaM  = lumaArray[index];
                float lumaN  = lumaArray[index - width];
                float lumaS  = lumaArray[index + width];
                float lumaE  = lumaArray[index + 1];
                float lumaW  = lumaArray[index - 1];

                float minLuma = Math.min(lumaM, Math.min(Math.min(lumaN, lumaS), Math.min(lumaE, lumaW)));
                float maxLuma = Math.max(lumaM, Math.max(Math.max(lumaN, lumaS), Math.max(lumaE, lumaW)));
                float contrast = maxLuma - minLuma;

                if (contrast < Math.max(_darkThreshold, maxLuma * _edgeThreshold)) {
                    resultPixels[index] = pixels[index];
                    continue;
                }

                float lumaNW = lumaArray[index - width - 1];
                float lumaNE = lumaArray[index - width + 1];
                float lumaSW = lumaArray[index + width - 1];
                float lumaSE = lumaArray[index + width + 1];

                float edgeHorz = Math.abs((0.25f * lumaNW) - (0.5f * lumaN) + (0.25f * lumaNE)) +
                        Math.abs((0.50f * lumaW ) - (1.0f * lumaM) + (0.50f * lumaE )) +
                        Math.abs((0.25f * lumaSW) - (0.5f * lumaS) + (0.25f * lumaSE));

                float edgeVert = Math.abs((0.25f * lumaNW) - (0.5f * lumaW) + (0.25f * lumaSW)) +
                        Math.abs((0.50f * lumaN ) - (1.0f * lumaM) + (0.50f * lumaS )) +
                        Math.abs((0.25f * lumaNE) - (0.5f * lumaE) + (0.25f * lumaSE));

                boolean isHorizontal = edgeHorz >= edgeVert;

                float gradientN = Math.abs(lumaN - lumaM);
                float gradientS = Math.abs(lumaS - lumaM);
                float gradientW = Math.abs(lumaW - lumaM);
                float gradientE = Math.abs(lumaE - lumaM);

                boolean isPositiveDir = isHorizontal ? (gradientS >= gradientN) : (gradientE >= gradientW);
                int stepX = isHorizontal ? 0 : (isPositiveDir ? 1 : -1);
                int stepY = isHorizontal ? (isPositiveDir ? 1 : -1) : 0;

                int neighborX = x + stepX;
                int neighborY = y + stepY;
                float lumaNeighbor = getLumaSafe(neighborX, neighborY, width, height, lumaArray);

                float edgeLuma = (lumaM + lumaNeighbor) * 0.5f;
                float gradientThreshold = Math.abs(lumaM - lumaNeighbor) * 0.25f;

                int searchStepX = isHorizontal ? 1 : 0;
                int searchStepY = isHorizontal ? 0 : 1;

                int nx = x - searchStepX;
                int ny = y - searchStepY;
                int distNeg = 0;
                float lumaEndNeg = 0;
                while (distNeg < _maxSearchSteps) {
                    float lL = getLumaSafe(nx, ny, width, height, lumaArray);
                    float lN = getLumaSafe(nx + stepX, ny + stepY, width, height, lumaArray);
                    lumaEndNeg = (lL + lN) * 0.5f;
                    if (Math.abs(lumaEndNeg - edgeLuma) >= gradientThreshold) break;
                    nx -= searchStepX;
                    ny -= searchStepY;
                    distNeg++;
                }

                int px = x + searchStepX;
                int py = y + searchStepY;
                int distPos = 0;
                float lumaEndPos = 0;
                while (distPos < _maxSearchSteps) {
                    float lL = getLumaSafe(px, py, width, height, lumaArray);
                    float lN = getLumaSafe(px + stepX, py + stepY, width, height, lumaArray);
                    lumaEndPos = (lL + lN) * 0.5f;
                    if (Math.abs(lumaEndPos - edgeLuma) >= gradientThreshold) break;
                    px += searchStepX;
                    py += searchStepY;
                    distPos++;
                }

                int shortestDist = Math.min(distNeg, distPos);
                boolean isDirectionNeg = distNeg < distPos;
                float lumaEnd = isDirectionNeg ? lumaEndNeg : lumaEndPos;

                boolean isLumaM_larger = lumaM >= edgeLuma;
                boolean isLumaEnd_larger = lumaEnd >= edgeLuma;
                boolean goodSpan = (isLumaM_larger != isLumaEnd_larger);

                float spanBlendFactor = 0.0f;
                if (goodSpan) {
                    spanBlendFactor = (0.5f - (float)shortestDist / (distNeg + distPos + 1));
                }

                // הגברנו את עוצמת ההחלקה התת-פיקסלית כדי להתמודד עם הספירה
                float lumaLocalAverage = (lumaN + lumaS + lumaE + lumaW) * 0.25f;
                float subPixelOffset = Math.abs(lumaLocalAverage - lumaM) / contrast;
                subPixelOffset = Math.max(0.0f, Math.min(1.0f, subPixelOffset));
                float subPixelBlendFactor = subPixelOffset * subPixelOffset * 1.0f; // אגרסיבי יותר

                float finalBlendFactor = Math.max(spanBlendFactor, subPixelBlendFactor);
                finalBlendFactor = Math.max(0.0f, Math.min(0.6f, finalBlendFactor)); // מאפשרים טשטוש עד 60%

                // --- 7. שילוב סטוכסטי (Stochastic Blending - הרעיון שלך!) ---
                Color cCenter = new Color(pixels[index]);
                Color cNeighbor = new Color(getSafePixel(neighborX, neighborY, width, height, pixels));

                // יצירת רעש אקראי שונה לכל ערוץ צבע
                float jitterR = (RANDOM.nextFloat() - 0.5f) * _jitterStrength;
                float jitterG = (RANDOM.nextFloat() - 0.5f) * _jitterStrength;
                float jitterB = (RANDOM.nextFloat() - 0.5f) * _jitterStrength;

                // חיבור הרעש למקדם השילוב, תוך שמירה שלא נחרוג מהגבולות
                float blendR = Math.max(0.0f, Math.min(0.8f, finalBlendFactor + jitterR));
                float blendG = Math.max(0.0f, Math.min(0.8f, finalBlendFactor + jitterG));
                float blendB = Math.max(0.0f, Math.min(0.8f, finalBlendFactor + jitterB));

                int r = (int) (cCenter.getRed() * (1 - blendR) + cNeighbor.getRed() * blendR);
                int g = (int) (cCenter.getGreen() * (1 - blendG) + cNeighbor.getGreen() * blendG);
                int b = (int) (cCenter.getBlue() * (1 - blendB) + cNeighbor.getBlue() * blendB);

                resultPixels[index] = new Color(r, g, b).getRGB();
            }
        }

        for (int x = 0; x < width; x++) {
            resultPixels[x] = pixels[x];
            resultPixels[(height - 1) * width + x] = pixels[(height - 1) * width + x];
        }
        for (int y = 0; y < height; y++) {
            resultPixels[y * width] = pixels[y * width];
            resultPixels[y * width + width - 1] = pixels[y * width + width - 1];
        }

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, width, height, resultPixels, 0, width);
        return result;
    }

    private float getLumaSafe(int x, int y, int width, int height, float[] lumaArray) {
        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, height - 1));
        return lumaArray[y * width + x];
    }

    private int getSafePixel(int x, int y, int width, int height, int[] pixels) {
        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, height - 1));
        return pixels[y * width + x];
    }

    private float getLuma(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r * _lumaR + g * _lumaG + b * _lumaB) / 255.0f;
    }
}