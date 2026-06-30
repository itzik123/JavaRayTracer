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
}