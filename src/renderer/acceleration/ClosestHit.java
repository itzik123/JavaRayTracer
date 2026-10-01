package renderer.acceleration;

import geometries.api.Intersectable.Intersection;
import primitives.Point;

import java.util.List;

/**
 * Tracks the closest intersection found so far during a traversal.
 *
 * @author itzik123
 */
final class ClosestHit {

    /** The ray origin that distances are measured from. */
    private final Point _origin;
    /** The closest intersection so far, or {@code null}. */
    private Intersection _intersection;
    /** The squared distance of the closest intersection so far. */
    private double _distanceSquared = Double.POSITIVE_INFINITY;

    /**
     * Creates an empty tracker for a ray.
     *
     * @param origin the ray origin
     */
    ClosestHit(Point origin) {
        _origin = origin;
    }

    /**
     * Keeps the closest of the candidates if it beats the current best.
     *
     * @param candidates the candidate intersections, or {@code null}
     */
    void offer(List<Intersection> candidates) {
        if (candidates == null) return;
        for (Intersection candidate : candidates) {
            double distanceSquared = candidate.point.distanceSquared(_origin);
            if (distanceSquared < _distanceSquared) {
                _distanceSquared = distanceSquared;
                _intersection = candidate;
            }
        }
    }

    /**
     * Returns the distance of the closest intersection so far (infinity if none).
     *
     * @return the distance of the closest intersection so far (infinity if none)
     */
    double distance() {
        return Math.sqrt(_distanceSquared);
    }

    /**
     * Returns the closest intersection so far, or {@code null}.
     *
     * @return the closest intersection so far, or {@code null}
     */
    Intersection intersection() {
        return _intersection;
    }
}
