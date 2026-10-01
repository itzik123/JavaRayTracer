package renderer.acceleration;

import geometries.api.Intersectable;
import geometries.api.Intersectable.Intersection;
import geometries.impl.Geometries;
import primitives.Ray;

import java.util.List;

/**
 * The baseline strategy: every ray is tested against every geometry. Simple and exact, but the
 * cost grows linearly with the scene size.
 *
 * @author itzik123
 */
public class LinearScan implements AccelerationStructure {

    /** All the geometries of the scene. */
    private final List<Intersectable> _geometries;

    /**
     * Creates a linear scan over the scene geometry.
     *
     * @param geometries the scene geometry
     */
    public LinearScan(Geometries geometries) {
        _geometries = geometries.flatten();
    }

    @Override
    public Intersection findClosestIntersection(Ray ray) {
        ClosestHit closest = new ClosestHit(ray.origin());
        for (Intersectable geometry : _geometries) closest.offer(geometry.calcIntersections(ray));
        return closest.intersection();
    }

    @Override
    public void traverse(Ray ray, double maxDistance, IntersectionVisitor visitor) {
        for (Intersectable geometry : _geometries)
            if (!SpatialStructure.visit(geometry, ray, maxDistance, visitor)) return;
    }
}
