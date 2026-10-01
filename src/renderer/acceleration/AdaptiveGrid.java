package renderer.acceleration;

import geometries.api.Intersectable;
import geometries.api.Intersectable.Intersection;
import primitives.AABB;
import primitives.Ray;

import java.util.HashSet;
import java.util.List;

/**
 * An adaptive grid: an octree that subdivides space only where geometry is dense.
 * <p>
 * Unlike a uniform grid, which wastes cells on empty space and overloads cells in detailed
 * areas, the octree splits a cell into eight octants only while it holds more than a given
 * number of geometries. Closest-hit queries visit the octants front-to-back and stop as soon as
 * the best hit is nearer than the next octant; shadow queries stop as soon as the light is fully
 * blocked.
 *
 * @author itzik123
 */
public class AdaptiveGrid extends SpatialStructure {

    /** The root cell, covering the scene box; {@code null} if there is no finite geometry. */
    private final AdaptiveNode _root;

    /**
     * Builds the octree.
     *
     * @param geometries    the flat list of the scene geometries
     * @param maxGeometries the number of geometries a cell may hold before it is split
     * @param maxDepth      the maximum depth of the tree
     * @throws IllegalArgumentException if a parameter is not positive
     */
    public AdaptiveGrid(List<Intersectable> geometries, int maxGeometries, int maxDepth) {
        super(geometries);
        if (maxGeometries < 1 || maxDepth < 1)
            throw new IllegalArgumentException("Octree capacity and depth must be positive");

        if (_sceneBox == null) {
            _root = null;
        } else {
            _root = new AdaptiveNode(_sceneBox, _finiteGeometries);
            _root.split(0, maxGeometries, maxDepth);
        }
    }

    @Override
    public Intersection findClosestIntersection(Ray ray) {
        ClosestHit closest = new ClosestHit(ray.origin());
        findClosestAmongInfinite(ray, closest);
        if (_root != null && _root.box().findIntersectionT(ray) != AABB.NO_HIT)
            _root.findClosest(ray, closest, new HashSet<>());
        return closest.intersection();
    }

    @Override
    public void traverse(Ray ray, double maxDistance, IntersectionVisitor visitor) {
        if (!visitInfinite(ray, maxDistance, visitor) || _root == null) return;
        double entryT = _root.box().findIntersectionT(ray);
        if (entryT != AABB.NO_HIT && entryT <= maxDistance)
            _root.traverse(ray, maxDistance, visitor, new HashSet<>());
    }
}
