package renderer;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import renderer.acceleration.AccelerationStructure;
import renderer.acceleration.AccelerationStructure.IntersectionVisitor;
import renderer.acceleration.LinearScan;
import renderer.sampling.AdaptiveSuperSampler;
import scene.Scene;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Recursive (Whitted-style) ray tracer with the Phong reflection model.
 * <p>
 * The color at a hit point is its emission, plus the diffuse and specular contribution of every
 * light that is not blocked, plus - recursively - the colors seen along the reflected and
 * refracted rays. Shadows are attenuated by the transparency of the occluders, so transparent
 * objects cast lighter shadows. Materials with a blur radius scatter their secondary rays over a
 * disk, producing glossy reflections and frosted glass; the disk is sampled either with a fixed
 * random beam or adaptively.
 * <p>
 * Geometry queries are delegated to an {@link AccelerationStructure}, so the tracer is unaware of
 * how the scene is organized in space.
 *
 * @author itzik123
 */
class SimpleRayTracer extends RayTracerBase {

    /** Maximum depth of the reflection/refraction recursion. */
    private static final int MAX_CALC_COLOR_LEVEL = 10;
    /** Contributions attenuated below this factor are not traced further. */
    private static final double MIN_CALC_COLOR_K = 0.001;
    /** The attenuation factor of a primary ray. */
    private static final Double3 INITIAL_K = Double3.ONE;

    /** Answers the ray-geometry queries. */
    private final AccelerationStructure _accelerator;
    /** Maximum subdivision depth for adaptive sampling of blurry effects (0 uses random beams). */
    private int _glossyDepth = 0;
    /** Color tolerance for adaptive sampling of blurry effects. */
    private double _glossyTolerance = 0;

    /**
     * Creates a ray tracer that tests every ray against every geometry.
     *
     * @param scene the scene to render
     */
    SimpleRayTracer(Scene scene) {
        this(scene, new LinearScan(scene.geometries));
    }

    /**
     * Creates a ray tracer.
     *
     * @param scene       the scene to render
     * @param accelerator the structure answering ray-geometry queries for the scene
     */
    SimpleRayTracer(Scene scene, AccelerationStructure accelerator) {
        super(scene);
        _accelerator = accelerator;
    }

    /**
     * Enables adaptive sampling of glossy reflections and frosted glass, instead of random beams.
     *
     * @param depth     the maximum subdivision depth (0 disables adaptive sampling)
     * @param tolerance the per-channel color difference under which samples are considered equal
     * @return this ray tracer
     */
    SimpleRayTracer setAdaptiveGlossySampling(int depth, double tolerance) {
        _glossyDepth = depth;
        _glossyTolerance = tolerance;
        return this;
    }

    @Override
    Color traceRay(Ray ray) {
        Intersection intersection = _accelerator.findClosestIntersection(ray);
        return intersection == null ? _scene.background : calcColor(intersection, ray.direction());
    }

    /**
     * Computes the color at the hit point of a primary ray. Ambient light is added only here,
     * once per pixel sample, rather than at every level of the recursion.
     *
     * @param intersection the hit point
     * @param v            the direction of the ray
     * @return the color at the point
     */
    private Color calcColor(Intersection intersection, Vector v) {
        return !preprocessIntersection(intersection, v) ? Color.BLACK
                : _scene.ambientLight.getIntensity().scale(intersection.material.kA)
                .add(calcColor(intersection, MAX_CALC_COLOR_LEVEL, INITIAL_K));
    }

    /**
     * Computes the local and global lighting at a hit point.
     *
     * @param intersection the hit point
     * @param level        the remaining recursion depth
     * @param k            the accumulated attenuation along the ray path
     * @return the color at the point
     */
    private Color calcColor(Intersection intersection, int level, Double3 k) {
        Color color = calcLocalEffects(intersection, k);
        return level == 1 ? color : color.add(calcGlobalEffects(intersection, level, k));
    }

    /**
     * Stores the view-dependent shading data on an intersection.
     *
     * @param intersection the intersection
     * @param v            the direction of the incoming ray
     * @return {@code false} if the ray grazes the surface, which then contributes nothing
     */
    private boolean preprocessIntersection(Intersection intersection, Vector v) {
        intersection.v = v;
        intersection.normal = intersection.geometry.getNormal(intersection.point);
        intersection.vNormal = alignZero(v.dotProduct(intersection.normal));
        return intersection.vNormal != 0;
    }

    /**
     * Stores the light-dependent shading data on an intersection.
     *
     * @param intersection the intersection
     * @param light        the light source
     * @return {@code true} if the light and the viewer are on the same side of the surface
     */
    private boolean preprocessLightSource(Intersection intersection, LightSource light) {
        intersection.light = light;
        intersection.l = light.getL(intersection.point);
        intersection.lNormal = alignZero(intersection.l.dotProduct(intersection.normal));
        return intersection.lNormal * intersection.vNormal > 0;
    }

