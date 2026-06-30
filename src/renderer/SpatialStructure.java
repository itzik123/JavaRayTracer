package renderer;

import geometries.api.Intersectable;
import primitives.AABB;
import primitives.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for spatial partitioning structures.
 * Responsible for calculating the global scene bounding box and filtering geometries.
 */
public abstract class SpatialStructure {

    protected AABB _sceneBox;
    protected final List<Intersectable> _infiniteGeometries = new ArrayList<>();
    protected final List<Intersectable> _finiteGeometries = new ArrayList<>();

    /**
     * Protected constructor that processes the geometries and calculates the scene bounds.
     * @param geometriesList the flat list of all geometries
     */
    protected SpatialStructure(List<Intersectable> geometriesList) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        boolean hasFiniteGeometries = false;

        for (Intersectable geo : geometriesList) {
            geo.setBoundingBox();
            AABB box = geo.getBoundingBox();

            if (box == null) {
                _infiniteGeometries.add(geo);
            } else {
                _finiteGeometries.add(geo); // שומרים את הסופיים ברשימה נפרדת כדי שהיורשים ישתמשו בהם!
                hasFiniteGeometries = true;

                if (box.min.getX() < minX) minX = box.min.getX();
                if (box.min.getY() < minY) minY = box.min.getY();
                if (box.min.getZ() < minZ) minZ = box.min.getZ();

                if (box.max.getX() > maxX) maxX = box.max.getX();
                if (box.max.getY() > maxY) maxY = box.max.getY();
                if (box.max.getZ() > maxZ) maxZ = box.max.getZ();
            }
        }

        if (hasFiniteGeometries) {
            double delta = 0.1;
            _sceneBox = new AABB(
                    new Point(minX - delta, minY - delta, minZ - delta),
                    new Point(maxX + delta, maxY + delta, maxZ + delta)
            );
        }
    }

    public List<Intersectable> getInfiniteGeometries() { return _infiniteGeometries; }
    public AABB getSceneBox() { return _sceneBox; }
}