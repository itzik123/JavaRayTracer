package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.*;
import scene.Scene;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Simple ray tracer implementation computing basic Phong illumination.
 * @author itzik123
 */
class SimpleRayTracer extends RayTracerBase {

    /** Constant for shifting the ray origin to prevent self-shadowing */
    private static final double DELTA = 0.1;

    /**
     * Checks whether a point is unshaded by other objects from a specific light source.
     * @param intersection the intersection point containing the geometric data and light source
     * @return true if the point is unshaded, false if it's in shadow
     */
    private boolean unshaded(Intersection intersection) {
        Vector lightDirection = intersection.light.getL(intersection.point).scale(-1); // וקטור מהנקודה לאור

        Vector epsVector = intersection.normal.scale(intersection.lNormal < 0 ? DELTA : -DELTA);
        Point point = intersection.point.add(epsVector);
        Ray lightRay = new Ray(point, lightDirection);

        double maxDistance = intersection.light.getDistance(intersection.point);

        var intersections = _scene.geometries.calcIntersections(lightRay, maxDistance);

        return intersections == null;
    }
    /**
     * Constructor.
     * @param scene the scene to render
     */
    public SimpleRayTracer(Scene scene) {
        super(scene);
    }

    @Override
    Color traceRay(Ray ray) {
        var intersections = _scene.geometries.calcIntersections(ray);
        return intersections == null ? _scene.background
                : calcColor(ray.findClosestIntersection(intersections), ray.direction());
    }

    /**
     * Calculates the color at a given intersection point.
     * @param intersection the intersection point context
     * @param v            the ray direction vector
     * @return the computed color
     */
    private Color calcColor(Intersection intersection, Vector v) {
        return !preprocessIntersection(intersection, v) ? Color.BLACK
                : _scene.ambientLight.getIntensity().scale(intersection.material.kA)
                .add(calcLocalEffects(intersection));
    }

    /**
     * Calculates the local lighting effects (Emission + Diffuse + Specular) for all light sources.
     * @param intersection the intersection point context
     * @return the local color contribution
     */
    private Color calcLocalEffects(Intersection intersection) {
        Color color = intersection.geometry.getEmission();
        for (LightSource lightSource : _scene.lights) {
            if (preprocessLightSource(intersection, lightSource)) {
                if (unshaded(intersection)) {
                    color = color.add(lightSource.getIntensity(intersection.point)
                            .scale(calcDiffuse(intersection).add(calcSpecular(intersection))));
                }
            }
        }
        return color;
    }

    /**
     * Calculates the diffuse component of the Phong reflection model: kD * |l * n|
     * @param intersection the intersection point context
     * @return the diffuse attenuation factor
     */
    private Double3 calcDiffuse(Intersection intersection) {
        double factor = Math.abs(intersection.lNormal);
        return intersection.material.kD.scale(factor);
    }

    /**
     * Calculates the specular component of the Phong reflection model: kS * (max(0, -v * r))^nShininess
     * @param intersection the intersection point context
     * @return the specular attenuation factor
     */
    private Double3 calcSpecular(Intersection intersection) {
        double ln = intersection.lNormal;
        if (isZero(ln)) return Double3.ZERO;

        Vector r = intersection.l.subtract(intersection.normal.scale(2 * ln));

        double minusVR = alignZero(-intersection.v.dotProduct(r));
        if (minusVR <= 0) return Double3.ZERO;

        return intersection.material.kS.scale(Math.pow(minusVR, intersection.material.nShininess));
    }
}