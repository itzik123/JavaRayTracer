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

    /** Maximum depth of recursion for global effects (reflection/refraction) */
    private static final int MAX_CALC_COLOR_LEVEL = 10;

    /** Minimum attenuation factor to continue recursion */
    private static final double MIN_CALC_COLOR_K = 0.001;

    /** Initial attenuation factor */
    private static final Double3 INITIAL_K = Double3.ONE;

    /**
     * Finds the closest intersection point for a given ray.
     * @param ray the ray to check
     * @return the closest intersection, or null if no intersections are found
     */
    private Intersection findClosestIntersection(Ray ray) {
        var intersections = _scene.geometries.calcIntersections(ray);
        return intersections == null ? null : ray.findClosestIntersection(intersections);
    }

    /**
     * Checks whether a point is unshaded by other objects from a specific light source.
     * @param intersection the intersection point containing the geometric data and light source
     * @return true if the point is unshaded, false if it's in shadow
     */
    private boolean unshaded(Intersection intersection) {
        Vector lightDirection = intersection.light.getL(intersection.point).scale(-1);

        Ray lightRay = new Ray(intersection.point, lightDirection, intersection.normal);

        double maxDistance = intersection.light.getDistance(intersection.point);
        var intersections = _scene.geometries.calcIntersections(lightRay, maxDistance);

        if (intersections == null) return true;

        for (Intersection geo : intersections) {
            // אם המסתיר הוא כמעט אטום לגמרי - יש צל מוחלט!
            if (geo.material.kT.isLowerThan(MIN_CALC_COLOR_K)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calculates the transparency attenuation factor for shadows.
     * @param intersection the intersection point containing the geometric data and light source
     * @return the accumulated transparency attenuation factor (Double3)
     */
    private Double3 transparency(Intersection intersection) {
        Vector lightDirection = intersection.light.getL(intersection.point).scale(-1);
        Ray lightRay = new Ray(intersection.point, lightDirection, intersection.normal);

        double maxDistance = intersection.light.getDistance(intersection.point);
        var intersections = _scene.geometries.calcIntersections(lightRay, maxDistance);

        if (intersections == null) return Double3.ONE;

        Double3 ktr = Double3.ONE;
        for (Intersection geo : intersections) {
            ktr = ktr.product(geo.material.kT);

            if (ktr.isLowerThan(MIN_CALC_COLOR_K)) {
                return Double3.ZERO;
            }
        }
        return ktr;
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
        Intersection intersection = findClosestIntersection(ray);
        return intersection == null ? _scene.background
                : calcColor(intersection, ray.direction());
    }

    /**
     * Wrapper method for calculating the color at an intersection.
     * Adds ambient light only once at the beginning of the ray path.
     */
    private Color calcColor(Intersection intersection, Vector v) {
        return !preprocessIntersection(intersection, v) ? Color.BLACK
                : _scene.ambientLight.getIntensity().scale(intersection.material.kA)
                .add(calcColor(intersection, MAX_CALC_COLOR_LEVEL, INITIAL_K));
    }

    /**
     * Recursive method to calculate the color at an intersection,
     * combining local and global effects.
     */
    private Color calcColor(Intersection intersection, int level, Double3 k) {
        Color color = calcLocalEffects(intersection, k);
        return 1 == level ? color
                : color.add(calcGlobalEffects(intersection, level, k));
    }

    /**
     * Constructs a reflection ray.
     */
    private Ray constructReflectionRay(Intersection intersection) {
        Vector v = intersection.v;
        Vector n = intersection.normal;
        double vn = intersection.vNormal;

        Vector r = v.subtract(n.scale(2 * vn)); // r = v - 2 * (v * n) * n
        return new Ray(intersection.point, r, n);
    }

    /**
     * Constructs a refraction (transparency) ray.
     */
    private Ray constructRefractionRay(Intersection intersection) {
        return new Ray(intersection.point, intersection.v, intersection.normal);
    }

    /**
     * Calculates all global effects (reflection and refraction) recursively.
     */
    private Color calcGlobalEffects(Intersection intersection, int level, Double3 k) {
        Color color = Color.BLACK;
        Material material = intersection.material;

        Double3 kR = material.kR;
        if (!kR.equals(Double3.ZERO)) {
            Ray reflectionRay = constructReflectionRay(intersection);
            color = color.add(calcGlobalEffect(reflectionRay, level, k, kR));
        }

        Double3 kT = material.kT;
        if (!kT.equals(Double3.ZERO)) {
            Ray refractionRay = constructRefractionRay(intersection);
            color = color.add(calcGlobalEffect(refractionRay, level, k, kT));
        }

        return color;
    }

    /**
     * Calculates the color contribution of a single global effect (secondary ray).
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {
        Double3 kkx = k.product(kx);
        // עצירה אם ההשפעה זניחה
        if (kkx.isLowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;

        Intersection intersection = findClosestIntersection(ray);
        if (intersection == null) return _scene.background.scale(kx);

        return !preprocessIntersection(intersection, ray.direction()) ? Color.BLACK
                : calcColor(intersection, level - 1, kkx).scale(kx);
    }

    /**
     * Calculates the local lighting effects (Emission + Diffuse + Specular) for all light sources.
     * @param intersection the intersection point context
     * @param k the accumulated attenuation factor from recursion
     * @return the local color contribution
     */
    private Color calcLocalEffects(Intersection intersection, Double3 k) {
        Color color = intersection.geometry.getEmission();
        for (LightSource lightSource : _scene.lights) {
            if (preprocessLightSource(intersection, lightSource)) {

                Double3 ktr = transparency(intersection);

                if (ktr.product(k).isGreaterThan(MIN_CALC_COLOR_K)) {
                    Color lightIntensity = lightSource.getIntensity(intersection.point).scale(ktr);
                    color = color.add(lightIntensity.scale(calcDiffuse(intersection).add(calcSpecular(intersection))));
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