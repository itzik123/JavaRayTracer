package renderer.sampling;

import static primitives.Util.isZero;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

import java.util.function.Function;

/**
 * Adaptive super-sampling over a rectangular area, organised as a quadtree.
 * <p>
 * The four corners and the center are sampled first. If all five colors agree within a tolerance
 * the area is considered uniform and their average is returned; otherwise the area is split into
 * four quadrants that are refined recursively, reusing the colors already computed on shared
 * corners. Rays are therefore concentrated on edges and fine details, while flat regions cost
 * only a handful of samples.
 * <p>
 * The sampler knows nothing about cameras or materials - it only asks a caller-supplied function
 * for the color seen through a point - so the same code anti-aliases pixels and smooths glossy
 * reflections.
 *
 * @author itzik123
 */
public final class AdaptiveSuperSampler {

    /** Utility class - not instantiable. */
    private AdaptiveSuperSampler() {}

    /**
     * Samples an area adaptively.
     *
     * @param center       the center of the area
     * @param width        the width of the area
     * @param height       the height of the area
     * @param vX           the unit vector along the area width
     * @param vY           the unit vector along the area height
     * @param maxDepth     the maximum subdivision depth
     * @param tolerance    the per-channel color difference under which samples are considered equal
     * @param colorSampler computes the color seen through a point of the area
     * @return the average color of the area
     */
    public static Color sample(Point center, double width, double height, Vector vX, Vector vY,
                               int maxDepth, double tolerance, Function<Point, Color> colorSampler) {
        Grid grid = new Grid(vX, vY, maxDepth, tolerance, colorSampler);
        double halfW = width / 2;
        double halfH = height / 2;
        return grid.refine(center, width, height, 1,
                grid.sampleAt(center, -halfW, halfH),
                grid.sampleAt(center, halfW, halfH),
                grid.sampleAt(center, -halfW, -halfH),
                grid.sampleAt(center, halfW, -halfH));
    }

    /**
     * The fixed parameters of one sampling run.
     *
     * @param vX           the unit vector along the area width
     * @param vY           the unit vector along the area height
     * @param maxDepth     the maximum subdivision depth
     * @param tolerance    the color tolerance
     * @param colorSampler the color function
     */
    private record Grid(Vector vX, Vector vY, int maxDepth, double tolerance, Function<Point, Color> colorSampler) {

        /**
         * Samples the color at an offset from a point on the area.
         *
         * @param origin the reference point
         * @param dx     the offset along {@code vX}
         * @param dy     the offset along {@code vY}
         * @return the sampled color
         */
        Color sampleAt(Point origin, double dx, double dy) {
            return colorSampler.apply(move(origin, dx, dy));
        }

        /**
         * Moves a point on the area, skipping zero offsets (which cannot build a vector).
         *
         * @param p  the point
         * @param dx the offset along {@code vX}
         * @param dy the offset along {@code vY}
         * @return the moved point
         */
        Point move(Point p, double dx, double dy) {
            Point result = p;
            if (!isZero(dx)) result = result.add(vX.scale(dx));
            if (!isZero(dy)) result = result.add(vY.scale(dy));
            return result;
        }

        /**
         * Refines one quadtree cell whose corner colors are already known.
         *
         * @param center the center of the cell
         * @param width  the width of the cell
         * @param height the height of the cell
         * @param depth  the depth of the cell in the quadtree
         * @param cTL    the top-left corner color
         * @param cTR    the top-right corner color
         * @param cBL    the bottom-left corner color
         * @param cBR    the bottom-right corner color
         * @return the average color of the cell
         */
        Color refine(Point center, double width, double height, int depth,
                     Color cTL, Color cTR, Color cBL, Color cBR) {
            Color cCenter = colorSampler.apply(center);
            if (depth >= maxDepth
                    || cCenter.isSimilar(cTL, tolerance) && cCenter.isSimilar(cTR, tolerance)
                    && cCenter.isSimilar(cBL, tolerance) && cCenter.isSimilar(cBR, tolerance))
                return cCenter.add(cTL, cTR, cBL, cBR).reduce(5);

            double halfW = width / 2;
            double halfH = height / 2;
            double quarterW = width / 4;
            double quarterH = height / 4;

            // Edge midpoints become corners of the four sub-cells
            Color cTop = sampleAt(center, 0, halfH);
            Color cBottom = sampleAt(center, 0, -halfH);
            Color cLeft = sampleAt(center, -halfW, 0);
            Color cRight = sampleAt(center, halfW, 0);

            int next = depth + 1;
            return refine(move(center, -quarterW, quarterH), halfW, halfH, next, cTL, cTop, cLeft, cCenter)
                    .add(refine(move(center, quarterW, quarterH), halfW, halfH, next, cTop, cTR, cCenter, cRight),
                            refine(move(center, -quarterW, -quarterH), halfW, halfH, next, cLeft, cCenter, cBL, cBottom),
                            refine(move(center, quarterW, -quarterH), halfW, halfH, next, cCenter, cRight, cBottom, cBR))
                    .reduce(4);
        }
    }
}
