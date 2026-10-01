package renderer;

import primitives.Color;
import primitives.Ray;
import scene.Scene;

/**
 * Base class of ray tracers: computes the color seen along a single ray.
 *
 * @author itzik123
 */
abstract class RayTracerBase {

    /** The scene being rendered. */
    protected final Scene _scene;

    /**
     * Creates a ray tracer for a scene.
     *
     * @param scene the scene to render
     */
    protected RayTracerBase(Scene scene) {
        _scene = scene;
    }

    /**
     * Computes the color seen along a ray.
     *
     * @param ray the ray
     * @return the color seen along the ray
     */
    abstract Color traceRay(Ray ray);
}
