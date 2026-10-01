package renderer.acceleration;

import geometries.api.Intersectable;
import geometries.api.Intersectable.Intersection;
import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A uniform voxel grid over the scene box.
 * <p>
 * Every bounded geometry is registered in all the voxels its bounding box overlaps. A ray then
 * walks only through the voxels it actually crosses, in front-to-back order, using the 3D-DDA
 * algorithm (Amanatides &amp; Woo): at each step it moves to the neighboring voxel whose boundary
 * is closest along the ray. Closest-hit queries stop as soon as a hit lies inside the current
 * voxel, since nothing in a later voxel can be closer.
 * <p>
 * A geometry spanning several voxels is tested at most once per ray ("mailboxing").
 *
 * @author itzik123
 */
public class RegularGrid extends SpatialStructure {

    /** The number of voxels along each axis. */
    private final int _resolution;
    /** The voxel dimensions. */
    private final double _voxelSizeX, _voxelSizeY, _voxelSizeZ;
    /** The geometries of every voxel, indexed by {@link #cellIndex}; {@code null} for empty voxels. */
    private final Intersectable[][] _cells;

    /**
     * Receives the voxels crossed by a ray, in front-to-back order.
     */
    @FunctionalInterface
    private interface VoxelVisitor {
        /**
         * Handles one voxel crossed by the ray.
         *
         * @param geometries the geometries of the voxel, or {@code null} if it is empty
         * @param exitT      the distance along the ray where it leaves the voxel
         * @return {@code true} to continue to the next voxel
         */
        boolean visit(Intersectable[] geometries, double exitT);
    }

    /**
     * Builds the grid.
     *
     * @param geometries the flat list of the scene geometries
     * @param resolution the number of voxels along each axis
     * @throws IllegalArgumentException if the resolution is not positive
     */
    public RegularGrid(List<Intersectable> geometries, int resolution) {
        super(geometries);
        if (resolution < 1) throw new IllegalArgumentException("Grid resolution must be positive");
        _resolution = resolution;

        if (_sceneBox == null) {
            _voxelSizeX = _voxelSizeY = _voxelSizeZ = 0;
            _cells = new Intersectable[0][];
            return;
        }
        _voxelSizeX = (_sceneBox.max().getX() - _sceneBox.min().getX()) / resolution;
        _voxelSizeY = (_sceneBox.max().getY() - _sceneBox.min().getY()) / resolution;
        _voxelSizeZ = (_sceneBox.max().getZ() - _sceneBox.min().getZ()) / resolution;
        _cells = buildCells();
    }

    /**
     * Registers every finite geometry in the voxels its bounding box overlaps.
     *
     * @return the voxel contents
     */
    private Intersectable[][] buildCells() {
        List<List<Intersectable>> cells = new ArrayList<>(_resolution * _resolution * _resolution);
        for (int i = 0; i < _resolution * _resolution * _resolution; i++) cells.add(null);

        Point min = _sceneBox.min();
        for (Intersectable geometry : _finiteGeometries) {
            AABB box = geometry.getBoundingBox();
            int x0 = voxel(box.min().getX(), min.getX(), _voxelSizeX);
            int x1 = voxel(box.max().getX(), min.getX(), _voxelSizeX);
            int y0 = voxel(box.min().getY(), min.getY(), _voxelSizeY);
            int y1 = voxel(box.max().getY(), min.getY(), _voxelSizeY);
            int z0 = voxel(box.min().getZ(), min.getZ(), _voxelSizeZ);
            int z1 = voxel(box.max().getZ(), min.getZ(), _voxelSizeZ);
            for (int x = x0; x <= x1; x++)
                for (int y = y0; y <= y1; y++)
                    for (int z = z0; z <= z1; z++) {
                        int index = cellIndex(x, y, z);
                        if (cells.get(index) == null) cells.set(index, new ArrayList<>());
                        cells.get(index).add(geometry);
                    }
        }

        Intersectable[][] result = new Intersectable[cells.size()][];
        for (int i = 0; i < result.length; i++) {
            List<Intersectable> cell = cells.get(i);
            if (cell != null) result[i] = cell.toArray(Intersectable[]::new);
        }
        return result;
    }

    /**
     * Converts a coordinate to a voxel index along one axis, clamped to the grid.
     *
     * @param coordinate the coordinate
     * @param min        the minimum of the scene box along the axis
     * @param voxelSize  the voxel size along the axis
     * @return the voxel index
     */
    private int voxel(double coordinate, double min, double voxelSize) {
        int index = (int) ((coordinate - min) / voxelSize);
        return Math.max(0, Math.min(_resolution - 1, index));
    }

