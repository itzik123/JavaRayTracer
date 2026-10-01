package geometries.api;

import lighting.LightSource;
import primitives.AABB;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;
import java.util.Objects;

/**
 * Base class of everything a ray can hit - single geometries and composites alike.
 * <p>
 * Intersection queries follow the Non-Virtual Interface pattern: the public {@code final}
 * methods define the contract, and subclasses only implement {@link #calcIntersectionsHelper}.
 * Every intersectable also exposes an axis-aligned bounding box, which the acceleration
 * structures use to place it in space.
 *
 * @author itzik123
 */
public abstract class Intersectable {

    /** The cached bounding box; meaningful only once {@link #_boxComputed} is set. */
    private AABB _box;
    /** Whether {@link #_box} has been computed (the box itself may be {@code null}). */
    private boolean _boxComputed = false;

    /** Creates an intersectable; the bounding box is computed on first use. */
    protected Intersectable() {
    }

    /**
     * Returns the axis-aligned bounding box, computing it on first use.
     *
     * @return the bounding box, or {@code null} for unbounded geometries such as planes
     */
    public final AABB getBoundingBox() {
        if (!_boxComputed) {
            _box = createBoundingBox();
            _boxComputed = true;
        }
        return _box;
    }

    /**
     * Calculates the bounding box of the concrete geometry.
     *
     * @return the bounding box, or {@code null} if the geometry is unbounded
     */
    protected abstract AABB createBoundingBox();

    /**
     * Finds all the intersections of a ray with this object.
     *
     * @param ray the ray
     * @return the intersections, or {@code null} if there are none
     */
    public final List<Intersection> calcIntersections(Ray ray) {
        return calcIntersectionsHelper(ray, Double.POSITIVE_INFINITY);
    }

    /**
     * Finds the intersections of a ray with this object, up to a maximum distance from the ray
     * origin. Used by shadow rays, which only care about occluders closer than the light.
     *
     * @param ray         the ray
     * @param maxDistance the maximum distance of an intersection from the ray origin
     * @return the intersections, or {@code null} if there are none
     */
    public final List<Intersection> calcIntersections(Ray ray, double maxDistance) {
        return calcIntersectionsHelper(ray, maxDistance);
    }

    /**
     * Finds the intersection points of a ray with this object.
     *
     * @param ray the ray
     * @return the intersection points, or {@code null} if there are none
     */
    public final List<Point> findIntersections(Ray ray) {
        var intersections = calcIntersections(ray);
        return intersections == null ? null : intersections.stream().map(i -> i.point).toList();
    }

    /**
     * Calculates the intersections of the concrete object with a ray.
     *
     * @param ray         the ray
     * @param maxDistance the maximum distance of an intersection from the ray origin
     * @return the intersections, or {@code null} if there are none
     */
    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance);

    /**
     * A ray-surface intersection, together with the shading data the ray tracer attaches to it
     * while computing the color at that point.
     * <p>
     * A passive data structure: the geometric fields are set at construction, and the shading
     * fields are filled in by the ray tracer to avoid recomputing shared vectors and dot products.
     */
    public static final class Intersection {

        /** The intersected geometry ({@code null} for bare points). */
        public final Geometry geometry;

        /** The intersection point. */
        public final Point point;

        /** The material of the intersected geometry. */
        public final Material material;

        /** The surface normal at the point. */
        public Vector normal;

        /** The direction of the incoming ray. */
        public Vector v;

        /** The dot product of {@link #v} and {@link #normal}. */
        public double vNormal;

        /** The light source currently being evaluated. */
        public LightSource light;

        /** The direction from the current light source to the point. */
        public Vector l;

        /** The dot product of {@link #l} and {@link #normal}. */
        public double lNormal;

        /**
         * Creates an intersection.
         *
         * @param geometry the intersected geometry, or {@code null}
         * @param point    the intersection point
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
            this.material = geometry == null ? new Material() : geometry.getMaterial();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof Intersection other && geometry == other.geometry && point.equals(other.point);
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(geometry), point);
        }

        @Override
        public String toString() {
            return "Intersection{geometry=" + geometry + ", point=" + point + '}';
        }
    }
}
