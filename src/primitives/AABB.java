package primitives;

import java.util.Collection;

/**
 * An immutable axis-aligned bounding box, the building block of the spatial acceleration
 * structures.
 *
 * @param min the corner with the smallest coordinates
 * @param max the corner with the largest coordinates
 * @author itzik123
 */
public record AABB(Point min, Point max) {

    /** Returned by {@link #findIntersectionT(Ray)} when the ray misses the box. */
    public static final double NO_HIT = Double.NEGATIVE_INFINITY;

    /**
     * Creates the smallest box enclosing a set of points.
     *
     * @param points the points to enclose (must not be empty)
     * @return the enclosing box
     */
    public static AABB enclosing(Collection<Point> points) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
        for (Point p : points) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }
        return new AABB(new Point(minX, minY, minZ), new Point(maxX, maxY, maxZ));
    }

    /**
     * Creates the smallest box enclosing this box and another one.
     *
     * @param other the other box, or {@code null}
     * @return the enclosing box ({@code this} when {@code other} is {@code null})
     */
    public AABB union(AABB other) {
        if (other == null) return this;
        return new AABB(
                new Point(Math.min(min.getX(), other.min.getX()),
                        Math.min(min.getY(), other.min.getY()),
                        Math.min(min.getZ(), other.min.getZ())),
                new Point(Math.max(max.getX(), other.max.getX()),
                        Math.max(max.getY(), other.max.getY()),
                        Math.max(max.getZ(), other.max.getZ())));
    }

    /**
     * Grows the box by a margin on every side.
     *
     * @param margin the margin to add
     * @return the expanded box
     */
    public AABB expand(double margin) {
        return new AABB(
                new Point(min.getX() - margin, min.getY() - margin, min.getZ() - margin),
                new Point(max.getX() + margin, max.getY() + margin, max.getZ() + margin));
    }

    /**
     * Finds where a ray enters the box, using the slab method: the ray is clipped against the
     * pair of planes bounding each axis, and it hits the box if the three parameter intervals
     * overlap in front of the origin.
     * <p>
     * Division by a zero direction component yields ±infinity, which the method handles
     * naturally. The interval updates use plain comparisons rather than {@code Math.min/max} so
     * that an undefined slab (a ray lying exactly in a slab plane) does not constrain the result.
     *
     * @param ray the ray
     * @return the entry distance along the ray (0 when the origin is inside the box),
     *         or {@link #NO_HIT} if the ray misses the box
     */
    public double findIntersectionT(Ray ray) {
        Point o = ray.origin();
        Vector d = ray.direction();

        double tMin = (min.getX() - o.getX()) / d.getX();
        double tMax = (max.getX() - o.getX()) / d.getX();
        if (tMin > tMax) { double tmp = tMin; tMin = tMax; tMax = tmp; }

        double tMinY = (min.getY() - o.getY()) / d.getY();
        double tMaxY = (max.getY() - o.getY()) / d.getY();
        if (tMinY > tMaxY) { double tmp = tMinY; tMinY = tMaxY; tMaxY = tmp; }

        if (tMin > tMaxY || tMinY > tMax) return NO_HIT;
        if (tMinY > tMin) tMin = tMinY;
        if (tMaxY < tMax) tMax = tMaxY;

        double tMinZ = (min.getZ() - o.getZ()) / d.getZ();
        double tMaxZ = (max.getZ() - o.getZ()) / d.getZ();
        if (tMinZ > tMaxZ) { double tmp = tMinZ; tMinZ = tMaxZ; tMaxZ = tmp; }

        if (tMin > tMaxZ || tMinZ > tMax) return NO_HIT;
        if (tMinZ > tMin) tMin = tMinZ;
        if (tMaxZ < tMax) tMax = tMaxZ;

        if (tMax < 0) return NO_HIT; // the box is behind the ray
        return tMin < 0 ? 0 : tMin;
    }

    /**
     * Checks whether this box overlaps another box (touching counts as overlapping).
     *
     * @param other the other box
     * @return {@code true} if the boxes overlap
     */
    public boolean isOverlapping(AABB other) {
        return other != null
                && min.getX() <= other.max.getX() && max.getX() >= other.min.getX()
                && min.getY() <= other.max.getY() && max.getY() >= other.min.getY()
                && min.getZ() <= other.max.getZ() && max.getZ() >= other.min.getZ();
    }
}
