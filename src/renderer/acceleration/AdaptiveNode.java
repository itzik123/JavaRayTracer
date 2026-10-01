package renderer.acceleration;

import geometries.api.Intersectable;
import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import renderer.acceleration.AccelerationStructure.IntersectionVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A cell of the {@link AdaptiveGrid} octree: either a leaf holding geometries, or an inner node
 * with eight octant children.
 *
 * @author itzik123
 */
final class AdaptiveNode {

    /** The number of octants of a split cell. */
    private static final int OCTANTS = 8;

    /** The bounds of the cell. */
    private final AABB _box;
    /** The geometries overlapping a leaf cell; {@code null} once the cell is split. */
    private List<Intersectable> _geometries;
    /** The octants of a split cell; {@code null} for a leaf. */
    private AdaptiveNode[] _children;

    /**
     * Creates a leaf cell.
     *
     * @param box        the bounds of the cell
     * @param geometries the geometries overlapping the cell
     */
    AdaptiveNode(AABB box, List<Intersectable> geometries) {
        _box = box;
        _geometries = new ArrayList<>(geometries);
    }

    /**
     * Returns the bounds of the cell.
     *
     * @return the bounds of the cell
     */
    AABB box() {
        return _box;
    }

    /**
     * Recursively splits the cell into octants while it holds too many geometries. A geometry is
     * copied into every octant its bounding box overlaps. A split that would not reduce the load
     * of any octant (every geometry overlaps all eight) is undone, since it only adds depth.
     *
     * @param depth         the depth of this cell
     * @param maxGeometries the number of geometries a cell may hold before it is split
     * @param maxDepth      the maximum depth of the tree
     */
    void split(int depth, int maxGeometries, int maxDepth) {
        if (_geometries.size() <= maxGeometries || depth >= maxDepth) return;

        Point min = _box.min();
        Point max = _box.max();
        Point mid = new Point((min.getX() + max.getX()) / 2, (min.getY() + max.getY()) / 2, (min.getZ() + max.getZ()) / 2);

        AdaptiveNode[] children = new AdaptiveNode[OCTANTS];
        boolean progress = false;
        for (int i = 0; i < OCTANTS; i++) {
            // Bits 0, 1 and 2 of the octant index select the upper half along X, Y and Z
            AABB octant = new AABB(
                    new Point((i & 1) == 0 ? min.getX() : mid.getX(),
                            (i & 2) == 0 ? min.getY() : mid.getY(),
                            (i & 4) == 0 ? min.getZ() : mid.getZ()),
                    new Point((i & 1) == 0 ? mid.getX() : max.getX(),
                            (i & 2) == 0 ? mid.getY() : max.getY(),
                            (i & 4) == 0 ? mid.getZ() : max.getZ()));
            List<Intersectable> inside = new ArrayList<>();
            for (Intersectable geometry : _geometries)
                if (octant.isOverlapping(geometry.getBoundingBox())) inside.add(geometry);
            progress |= inside.size() < _geometries.size();
            children[i] = new AdaptiveNode(octant, inside);
        }
        if (!progress) return;

        _children = children;
        _geometries = null; // only leaves hold geometries
        for (AdaptiveNode child : _children) child.split(depth + 1, maxGeometries, maxDepth);
    }

    /**
     * Searches the cell for the closest intersection, visiting octants front-to-back.
     *
     * @param ray     the ray, known to hit this cell
     * @param closest the closest intersection tracker to update
     * @param tested  the geometries already tested against the ray
     */
    void findClosest(Ray ray, ClosestHit closest, Set<Intersectable> tested) {
        if (_children == null) {
            for (Intersectable geometry : _geometries)
                if (tested.add(geometry)) closest.offer(geometry.calcIntersections(ray));
            return;
        }

        // Sort the octants hit by the ray by their entry distance (insertion sort - at most 8)
        AdaptiveNode[] order = new AdaptiveNode[OCTANTS];
        double[] entry = new double[OCTANTS];
        int count = 0;
        for (AdaptiveNode child : _children) {
            double t = child._box.findIntersectionT(ray);
            if (t == AABB.NO_HIT) continue;
            int i = count++;
            for (; i > 0 && entry[i - 1] > t; i--) {
                entry[i] = entry[i - 1];
                order[i] = order[i - 1];
            }
            entry[i] = t;
            order[i] = child;
        }

        for (int i = 0; i < count; i++) {
            // Everything in this octant and the ones after it is farther than the best hit
            if (closest.distance() < entry[i]) return;
            order[i].findClosest(ray, closest, tested);
        }
    }

    /**
     * Passes the intersections inside the cell to a visitor, in no particular order.
     *
     * @param ray         the ray, known to hit this cell
     * @param maxDistance the maximum distance of an intersection from the ray origin
     * @param visitor     the intersection handler
     * @param tested      the geometries already tested against the ray
     * @return {@code false} if the visitor asked to stop
     */
    boolean traverse(Ray ray, double maxDistance, IntersectionVisitor visitor, Set<Intersectable> tested) {
        if (_children == null) {
            for (Intersectable geometry : _geometries)
                if (tested.add(geometry) && !SpatialStructure.visit(geometry, ray, maxDistance, visitor)) return false;
            return true;
        }
        for (AdaptiveNode child : _children) {
            double t = child._box.findIntersectionT(ray);
            if (t != AABB.NO_HIT && t <= maxDistance && !child.traverse(ray, maxDistance, visitor, tested))
                return false;
        }
        return true;
    }
}
