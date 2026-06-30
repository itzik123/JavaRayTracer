package renderer;

import geometries.api.Intersectable;
import java.util.List;

/**
 * Represents an Adaptive Grid (Octree) for spatial partitioning.
 */
public class AdaptiveGrid extends SpatialStructure {

    private AdaptiveNode _root;

    public AdaptiveGrid(List<Intersectable> geometriesList, int maxGeometries, int maxDepth) {
        super(geometriesList); // מחלקת האב מחשבת את _sceneBox ואת _finiteGeometries

        if (_sceneBox != null) {
            buildTree(maxGeometries, maxDepth);
        }
    }

    private void buildTree(int maxGeometries, int maxDepth) {
        _root = new AdaptiveNode(_sceneBox);

        for (Intersectable geo : _finiteGeometries) {
            _root.addGeometry(geo);
        }

        // קסם הרקורסיה: פיצול העץ!
        _root.split(0, maxGeometries, maxDepth);
    }

    public AdaptiveNode getRoot() { return _root; }
}