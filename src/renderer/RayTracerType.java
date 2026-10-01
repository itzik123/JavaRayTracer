package renderer;

/**
 * Selects how the ray tracer finds the geometry a ray hits.
 *
 * @author itzik123
 */
public enum RayTracerType {
    /** Tests every ray against every geometry - simplest, best for small scenes. */
    SIMPLE,

    /** Uniform voxel grid traversed with 3D-DDA - good for evenly spread geometry. */
    REGULAR_GRID,

    /** Octree that subdivides only dense regions - best for detailed meshes. */
    ADAPTIVE_GRID
}
