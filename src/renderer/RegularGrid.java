package renderer;

import geometries.api.Intersectable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegularGrid extends SpatialStructure {

    public record VoxelIndex(int x, int y, int z) {}

    private final int _resolution;
    private final Map<VoxelIndex, List<Intersectable>> _voxels = new HashMap<>();

    private double _voxelSizeX, _voxelSizeY, _voxelSizeZ;

    public RegularGrid(List<Intersectable> geometriesList, int resolution) {
        super(geometriesList);
        _resolution = resolution;

        if (_sceneBox != null) {
            buildGrid();
        }
    }

    private void buildGrid() {
        _voxelSizeX = (_sceneBox.max.getX() - _sceneBox.min.getX()) / _resolution;
        _voxelSizeY = (_sceneBox.max.getY() - _sceneBox.min.getY()) / _resolution;
        _voxelSizeZ = (_sceneBox.max.getZ() - _sceneBox.min.getZ()) / _resolution;

        // עוברים רק על הגופים הסופיים שהוכנו במחלקת האב!
        for (Intersectable geo : _finiteGeometries) {
            primitives.AABB box = geo.getBoundingBox();

            int minVoxelX = (int) ((box.min.getX() - _sceneBox.min.getX()) / _voxelSizeX);
            int maxVoxelX = (int) ((box.max.getX() - _sceneBox.min.getX()) / _voxelSizeX);
            int minVoxelY = (int) ((box.min.getY() - _sceneBox.min.getY()) / _voxelSizeY);
            int maxVoxelY = (int) ((box.max.getY() - _sceneBox.min.getY()) / _voxelSizeY);
            int minVoxelZ = (int) ((box.min.getZ() - _sceneBox.min.getZ()) / _voxelSizeZ);
            int maxVoxelZ = (int) ((box.max.getZ() - _sceneBox.min.getZ()) / _voxelSizeZ);

            for (int x = minVoxelX; x <= maxVoxelX; x++) {
                for (int y = minVoxelY; y <= maxVoxelY; y++) {
                    for (int z = minVoxelZ; z <= maxVoxelZ; z++) {
                        VoxelIndex idx = new VoxelIndex(x, y, z);
                        _voxels.computeIfAbsent(idx, k -> new ArrayList<>()).add(geo);
                    }
                }
            }
        }
    }

    public List<Intersectable> getVoxel(int x, int y, int z) { return _voxels.get(new VoxelIndex(x, y, z)); }
    public int getResolution() { return _resolution; }
    public double getVoxelSizeX() { return _voxelSizeX; }
    public double getVoxelSizeY() { return _voxelSizeY; }
    public double getVoxelSizeZ() { return _voxelSizeZ; }
}