    /**
     * Computes the emission plus the direct diffuse and specular lighting at a point.
     *
     * @param intersection the hit point
     * @param k            the accumulated attenuation along the ray path
     * @return the local color contribution
     */
    private Color calcLocalEffects(Intersection intersection, Double3 k) {
        Color color = intersection.geometry.getEmission(intersection.point);
        for (LightSource light : _scene.lights) {
            if (!preprocessLightSource(intersection, light)) continue;
            Double3 ktr = transparency(intersection);
            if (ktr.product(k).isGreaterThan(MIN_CALC_COLOR_K)) {
                Color intensity = light.getIntensity(intersection.point).scale(ktr);
                color = color.add(intensity.scale(calcDiffuse(intersection).add(calcSpecular(intersection))));
            }
        }
        return color;
    }

    /**
     * Calculates the Lambertian diffuse factor {@code kD·|l·n|}.
     *
     * @param intersection the hit point, with light data
     * @return the diffuse factor
     */
    private Double3 calcDiffuse(Intersection intersection) {
        return intersection.material.kD.scale(Math.abs(intersection.lNormal));
    }

    /**
     * Calculates the Phong specular factor {@code kS·max(0, -v·r)^n}, where {@code r} is the
     * light direction mirrored about the normal.
     *
     * @param intersection the hit point, with light data
     * @return the specular factor
     */
    private Double3 calcSpecular(Intersection intersection) {
        double ln = intersection.lNormal;
        if (isZero(ln)) return Double3.ZERO;
        Vector r = intersection.l.subtract(intersection.normal.scale(2 * ln));
        double minusVR = alignZero(-intersection.v.dotProduct(r));
        return minusVR <= 0 ? Double3.ZERO
                : intersection.material.kS.scale(Math.pow(minusVR, intersection.material.nShininess));
    }

    /**
     * Calculates how much light passes from the current light source to a point. Every occluder
     * multiplies the light by its transparency, and the search stops as soon as the light is
     * effectively blocked.
     *
     * @param intersection the hit point, with light data
     * @return the transparency factor between the light and the point
     */
    private Double3 transparency(Intersection intersection) {
        Ray shadowRay = new Ray(intersection.point, intersection.l.scale(-1), intersection.normal);
        Transmittance transmittance = new Transmittance();
        _accelerator.traverse(shadowRay, intersection.light.getDistance(intersection.point), transmittance);
        return transmittance._value;
    }

    /**
     * Accumulates the transparency of the occluders met by a shadow ray.
     */
    private static final class Transmittance implements IntersectionVisitor {
        /** The transparency accumulated so far. */
        private Double3 _value = Double3.ONE;

        /** Starts with full transparency. */
        Transmittance() {
        }

        @Override
        public boolean visit(Intersection intersection) {
            _value = _value.product(intersection.material.kT);
            if (!_value.isLowerThan(MIN_CALC_COLOR_K)) return true;
            _value = Double3.ZERO;
            return false;
        }
    }

    /**
     * Computes the reflected and refracted contributions at a point.
     *
     * @param intersection the hit point
     * @param level        the remaining recursion depth
     * @param k            the accumulated attenuation along the ray path
     * @return the global color contribution
     */
    private Color calcGlobalEffects(Intersection intersection, int level, Double3 k) {
        Material material = intersection.material;
        Color color = Color.BLACK;
        if (!material.kR.equals(Double3.ZERO))
            color = color.add(calcGlobalEffect(constructReflectionRay(intersection), intersection, level, k, material.kR));
        if (!material.kT.equals(Double3.ZERO))
            color = color.add(calcGlobalEffect(constructRefractionRay(intersection), intersection, level, k, material.kT));
        return color;
    }

    /**
     * Constructs the mirror reflection of the incoming ray: {@code r = v - 2(v·n)n}.
     *
     * @param intersection the hit point
     * @return the reflected ray
     */
    private Ray constructReflectionRay(Intersection intersection) {
        Vector r = intersection.v.subtract(intersection.normal.scale(2 * intersection.vNormal));
        return new Ray(intersection.point, r, intersection.normal);
    }

    /**
     * Constructs the refracted ray. Refraction indices are not modeled, so the ray continues in
     * its original direction.
     *
     * @param intersection the hit point
     * @return the refracted ray
     */
    private Ray constructRefractionRay(Intersection intersection) {
        return new Ray(intersection.point, intersection.v, intersection.normal);
    }

