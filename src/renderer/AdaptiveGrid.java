package renderer;

import geometries.api.Intersectable;
import java.util.List;

/**
 * Represents an Adaptive Grid (Octree) for spatial partitioning.
 * This spatial structure subdivides the 3D space adaptively based on the
 * concentration of geometries to optimize intersection calculations.
 */
public class AdaptiveGrid extends SpatialStructure {

    /** The root node of the adaptive grid (Octree). */
    private AdaptiveNode _root;

    /**
     * Constructs an AdaptiveGrid (Octree) structure with the specified geometries,
     * maximum capacity per node, and maximum depth level.
     * If the scene's bounding box is properly initialized, it automatically builds the tree.
     *
     * @param geometriesList the list of intersectable geometries in the scene
     * @param maxGeometries  the maximum number of geometries allowed in a single leaf node before it splits
     * @param maxDepth       the maximum allowed recursion depth for the tree
     */
    public AdaptiveGrid(List<Intersectable> geometriesList, int maxGeometries, int maxDepth) {
        super(geometriesList); // מחלקת האב מחשבת את _sceneBox ואת _finiteGeometries

        if (_sceneBox != null) {
            buildTree(maxGeometries, maxDepth);
        }
    }

    /**
     * Builds the spatial partitioning tree starting from the root node.
     * It adds all finite geometries to the root and then recursively splits
     * the nodes until the constraints (max depth or max geometries) are met.
     *
     * @param maxGeometries the maximum number of geometries allowed in a leaf node
     * @param maxDepth      the maximum recursion depth for splitting the nodes
     */
    private void buildTree(int maxGeometries, int maxDepth) {
        _root = new AdaptiveNode(_sceneBox);

        for (Intersectable geo : _finiteGeometries) {
            _root.addGeometry(geo);
        }

        // קסם הרקורסיה: פיצול העץ!
        _root.split(0, maxGeometries, maxDepth);
    }

    /**
     * Gets the root node of the adaptive grid.
     *
     * @return the root {@link AdaptiveNode} of the octree structure
     */
    public AdaptiveNode getRoot() { return _root; }
}