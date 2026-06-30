package renderer;

import geometries.api.Intersectable;
import geometries.api.Intersectable.Intersection;
import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A ray tracer that utilizes a Regular Grid (Voxel Grid) to accelerate intersection calculations
 * using the 3DDDA algorithm.
 */
public class RegularGridRayTracer extends SimpleRayTracer {

    private final RegularGrid _grid;

    public RegularGridRayTracer(Scene scene, int resolution) {
        super(scene);
        _grid = new RegularGrid(scene.geometries.flatten(), resolution);
    }

    /**
     * Core 3DDDA traversal logic. Used by all ray types (primary, reflection, shadow).
     */
    private List<Intersection> traceGrid(Ray ray, double maxDistance) {
        List<Intersection> allIntersections = new ArrayList<>();

        // 1. חישוב חיתוכים מול גופים אינסופיים (שלא יושבים ברשת)
        for (Intersectable geo : _grid.getInfiniteGeometries()) {
            List<Intersection> ints = geo.calcIntersections(ray, maxDistance);
            if (ints != null) allIntersections.addAll(ints);
        }

        AABB sceneBox = _grid.getSceneBox();
        if (sceneBox == null) return allIntersections.isEmpty() ? null : allIntersections;

        // 2. מציאת נקודת הכניסה של הקרן לתוך הרשת
        double tEntry = sceneBox.findIntersectionT(ray);
        if (tEntry == Double.NEGATIVE_INFINITY || tEntry > maxDistance) {
            return allIntersections.isEmpty() ? null : allIntersections;
        }

        Point startPoint = tEntry > 0 ? ray.getPoint(tEntry) : ray.origin();

        // 3. חישוב אינדקסי הווקסל ההתחלתי
        int res = _grid.getResolution();
        int X = (int) ((startPoint.getX() - sceneBox.min.getX()) / _grid.getVoxelSizeX());
        int Y = (int) ((startPoint.getY() - sceneBox.min.getY()) / _grid.getVoxelSizeY());
        int Z = (int) ((startPoint.getZ() - sceneBox.min.getZ()) / _grid.getVoxelSizeZ());

        // הגנות קצה לפספוסי Floating Point
        if (X < 0) X = 0; if (X >= res) X = res - 1;
        if (Y < 0) Y = 0; if (Y >= res) Y = res - 1;
        if (Z < 0) Z = 0; if (Z >= res) Z = res - 1;

        // 4. הגדרת משתני אלגוריתם 3DDDA
        Vector dir = ray.direction();
        int stepX = dir.getX() > 0 ? 1 : (dir.getX() < 0 ? -1 : 0);
        int stepY = dir.getY() > 0 ? 1 : (dir.getY() < 0 ? -1 : 0);
        int stepZ = dir.getZ() > 0 ? 1 : (dir.getZ() < 0 ? -1 : 0);

        // מרחק t שעוברים כאשר חוצים ווקסל שלם לאורך כל ציר
        double tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(_grid.getVoxelSizeX() / dir.getX());
        double tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(_grid.getVoxelSizeY() / dir.getY());
        double tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(_grid.getVoxelSizeZ() / dir.getZ());

        // מרחק t מהנקודה הנוכחית ועד הגבול הראשון של הווקסל
        double voxelMaxX = sceneBox.min.getX() + (X + (stepX > 0 ? 1 : 0)) * _grid.getVoxelSizeX();
        double voxelMaxY = sceneBox.min.getY() + (Y + (stepY > 0 ? 1 : 0)) * _grid.getVoxelSizeY();
        double voxelMaxZ = sceneBox.min.getZ() + (Z + (stepZ > 0 ? 1 : 0)) * _grid.getVoxelSizeZ();

        double tMaxX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs((voxelMaxX - startPoint.getX()) / dir.getX());
        double tMaxY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs((voxelMaxY - startPoint.getY()) / dir.getY());
        double tMaxZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs((voxelMaxZ - startPoint.getZ()) / dir.getZ());

        // מנגנון Mailbox למניעת חישוב כפול לאותו גוף
        Set<Intersectable> checkedGeometries = new HashSet<>();

        // 5. לולאת המעבר ברשת
        while (X >= 0 && X < res && Y >= 0 && Y < res && Z >= 0 && Z < res) {
            List<Intersectable> voxelGeometries = _grid.getVoxel(X, Y, Z);

            if (voxelGeometries != null) {
                for (Intersectable geo : voxelGeometries) {
                    if (checkedGeometries.add(geo)) { // יחזיר true רק אם הגוף טרם נבדק
                        List<Intersection> ints = geo.calcIntersections(ray, maxDistance);
                        if (ints != null) allIntersections.addAll(ints);
                    }
                }
            }

            double tVoxelExit = tEntry + Math.min(tMaxX, Math.min(tMaxY, tMaxZ));

            // עצירה מוקדמת: אם מצאנו חיתוך בתוך הווקסל הנוכחי - אין טעם להמשיך לבדוק ווקסלים רחוקים יותר!
            if (maxDistance == Double.POSITIVE_INFINITY) {
                Intersection closest = ray.findClosestIntersection(allIntersections);
                if (closest != null) {
                    double tClosest = ray.origin().distance(closest.point);
                    if (tClosest <= tVoxelExit + 0.001) {
                        break;
                    }
                }
            }

            // יציאה אם חצינו את המרחק המקסימלי (רלוונטי לקרני צל)
            if (tVoxelExit > maxDistance) {
                break;
            }

            // קידום הקרן לווקסל הבא (תמיד מתקדמים לציר שבו המרחק לגבול הוא הקצר ביותר)
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                X += stepX;
                tMaxX += tDeltaX;
            } else if (tMaxY < tMaxZ) {
                Y += stepY;
                tMaxY += tDeltaY;
            } else {
                Z += stepZ;
                tMaxZ += tDeltaZ;
            }
        }

        return allIntersections.isEmpty() ? null : allIntersections;
    }

    // --- דריסת הפונקציות של SimpleRayTracer כדי להשתמש ברשת ---

    @Override
    protected Intersection findClosestIntersection(Ray ray) {
        var intersections = traceGrid(ray, Double.POSITIVE_INFINITY);
        return intersections == null ? null : ray.findClosestIntersection(intersections);
    }

    @Override
    protected boolean unshaded(Intersection intersection) {
        Vector lightDirection = intersection.light.getL(intersection.point).scale(-1);
        Ray lightRay = new Ray(intersection.point, lightDirection, intersection.normal);
        double maxDistance = intersection.light.getDistance(intersection.point);

        var intersections = traceGrid(lightRay, maxDistance);
        if (intersections == null) return true;

        for (Intersection geo : intersections) {
            if (geo.material.kT.isLowerThan(0.001)) return false;
        }
        return true;
    }

    @Override
    protected primitives.Double3 transparency(Intersection intersection) {
        Vector lightDirection = intersection.light.getL(intersection.point).scale(-1);
        Ray lightRay = new Ray(intersection.point, lightDirection, intersection.normal);
        double maxDistance = intersection.light.getDistance(intersection.point);

        var intersections = traceGrid(lightRay, maxDistance);
        if (intersections == null) return primitives.Double3.ONE;

        primitives.Double3 ktr = primitives.Double3.ONE;
        for (Intersection geo : intersections) {
            ktr = ktr.product(geo.material.kT);
            if (ktr.isLowerThan(0.001)) return primitives.Double3.ZERO;
        }
        return ktr;
    }
}