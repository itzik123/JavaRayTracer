package renderer.acceleration;

import geometries.api.Intersectable;
import geometries.api.Intersectable.Intersection;
import primitives.AABB;
import primitives.Ray;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class of the space-partitioning structures.
 * <p>
 * Splits the scene into bounded geometries, which are partitioned by the concrete structure, and
 * unbounded ones (planes, tubes), which cannot be placed in a finite cell and are tested against
 * every ray. Also computes the scene box that encloses all the bounded geometries.
 *
 * @author itzik123
 */
public abstract class SpatialStructure implements AccelerationStructure {

    /** Margin added around the scene box so geometry on its faces is safely inside. */
    private static final double SCENE_BOX_MARGIN = 0.1;

    /** The geometries without a bounding box, tested against every ray. */
    protected final List<Intersectable> _infiniteGeometries = new ArrayList<>();
    /** The geometries with a bounding box, to be partitioned by the concrete structure. */
    protected final List<Intersectable> _finiteGeometries = new ArrayList<>();
    /** The box enclosing all finite geometries, or {@code null} if there are none. */
    protected final AABB _sceneBox;

    /**
     * Classifies the geometries and computes the scene box.
     *
     * @param geometries the flat list of the scene geometries
     */
    protected SpatialStructure(List<Intersectable> geometries) {
        AABB sceneBox = null;
        for (Intersectable geometry : geometries) {
            AABB box = geometry.getBoundingBox();
            if (box == null) {
                _infiniteGeometries.add(geometry);
            } else {
                _finiteGeometries.add(geometry);
                sceneBox = box.union(sceneBox);
            }
        }
        _sceneBox = sceneBox == null ? null : sceneBox.expand(SCENE_BOX_MARGIN);
    }

    /**
     * Returns the box enclosing all finite geometries, or {@code null} if there are none.
     *
     * @return the box enclosing all finite geometries, or {@code null} if there are none
     */
    public AABB getSceneBox() {
        return _sceneBox;
    }

    /**
     * Tests the unbounded geometries for the closest intersection.
     *
     * @param ray     the ray
     * @param closest the closest intersection tracker to update
     */
    protected void findClosestAmongInfinite(Ray ray, ClosestHit closest) {
        for (Intersectable geometry : _infiniteGeometries) closest.offer(geometry.calcIntersections(ray));
    }

    /**
     * Passes the intersections with the unbounded geometries to a visitor.
     *
     * @param ray         the ray
     * @param maxDistance the maximum distance of an intersection from the ray origin
     * @param visitor     the intersection handler
     * @return {@code false} if the visitor asked to stop
     */
    protected boolean visitInfinite(Ray ray, double maxDistance, IntersectionVisitor visitor) {
        for (Intersectable geometry : _infiniteGeometries)
            if (!visit(geometry, ray, maxDistance, visitor)) return false;
        return true;
    }

    /**
     * Passes the intersections of a ray with one geometry to a visitor.
     *
     * @param geometry    the geometry
     * @param ray         the ray
     * @param maxDistance the maximum distance of an intersection from the ray origin
     * @param visitor     the intersection handler
     * @return {@code false} if the visitor asked to stop
     */
    static boolean visit(Intersectable geometry, Ray ray, double maxDistance, IntersectionVisitor visitor) {
        List<Intersection> intersections = geometry.calcIntersections(ray, maxDistance);
        if (intersections != null)
            for (Intersection intersection : intersections)
                if (!visitor.visit(intersection)) return false;
        return true;
    }
}
