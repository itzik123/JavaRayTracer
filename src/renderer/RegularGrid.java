package renderer;

import geometries.api.Intersectable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a uniform spatial acceleration grid (Voxel Grid) used to optimize ray tracing.
 * It divides the scene's bounding box into equal-sized voxels and maps intersectable geometries
 * to the specific voxels they overlap with, reducing the number of intersection tests.
 */
public class RegularGrid extends SpatialStructure {

    /**
     * A record representing the 3D integer indices (coordinates) of a voxel within the grid.
     *
     * @param x the x-axis grid index
     * @param y the y-axis grid index
     * @param z the z-axis grid index
     */
    public record VoxelIndex(int x, int y, int z) {}

    /** The number of voxel partitions along each axis. */
    private final int _resolution;

    /** A map associating each 3D voxel index with the list of geometries overlapping it. */
    private final Map<VoxelIndex, List<Intersectable>> _voxels = new HashMap<>();

    /** The dimensions of a single voxel along the X, Y, and Z axes. */
    private double _voxelSizeX, _voxelSizeY, _voxelSizeZ;

    /**
     * Constructs a RegularGrid acceleration structure with a given list of geometries and grid resolution.
     * If the scene's bounding box is initialized, it automatically builds the grid.
     *
     * @param geometriesList the list of intersectable geometries in the scene
     * @param resolution     the grid partition resolution for all axes
     */
    public RegularGrid(List<Intersectable> geometriesList, int resolution) {
        super(geometriesList);
        _resolution = resolution;

        if (_sceneBox != null) {
            buildGrid();
        }
    }

    /**
     * Computes the voxel dimensions based on the scene's bounding box and resolution,
     * and populates the voxel map by determining which voxels intersect each geometry's bounding box.
     */
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

    /**
     * Retrieves the list of intersectable geometries residing in or overlapping the specified voxel.
     *
     * @param x the x-coordinate of the voxel
     * @param y the y-coordinate of the voxel
     * @param z the z-coordinate of the voxel
     * @return a {@link List} of {@link Intersectable} geometries within the voxel,
     * or {@code null} if the voxel contains no geometries
     */
    public List<Intersectable> getVoxel(int x, int y, int z) { return _voxels.get(new VoxelIndex(x, y, z)); }

    /**
     * Gets the resolution of the grid.
     *
     * @return the grid resolution
     */
    public int getResolution() { return _resolution; }

    /**
     * Gets the voxel size along the X axis.
     *
     * @return the size of a voxel on the X axis
     */
    public double getVoxelSizeX() { return _voxelSizeX; }

    /**
     * Gets the voxel size along the Y axis.
     *
     * @return the size of a voxel on the Y axis
     */
    public double getVoxelSizeY() { return _voxelSizeY; }

    /**
     * Gets the voxel size along the Z axis.
     *
     * @return the size of a voxel on the Z axis
     */
    public double getVoxelSizeZ() { return _voxelSizeZ; }
}