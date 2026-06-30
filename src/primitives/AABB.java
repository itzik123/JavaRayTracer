package primitives;

/**
 * Represents an Axis-Aligned Bounding Box (AABB) for spatial partitioning.
 */
public class AABB {
    public final Point min;
    public final Point max;

    /**
     * Constructor for AABB.
     * @param min the minimum point of the box (lowest x, y, z)
     * @param max the maximum point of the box (highest x, y, z)
     */
    public AABB(Point min, Point max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Checks if a ray intersects this bounding box using the Slab Method.
     * @param ray the ray to check
     * @return true if the ray intersects the box, false otherwise
     */
    public boolean isIntersected(Ray ray) {
        Point o = ray.origin();
        Vector d = ray.direction();

        double dirX = d.getX();
        double dirY = d.getY();
        double dirZ = d.getZ();

        double origX = o.getX();
        double origY = o.getY();
        double origZ = o.getZ();

        // בדיקה עבור ציר X
        double tMinX = (min.getX() - origX) / dirX;
        double tMaxX = (max.getX() - origX) / dirX;
        if (tMinX > tMaxX) { double temp = tMinX; tMinX = tMaxX; tMaxX = temp; }

        // בדיקה עבור ציר Y
        double tMinY = (min.getY() - origY) / dirY;
        double tMaxY = (max.getY() - origY) / dirY;
        if (tMinY > tMaxY) { double temp = tMinY; tMinY = tMaxY; tMaxY = temp; }

        // אם תחומי ה-X וה-Y לא חופפים, אין חיתוך עם הקופסה
        if ((tMinX > tMaxY) || (tMinY > tMaxX)) return false;

        // עדכון תחום החיתוך המשותף ל-X ול-Y
        if (tMinY > tMinX) tMinX = tMinY;
        if (tMaxY < tMaxX) tMaxX = tMaxY;

        // בדיקה עבור ציר Z
        double tMinZ = (min.getZ() - origZ) / dirZ;
        double tMaxZ = (max.getZ() - origZ) / dirZ;
        if (tMinZ > tMaxZ) { double temp = tMinZ; tMinZ = tMaxZ; tMaxZ = temp; }

        // אם תחומי ה-(X,Y) לא חופפים עם תחום ה-Z, אין חיתוך
        if ((tMinX > tMaxZ) || (tMinZ > tMaxX)) return false;

        return true;
    }

    /**
     * Calculates the distance (t) from the ray origin to the intersection point with the AABB.
     * @param ray the ray to check
     * @return the t value of the intersection, or Double.NEGATIVE_INFINITY if it misses.
     */
    public double findIntersectionT(Ray ray) {
        Point o = ray.origin();
        Vector d = ray.direction();

        double tMinX = (min.getX() - o.getX()) / d.getX();
        double tMaxX = (max.getX() - o.getX()) / d.getX();
        if (tMinX > tMaxX) { double temp = tMinX; tMinX = tMaxX; tMaxX = temp; }

        double tMinY = (min.getY() - o.getY()) / d.getY();
        double tMaxY = (max.getY() - o.getY()) / d.getY();
        if (tMinY > tMaxY) { double temp = tMinY; tMinY = tMaxY; tMaxY = temp; }

        if ((tMinX > tMaxY) || (tMinY > tMaxX)) return Double.NEGATIVE_INFINITY;

        if (tMinY > tMinX) tMinX = tMinY;
        if (tMaxY < tMaxX) tMaxX = tMaxY;

        double tMinZ = (min.getZ() - o.getZ()) / d.getZ();
        double tMaxZ = (max.getZ() - o.getZ()) / d.getZ();
        if (tMinZ > tMaxZ) { double temp = tMinZ; tMinZ = tMaxZ; tMaxZ = temp; }

        if ((tMinX > tMaxZ) || (tMinZ > tMaxX)) return Double.NEGATIVE_INFINITY;

        if (tMinZ > tMinX) tMinX = tMinZ;
        if (tMaxZ < tMaxX) tMaxX = tMaxZ;

        if (tMaxX < 0) return Double.NEGATIVE_INFINITY; // הקופסה נמצאת מאחורי הקרן
        return tMinX < 0 ? 0 : tMinX; // אם ראשית הקרן בתוך הקופסה, המרחק לכניסה הוא 0
    }

    /**
     * Checks if this bounding box overlaps with another bounding box.
     * @param other the other AABB to check against
     * @return true if the boxes overlap, false otherwise
     */
    public boolean isOverlapping(AABB other) {
        if (other == null) return false;

        boolean overlapX = this.min.getX() <= other.max.getX() && this.max.getX() >= other.min.getX();
        boolean overlapY = this.min.getY() <= other.max.getY() && this.max.getY() >= other.min.getY();
        boolean overlapZ = this.min.getZ() <= other.max.getZ() && this.max.getZ() >= other.min.getZ();

        return overlapX && overlapY && overlapZ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AABB other = (AABB) obj;
        return min.equals(other.min) && max.equals(other.max);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return "AABB [min=" + min + ", max=" + max + "]";
    }
}