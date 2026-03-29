package geometries.impl;

import geometries.api.Intersectable;
import primitives.Point;
import primitives.Ray;

import java.util.ArrayList;
import java.util.Collections;
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
    public List<Point> findIntersections(Ray ray) {
        List<Point> result = null;

        for (Intersectable geometry : geometries) {
            List<Point> geoIntersections = geometry.findIntersections(ray);

            if (geoIntersections != null) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.addAll(geoIntersections);
            }
        }
        return result;
    }
}