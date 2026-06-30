package geometries.impl;

import geometries.api.Intersectable;
import primitives.Point;
import primitives.Ray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Composite class representing a collection of geometries.
 * @author ityy35-bit
 */
public class Geometries extends Intersectable {

    /**
     * List of geometries. Initialized immediately to avoid null checks on the list itself.
     */
    private final List<Intersectable> geometries = new ArrayList<>();

    /**
     * Default constructor. Creates an empty collection of geometries.
     */
    public Geometries() {
    }

    /**
     * Constructor that receives a list of geometries and adds them to the collection.
     * @param geometries an array (varargs) of geometries to add
     */
    public Geometries(Intersectable... geometries) {
        add(geometries);
    }

    /**
     * Adds a list of geometries to the current collection.
     * @param geometries an array (varargs) of geometries to add
     */
    public void add(Intersectable... geometries) {
        Collections.addAll(this.geometries, geometries);
    }

    @Override
    protected primitives.AABB createBoundingBox() {
        if (geometries.isEmpty()) return null;

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;

        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        boolean hasFiniteGeometries = false;

        for (Intersectable geo : geometries) {
            geo.setBoundingBox(); // דואגים שהקופסה של הילד תיווצר (NVI)
            primitives.AABB geoBox = geo.getBoundingBox();

            if (geoBox != null) { // מתעלמים מגופים אינסופיים
                hasFiniteGeometries = true;

                if (geoBox.min.getX() < minX) minX = geoBox.min.getX();
                if (geoBox.min.getY() < minY) minY = geoBox.min.getY();
                if (geoBox.min.getZ() < minZ) minZ = geoBox.min.getZ();

                if (geoBox.max.getX() > maxX) maxX = geoBox.max.getX();
                if (geoBox.max.getY() > maxY) maxY = geoBox.max.getY();
                if (geoBox.max.getZ() > maxZ) maxZ = geoBox.max.getZ();
            }
        }

        // אם האוסף הכיל רק גופים אינסופיים (או היה ריק מתוכן ממשי)
        if (!hasFiniteGeometries) return null;

        return new primitives.AABB(new Point(minX, minY, minZ), new Point(maxX, maxY, maxZ));
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> result = null;

        for (Intersectable geometry : geometries) {
            List<Intersection> geoIntersections = geometry.calcIntersections(ray, maxDistance);
            if (geoIntersections != null) {
                if (result == null) {
                    result = new LinkedList<>();
                }
                result.addAll(geoIntersections);
            }
        }
        return result;
    }

    /**
     * Flattens the geometries composite into a single list of geometries.
     * @return a flat list of all Intersectable geometries
     */
    public List<Intersectable> flatten() {
        List<Intersectable> flatList = new ArrayList<>();
        for (Intersectable item : geometries) {
            if (item instanceof Geometries comp) {
                flatList.addAll(comp.flatten()); // קריאה רקורסיבית
            } else {
                flatList.add(item);
            }
        }
        return flatList;
    }
}