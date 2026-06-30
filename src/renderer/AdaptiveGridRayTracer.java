package renderer;

import geometries.api.Intersectable;
import geometries.api.Intersectable.Intersection;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A ray tracer that utilizes an Adaptive Grid (Octree) to accelerate intersection calculations
 * using recursive Front-to-Back traversal.
 */
public class AdaptiveGridRayTracer extends SimpleRayTracer {

    private final AdaptiveGrid _grid;

    /** Record to hold a child node and the ray's entry distance to it, for sorting purposes. */
    private record ChildHit(AdaptiveNode node, double t) implements Comparable<ChildHit> {
        @Override
        public int compareTo(ChildHit o) {
            return Double.compare(this.t, o.t);
        }
    }

    public AdaptiveGridRayTracer(Scene scene, int maxGeometries, int maxDepth) {
        super(scene);
        _grid = new AdaptiveGrid(scene.geometries.flatten(), maxGeometries, maxDepth);
    }

    /**
     * Core recursive Octree traversal logic.
     */
    private List<Intersection> traceTree(Ray ray, double maxDistance) {
        List<Intersection> allIntersections = new ArrayList<>();
        Set<Intersectable> checkedGeometries = new HashSet<>(); // Mailbox mechanism

        // 1. טיפול בגופים אינסופיים (שלא יושבים בעץ)
        for (Intersectable geo : _grid.getInfiniteGeometries()) {
            List<Intersection> ints = geo.calcIntersections(ray, maxDistance);
            if (ints != null) allIntersections.addAll(ints);
        }

        // 2. כניסה לעץ הרקורסיבי
        AdaptiveNode root = _grid.getRoot();
        if (root != null) {
            double tEntry = root.box.findIntersectionT(ray);
            // אם הקרן פוגעת בשורש הסצנה, נתחיל לסרוק
            if (tEntry != Double.NEGATIVE_INFINITY && tEntry <= maxDistance) {
                traverseNode(root, ray, maxDistance, allIntersections, checkedGeometries);
            }
        }

        return allIntersections.isEmpty() ? null : allIntersections;
    }

    /**
     * Recursive function to traverse the Octree nodes Front-to-Back.
     */
    private void traverseNode(AdaptiveNode node, Ray ray, double maxDistance,
                              List<Intersection> allIntersections, Set<Intersectable> checkedGeometries) {

        // מקרה בסיס: הגענו ל"עלה" (Leaf Node)
        if (node.children == null) {
            if (node.geometries != null) {
                for (Intersectable geo : node.geometries) {
                    // מוודאים שלא בדקנו את הגוף הזה בעבר בעזרת ה-Mailbox
                    if (checkedGeometries.add(geo)) {
                        List<Intersection> ints = geo.calcIntersections(ray, maxDistance);
                        if (ints != null) allIntersections.addAll(ints);
                    }
                }
            }
            return;
        }

        // מקרה רקורסיבי: זהו צומת מפוצל (Internal Node)
        List<ChildHit> hits = new ArrayList<>(8);

        // מציאת כל הילדים שהקרן חותכת ואת מרחק הכניסה אליהם (t)
        for (AdaptiveNode child : node.children) {
            double t = child.box.findIntersectionT(ray);
            if (t != Double.NEGATIVE_INFINITY && t <= maxDistance) {
                hits.add(new ChildHit(child, t));
            }
        }

        // מיון הילדים מהקרוב לרחוק (Front-to-Back)
        Collections.sort(hits);

        // כניסה רקורסיבית לילדים לפי סדר הפגיעה
        for (ChildHit hit : hits) {

            // --- Early Exit Optimization ---
            // אם אנחנו מחפשים את הפגיעה הקרובה ביותר למצלמה (maxDistance = Infinity),
            // וכבר מצאנו פגיעה שהמרחק אליה *קטן* ממרחק הכניסה (hit.t) לקופסת הילד הבא,
            // אנחנו יודעים בוודאות שאין טעם לחפש בילד הבא, כי כל מה שבו יהיה רחוק יותר!
            if (maxDistance == Double.POSITIVE_INFINITY && !allIntersections.isEmpty()) {
                Intersection closest = ray.findClosestIntersection(allIntersections);
                if (closest != null) {
                    double tClosest = ray.origin().distance(closest.point);
                    if (tClosest < hit.t) {
                        break; // יציאה מוקדמת חוסכת המון זמן חישוב!
                    }
                }
            }

            traverseNode(hit.node, ray, maxDistance, allIntersections, checkedGeometries);
        }
    }

    // --- דריסת הפונקציות של SimpleRayTracer כדי להשתמש בעץ המסתגל ---

    @Override
    protected Intersection findClosestIntersection(Ray ray) {
        var intersections = traceTree(ray, Double.POSITIVE_INFINITY);
        return intersections == null ? null : ray.findClosestIntersection(intersections);
    }

    @Override
    protected boolean unshaded(Intersection intersection) {
        Vector lightDirection = intersection.light.getL(intersection.point).scale(-1);
        Ray lightRay = new Ray(intersection.point, lightDirection, intersection.normal);
        double maxDistance = intersection.light.getDistance(intersection.point);

        var intersections = traceTree(lightRay, maxDistance);
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

        var intersections = traceTree(lightRay, maxDistance);
        if (intersections == null) return primitives.Double3.ONE;

        primitives.Double3 ktr = primitives.Double3.ONE;
        for (Intersection geo : intersections) {
            ktr = ktr.product(geo.material.kT);
            if (ktr.isLowerThan(0.001)) return primitives.Double3.ZERO;
        }
        return ktr;
    }
}