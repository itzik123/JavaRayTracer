package renderer;

import geometries.api.Intersectable;
import primitives.AABB;
import primitives.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a 3D Regular Grid for spatial partitioning.
 */
public class RegularGrid {

    /** Record representing a 3D index of a voxel */
    public record VoxelIndex(int x, int y, int z) {}

    private final int _resolution;
    private AABB _sceneBox;
    private final Map<VoxelIndex, List<Intersectable>> _voxels = new HashMap<>();
    private final List<Intersectable> _infiniteGeometries = new ArrayList<>();

    private double _voxelSizeX;
    private double _voxelSizeY;
    private double _voxelSizeZ;

    /**
     * Constructor builds the voxel grid from a list of geometries.
     * @param geometriesList a flat list of geometries
     * @param resolution the number of voxels per axis (Nx = Ny = Nz = resolution)
     */
    public RegularGrid(List<Intersectable> geometriesList, int resolution) {
        _resolution = resolution;
        buildGrid(geometriesList);
    }

    private void buildGrid(List<Intersectable> geometriesList) {
        // 1. חישוב הקופסה התוחמת של הסצנה כולה
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        boolean hasFiniteGeometries = false;

        for (Intersectable geo : geometriesList) {
            geo.setBoundingBox();
            AABB box = geo.getBoundingBox();

            if (box == null) {
                _infiniteGeometries.add(geo); // גופים אינסופיים נשמרים בנפרד
            } else {
                hasFiniteGeometries = true;
                if (box.min.getX() < minX) minX = box.min.getX();
                if (box.min.getY() < minY) minY = box.min.getY();
                if (box.min.getZ() < minZ) minZ = box.min.getZ();

                if (box.max.getX() > maxX) maxX = box.max.getX();
                if (box.max.getY() > maxY) maxY = box.max.getY();
                if (box.max.getZ() > maxZ) maxZ = box.max.getZ();
            }
        }

        if (!hasFiniteGeometries) return;

        // הרחבה קלה של גבולות הסצנה כדי למנוע חריגות אינדקס של נקודות קצה (Floating point issues)
        double delta = 0.1;
        _sceneBox = new AABB(
                new Point(minX - delta, minY - delta, minZ - delta),
                new Point(maxX + delta, maxY + delta, maxZ + delta)
        );

        // 2. חישוב גודלו של ווקסל בודד לכל ציר
        _voxelSizeX = (_sceneBox.max.getX() - _sceneBox.min.getX()) / _resolution;
        _voxelSizeY = (_sceneBox.max.getY() - _sceneBox.min.getY()) / _resolution;
        _voxelSizeZ = (_sceneBox.max.getZ() - _sceneBox.min.getZ()) / _resolution;

        // 3. שיבוץ הגופים למילון הווקסלים (Mapping)
        for (Intersectable geo : geometriesList) {
            AABB box = geo.getBoundingBox();
            if (box == null) continue;

            // מציאת טווח האינדקסים של הווקסלים שהגוף "נוגע" בהם
            int minVoxelX = (int) ((box.min.getX() - _sceneBox.min.getX()) / _voxelSizeX);
            int maxVoxelX = (int) ((box.max.getX() - _sceneBox.min.getX()) / _voxelSizeX);

            int minVoxelY = (int) ((box.min.getY() - _sceneBox.min.getY()) / _voxelSizeY);
            int maxVoxelY = (int) ((box.max.getY() - _sceneBox.min.getY()) / _voxelSizeY);

            int minVoxelZ = (int) ((box.min.getZ() - _sceneBox.min.getZ()) / _voxelSizeZ);
            int maxVoxelZ = (int) ((box.max.getZ() - _sceneBox.min.getZ()) / _voxelSizeZ);

            // הוספת הגוף לכל ווקסל שנמצא בטווח
            for (int x = minVoxelX; x <= maxVoxelX; x++) {
                for (int y = minVoxelY; y <= maxVoxelY; y++) {
                    for (int z = minVoxelZ; z <= maxVoxelZ; z++) {
                        VoxelIndex idx = new VoxelIndex(x, y, z);
                        // אם הווקסל לא קיים במילון - הוא נוצר. לאחר מכן מוסיפים לו את הגוף.
                        _voxels.computeIfAbsent(idx, k -> new ArrayList<>()).add(geo);
                    }
                }
            }
        }
    }

    // ========== Getters שישמשו את אלגוריתם סריקת הקרניים בהמשך ==========

    public List<Intersectable> getVoxel(int x, int y, int z) {
        return _voxels.get(new VoxelIndex(x, y, z));
    }

    public List<Intersectable> getInfiniteGeometries() { return _infiniteGeometries; }
    public AABB getSceneBox() { return _sceneBox; }
    public int getResolution() { return _resolution; }
    public double getVoxelSizeX() { return _voxelSizeX; }
    public double getVoxelSizeY() { return _voxelSizeY; }
    public double getVoxelSizeZ() { return _voxelSizeZ; }
}