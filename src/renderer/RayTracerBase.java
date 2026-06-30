package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Color;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.alignZero;

abstract class RayTracerBase {
    protected final Scene _scene;

    protected int _adaptiveAASDepth = 0;
    protected double _adaptiveAASThreshold = 0.0;

    /**
     * Sets the Adaptive Super-Sampling configuration.
     */
    public RayTracerBase setAdaptiveSuperSampling(int depth, double threshold) {
        this._adaptiveAASDepth = depth;
        this._adaptiveAASThreshold = threshold;
        return this;
    }

    abstract Color traceRay(Ray ray);

    RayTracerBase(Scene scene){
        _scene = scene;
    }

    protected boolean preprocessIntersection(Intersection intersection, Vector v){
        intersection.v = v;
        intersection.normal = intersection.geometry.getNormal(intersection.point);
        intersection.vNormal = alignZero(intersection.v.dotProduct(intersection.normal));
        return intersection.vNormal != 0;
    }

    protected boolean preprocessLightSource(Intersection intersection, LightSource light) {
        intersection.light = light;
        intersection.l = light.getL(intersection.point);
        intersection.lNormal = alignZero(intersection.l.dotProduct(intersection.normal));
        return intersection.lNormal * intersection.vNormal > 0;
    }
}
