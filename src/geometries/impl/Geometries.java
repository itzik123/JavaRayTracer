package geometries.impl;

import geometries.api.Intersectable;
import primitives.AABB;
import primitives.Ray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A collection of intersectables that is itself intersectable (Composite pattern), so a whole
 * model can be treated as a single object.
 *
 * @author ityy35-bit
 */
public class Geometries extends Intersectable {

    /** The members of the collection. */
    private final List<Intersectable> _geometries = new ArrayList<>();

    /**
     * Creates a collection.
     *
     * @param geometries the initial members
     */
    public Geometries(Intersectable... geometries) {
        Collections.addAll(_geometries, geometries);
    }

    /**
     * Adds members to the collection.
     *
     * @param geometries the members to add
     * @return this collection
     */
    public Geometries add(Intersectable... geometries) {
        Collections.addAll(_geometries, geometries);
        return this;
    }

    /**
     * Flattens nested collections into a single list of leaf intersectables.
     *
     * @return the leaves of the composite, in insertion order
     */
    public List<Intersectable> flatten() {
        List<Intersectable> leaves = new ArrayList<>();
        for (Intersectable item : _geometries) {
            if (item instanceof Geometries composite) leaves.addAll(composite.flatten());
            else leaves.add(item);
        }
        return leaves;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Encloses the bounded members only; unbounded members are handled outside the spatial
     * partitioning.
     *
     * @return the box around all bounded members, or {@code null} if there are none
     */
    @Override
    protected AABB createBoundingBox() {
        AABB box = null;
        for (Intersectable geometry : _geometries) {
            AABB memberBox = geometry.getBoundingBox();
            if (memberBox != null) box = memberBox.union(box);
        }
        return box;
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> result = null;
        for (Intersectable geometry : _geometries) {
            List<Intersection> intersections = geometry.calcIntersections(ray, maxDistance);
            if (intersections != null) {
                if (result == null) result = new ArrayList<>();
                result.addAll(intersections);
            }
        }
        return result;
    }
}
