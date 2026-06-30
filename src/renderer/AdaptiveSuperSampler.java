package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Vector;
import java.util.function.Function;

import static primitives.Util.isZero;

/**
 * Generic utility for Adaptive Super-Sampling (Quadtree).
 * Completely decoupled from specific engines to support DRY.
 */
public class AdaptiveSuperSampler {

    /**
     * Helper method to safely move a point on the sampling plane.
     */
    private static Point movePoint(Point p, double dx, double dy, Vector vX, Vector vY) {
        Point res = p;
        if (!isZero(dx)) res = res.add(vX.scale(dx));
        if (!isZero(dy)) res = res.add(vY.scale(dy));
        return res;
    }

    /**
     * Initiates the Adaptive Super-Sampling algorithm on a given 2D plane.
     * @param center The center point of the sampling area
     * @param width The width of the sampling area
     * @param height The height of the sampling area
     * @param vX The local X axis vector of the plane
     * @param vY The local Y axis vector of the plane
     * @param maxDepth Maximum recursion depth
     * @param tolerance Color difference tolerance for early exit
     * @param colorSampler The lambda function that calculates color for a given point
     * @return The averaged color of the sampled area
     */
    public static Color sample(Point center, double width, double height,
                               Vector vX, Vector vY,
                               int maxDepth, double tolerance,
                               Function<Point, Color> colorSampler) {

        // 1. חישוב 4 הפינות על המישור
        Point pTL = movePoint(center, -width / 2, height / 2, vX, vY);
        Point pTR = movePoint(center, width / 2, height / 2, vX, vY);
        Point pBL = movePoint(center, -width / 2, -height / 2, vX, vY);
        Point pBR = movePoint(center, width / 2, -height / 2, vX, vY);

        // 2. הפעלת הלמבדה כדי לקבל את צבעי הפינות
        Color cTL = colorSampler.apply(pTL);
        Color cTR = colorSampler.apply(pTR);
        Color cBL = colorSampler.apply(pBL);
        Color cBR = colorSampler.apply(pBR);

        // 3. כניסה לרקורסיה
        return calcAdaptive(center, width, height, 1, maxDepth, tolerance, vX, vY,
                cTL, cTR, cBL, cBR, colorSampler);
    }

    private static Color calcAdaptive(Point center, double width, double height, int depth,
                                      int maxDepth, double tolerance, Vector vX, Vector vY,
                                      Color cTL, Color cTR, Color cBL, Color cBR,
                                      Function<Point, Color> colorSampler) {

        Color cCenter = colorSampler.apply(center);

        // תנאי עצירה (Early Exit)
        if (depth == maxDepth ||
                (cCenter.isSimilar(cTL, tolerance) &&
                        cCenter.isSimilar(cTR, tolerance) &&
                        cCenter.isSimilar(cBL, tolerance) &&
                        cCenter.isSimilar(cBR, tolerance))) {
            return cCenter.add(cTL, cTR, cBL, cBR).reduce(5);
        }

        // חלוקה ל-4 רבעים
        double halfW = width / 2;
        double halfH = height / 2;
        double quarterW = width / 4;
        double quarterH = height / 4;

        Point pTop = movePoint(center, 0, halfH, vX, vY);
        Point pBottom = movePoint(center, 0, -halfH, vX, vY);
        Point pLeft = movePoint(center, -halfW, 0, vX, vY);
        Point pRight = movePoint(center, halfW, 0, vX, vY);

        Color cTop = colorSampler.apply(pTop);
        Color cBottom = colorSampler.apply(pBottom);
        Color cLeft = colorSampler.apply(pLeft);
        Color cRight = colorSampler.apply(pRight);

        Point centerTL = movePoint(center, -quarterW, quarterH, vX, vY);
        Point centerTR = movePoint(center, quarterW, quarterH, vX, vY);
        Point centerBL = movePoint(center, -quarterW, -quarterH, vX, vY);
        Point centerBR = movePoint(center, quarterW, -quarterH, vX, vY);

        // Caching - שימוש חוזר בצבעי הפינות הידועים
        Color colorTL = calcAdaptive(centerTL, halfW, halfH, depth + 1, maxDepth, tolerance, vX, vY, cTL, cTop, cLeft, cCenter, colorSampler);
        Color colorTR = calcAdaptive(centerTR, halfW, halfH, depth + 1, maxDepth, tolerance, vX, vY, cTop, cTR, cCenter, cRight, colorSampler);
        Color colorBL = calcAdaptive(centerBL, halfW, halfH, depth + 1, maxDepth, tolerance, vX, vY, cLeft, cCenter, cBL, cBottom, colorSampler);
        Color colorBR = calcAdaptive(centerBR, halfW, halfH, depth + 1, maxDepth, tolerance, vX, vY, cCenter, cRight, cBottom, cBR, colorSampler);

        return colorTL.add(colorTR, colorBL, colorBR).reduce(4);
    }
}