    /**
     * Computes one global effect (reflection or refraction). A blurry material scatters the ideal
     * ray over a disk - but only at the primary hit, since scattering at every bounce would make
     * the number of rays grow exponentially.
     *
     * @param idealRay     the ideal reflected or refracted ray
     * @param intersection the hit point
     * @param level        the remaining recursion depth
     * @param k            the accumulated attenuation along the ray path
     * @param kx           the attenuation factor of this effect
     * @return the color contribution of the effect
     */
    private Color calcGlobalEffect(Ray idealRay, Intersection intersection, int level, Double3 k, Double3 kx) {
        Material material = intersection.material;
        if (level != MAX_CALC_COLOR_LEVEL || isZero(material.blurRadius))
            return traceSecondaryRay(idealRay, level, k, kx);

        ScatterDisk disk = ScatterDisk.of(idealRay, intersection.normal, material.blurRadius);
        if (_glossyDepth == 0) return sampleRandomBeam(disk, material.blurGlassRays, level, k, kx);

        double size = 2 * disk.radius();
        return AdaptiveSuperSampler.sample(disk.center(), size, size, disk.vX(), disk.vY(), _glossyDepth,
                _glossyTolerance, target -> {
                    Ray ray = disk.rayThrough(target);
                    return ray == null ? Color.BLACK : traceSecondaryRay(ray, level, k, kx);
                });
    }

    /**
     * Averages a beam made of the ideal ray and rays through random points of the scatter disk.
     *
     * @param disk  the scatter disk
     * @param rays  the number of rays in the beam
     * @param level the remaining recursion depth
     * @param k     the accumulated attenuation along the ray path
     * @param kx    the attenuation factor of the effect
     * @return the average color of the beam
     */
    private Color sampleRandomBeam(ScatterDisk disk, int rays, int level, Double3 k, Double3 kx) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Color sum = traceSecondaryRay(disk.idealRay(), level, k, kx);
        int count = 1;
        for (int i = 1; i < rays; i++) {
            // sqrt spreads the points uniformly over the disk area rather than crowding the center
            double r = disk.radius() * Math.sqrt(random.nextDouble());
            double theta = 2 * Math.PI * random.nextDouble();
            Ray ray = disk.rayThrough(disk.pointAt(r * Math.cos(theta), r * Math.sin(theta)));
            if (ray != null) {
                sum = sum.add(traceSecondaryRay(ray, level, k, kx));
                count++;
            }
        }
        return sum.reduce(count);
    }

    /**
     * Traces a secondary ray and returns its contribution.
     *
     * @param ray   the secondary ray
     * @param level the recursion depth of the point the ray leaves from
     * @param k     the accumulated attenuation along the ray path
     * @param kx    the attenuation factor of the effect
     * @return the attenuated color seen along the ray
     */
    private Color traceSecondaryRay(Ray ray, int level, Double3 k, Double3 kx) {
        Double3 kkx = k.product(kx);
        if (kkx.isLowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;

        Intersection intersection = _accelerator.findClosestIntersection(ray);
        if (intersection == null) return _scene.background.scale(kx);
        return preprocessIntersection(intersection, ray.direction())
                ? calcColor(intersection, level - 1, kkx).scale(kx)
                : Color.BLACK;
    }

    /**
     * The disk that the secondary rays of a blurry material are scattered over: centered at unit
     * distance along the ideal ray and orthogonal to it.
     *
     * @param idealRay the ideal secondary ray
     * @param normal   the surface normal at the ray origin
     * @param radius   the radius of the disk
     * @param center   the center of the disk
     * @param vX       the first in-plane axis of the disk
     * @param vY       the second in-plane axis of the disk
     */
    private record ScatterDisk(Ray idealRay, Vector normal, double radius, Point center, Vector vX, Vector vY) {

        /**
         * Creates the scatter disk of an ideal ray.
         *
         * @param idealRay the ideal secondary ray
         * @param normal   the surface normal at the ray origin
         * @param radius   the radius of the disk
         * @return the scatter disk
         */
        static ScatterDisk of(Ray idealRay, Vector normal, double radius) {
            Vector direction = idealRay.direction();
            Vector vX = direction.orthogonal();
            return new ScatterDisk(idealRay, normal, radius, idealRay.origin().add(direction), vX,
                    direction.crossProduct(vX).normalize());
        }

        /**
         * Finds a point of the disk plane.
         *
         * @param x the offset along {@code vX}
         * @param y the offset along {@code vY}
         * @return the point of the disk plane at the offset from the center
         */
        Point pointAt(double x, double y) {
            Point p = center;
            if (!isZero(x)) p = p.add(vX.scale(x));
            if (!isZero(y)) p = p.add(vY.scale(y));
            return p;
        }

        /**
         * Builds the ray from the ideal ray origin through a point of the disk.
         *
         * @param target a point of the disk
         * @return the ray, or {@code null} if it would cross to the other side of the surface
         */
        Ray rayThrough(Point target) {
            Vector direction = target.subtract(idealRay.origin());
            return normal.dotProduct(direction) * normal.dotProduct(idealRay.direction()) > 0
                    ? new Ray(idealRay.origin(), direction)
                    : null;
        }
    }
}