    /**
     * Flattens 3D voxel indices into an index of the cell array.
     *
     * @param x the voxel index along X
     * @param y the voxel index along Y
     * @param z the voxel index along Z
     * @return the position of the voxel in {@link #_cells}
     */
    private int cellIndex(int x, int y, int z) {
        return (z * _resolution + y) * _resolution + x;
    }

    @Override
    public Intersection findClosestIntersection(Ray ray) {
        ClosestHit closest = new ClosestHit(ray.origin());
        findClosestAmongInfinite(ray, closest);

        Set<Intersectable> tested = new HashSet<>();
        walk(ray, Double.POSITIVE_INFINITY, (geometries, exitT) -> {
            if (geometries != null)
                for (Intersectable geometry : geometries)
                    if (tested.add(geometry)) closest.offer(geometry.calcIntersections(ray));
            // A hit inside this voxel is closer than anything in the voxels ahead
            return closest.distance() > exitT;
        });
        return closest.intersection();
    }

    @Override
    public void traverse(Ray ray, double maxDistance, IntersectionVisitor visitor) {
        if (!visitInfinite(ray, maxDistance, visitor)) return;

        Set<Intersectable> tested = new HashSet<>();
        walk(ray, maxDistance, (geometries, exitT) -> {
            if (geometries != null)
                for (Intersectable geometry : geometries)
                    if (tested.add(geometry) && !visit(geometry, ray, maxDistance, visitor)) return false;
            return true;
        });
    }

    /**
     * Walks the voxels crossed by a ray with the 3D-DDA algorithm.
     *
     * @param ray         the ray
     * @param maxDistance the distance after which the walk stops
     * @param visitor     receives every crossed voxel until it asks to stop
     */
    private void walk(Ray ray, double maxDistance, VoxelVisitor visitor) {
        if (_sceneBox == null) return;
        double entryT = _sceneBox.findIntersectionT(ray);
        if (entryT == AABB.NO_HIT || entryT > maxDistance) return;

        Point start = entryT > 0 ? ray.getPoint(entryT) : ray.origin();
        Point min = _sceneBox.min();
        int x = voxel(start.getX(), min.getX(), _voxelSizeX);
        int y = voxel(start.getY(), min.getY(), _voxelSizeY);
        int z = voxel(start.getZ(), min.getZ(), _voxelSizeZ);

        Vector dir = ray.direction();
        int stepX = (int) Math.signum(dir.getX());
        int stepY = (int) Math.signum(dir.getY());
        int stepZ = (int) Math.signum(dir.getZ());

        // Distance along the ray to cross one whole voxel on each axis
        double deltaX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(_voxelSizeX / dir.getX());
        double deltaY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(_voxelSizeY / dir.getY());
        double deltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(_voxelSizeZ / dir.getZ());

        // Distance from the start point to the first voxel boundary on each axis
        double nextX = stepX == 0 ? Double.POSITIVE_INFINITY
                : Math.abs((min.getX() + (x + (stepX > 0 ? 1 : 0)) * _voxelSizeX - start.getX()) / dir.getX());
        double nextY = stepY == 0 ? Double.POSITIVE_INFINITY
                : Math.abs((min.getY() + (y + (stepY > 0 ? 1 : 0)) * _voxelSizeY - start.getY()) / dir.getY());
        double nextZ = stepZ == 0 ? Double.POSITIVE_INFINITY
                : Math.abs((min.getZ() + (z + (stepZ > 0 ? 1 : 0)) * _voxelSizeZ - start.getZ()) / dir.getZ());

        while (x >= 0 && x < _resolution && y >= 0 && y < _resolution && z >= 0 && z < _resolution) {
            double exitT = entryT + Math.min(nextX, Math.min(nextY, nextZ));
            if (!visitor.visit(_cells[cellIndex(x, y, z)], exitT) || exitT > maxDistance) return;

            // Step into the neighbor across the nearest boundary
            if (nextX < nextY && nextX < nextZ) {
                x += stepX;
                nextX += deltaX;
            } else if (nextY < nextZ) {
                y += stepY;
                nextY += deltaY;
            } else {
                z += stepZ;
                nextZ += deltaZ;
            }
        }
    }
}
