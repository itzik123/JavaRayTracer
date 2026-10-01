package renderer.acceleration;

import geometries.api.Intersectable.Intersection;
import primitives.Ray;

/**
 * Strategy for answering ray queries against the scene geometry.
 * <p>
 * The ray tracer only ever asks two questions - "what does this ray hit first?" and "what does
 * this ray pass through on its way to a light?" - so the way geometries are organized in space
 * (a plain list, a uniform grid, an octree) can be swapped freely without touching any shading
 * code.
 *
 * @author itzik123
 */
public interface AccelerationStructure {

    /**
     * Receives the intersections found along a ray, one at a time.
     */
    @FunctionalInterface
    interface IntersectionVisitor {
        /**
         * Handles one intersection.
         *
         * @param intersection the intersection found
         * @return {@code true} to continue the traversal, {@code false} to stop it
         */
        boolean visit(Intersection intersection);
    }

    /**
     * Finds the intersection closest to the ray origin.
     *
     * @param ray the ray
     * @return the closest intersection, or {@code null} if the ray hits nothing
     */
    Intersection findClosestIntersection(Ray ray);

    /**
     * Passes every intersection within a distance from the ray origin to a visitor, in no
     * particular order, until the visitor asks to stop. Stopping early lets shadow rays quit as
     * soon as they meet an opaque occluder.
     *
     * @param ray         the ray
     * @param maxDistance the maximum distance of an intersection from the ray origin
     * @param visitor     the intersection handler
     */
    void traverse(Ray ray, double maxDistance, IntersectionVisitor visitor);
}
