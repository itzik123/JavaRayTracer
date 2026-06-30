package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.*;
import scene.Scene;

import java.util.List;

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
    protected Intersection findClosestIntersection(Ray ray) {
        var intersections = _scene.geometries.calcIntersections(ray);
        return intersections == null ? null : ray.findClosestIntersection(intersections);
    }

    /**
     * Checks whether a point is unshaded by other objects from a specific light source.
     * @param intersection the intersection point containing the geometric data and light source
     * @return true if the point is unshaded, false if it's in shadow
     */
    protected boolean unshaded(Intersection intersection) {
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
    protected Double3 transparency(Intersection intersection) {
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

        // --- התיקון הקריטי למניעת "התפוצצות עץ הקרניים" ---
        // נייצר אלומה גדולה רק בפגיעה הראשונה (כשהרקורסיה מתחילה).
        // בכל פגיעה משנית, נסתפק בקרן אחת כדי למנוע גידול מעריכי של זמן הריצה.
        int reflectionRaysCount = (level == MAX_CALC_COLOR_LEVEL) ? material.blurGlassRays : 1;
        int refractionRaysCount = (level == MAX_CALC_COLOR_LEVEL) ? material.blurGlassRays : 1;

        // --- 1. אפקט ההשתקפות (Reflection / Glossy Surfaces) ---
        Double3 kR = material.kR;
        if (!kR.equals(Double3.ZERO)) {
            Ray reflectionRay = constructReflectionRay(intersection);

            // שימוש ברשת מסתגלת במקום אלומה עיוורת!
            if (_adaptiveAASDepth > 0 && material.blurRadius > 0 && level == MAX_CALC_COLOR_LEVEL) {
                color = color.add(sampleGlossyAdaptive(reflectionRay, intersection.normal, material.blurRadius, level, k, kR));
            } else {
                List<Ray> reflectionRaysBeam = constructRayBeam(
                        reflectionRay, intersection.normal, material.blurRadius, reflectionRaysCount
                );
                Color tempColor = Color.BLACK;
                for (Ray ray : reflectionRaysBeam) {
                    tempColor = tempColor.add(calcGlobalEffect(ray, level, k, kR));
                }
                color = color.add(tempColor.reduce(reflectionRaysBeam.size()));
            }
        }

        // --- 2. אפקט השקיפות (Refraction / Diffused Glass) ---
        Double3 kT = material.kT;
        if (!kT.equals(Double3.ZERO)) {
            Ray refractedRay = constructRefractionRay(intersection);

            // שימוש ברשת מסתגלת גם לזכוכית!
            if (_adaptiveAASDepth > 0 && material.blurRadius > 0 && level == MAX_CALC_COLOR_LEVEL) {
                color = color.add(sampleGlossyAdaptive(refractedRay, intersection.normal, material.blurRadius, level, k, kT));
            } else {
                List<Ray> refractedRaysBeam = constructRayBeam(
                        refractedRay, intersection.normal, material.blurRadius, refractionRaysCount
                );
                Color tempColor = Color.BLACK;
                for (Ray ray : refractedRaysBeam) {
                    tempColor = tempColor.add(calcGlobalEffect(ray, level, k, kT));
                }
                color = color.add(tempColor.reduce(refractedRaysBeam.size()));
            }
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
        Color color = intersection.geometry.getEmission(intersection.point);
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

    /**
     * Constructs a beam of rays around a center ideal ray for glossy/diffused effects.
     * @param centerRay The ideal reflected or refracted ray
     * @param n The normal at the intersection point
     * @param radius The blur radius
     * @param numOfRays Number of rays to generate in the beam
     * @return A list of scattered rays
     */
    private List<Ray> constructRayBeam(Ray centerRay, Vector n, double radius, int numOfRays) {
        if (numOfRays <= 1 || Util.isZero(radius)) {
            return List.of(centerRay); // זכוכית שקופה לחלוטין, אין צורך באלומה
        }

        List<Ray> beam = new java.util.ArrayList<>(numOfRays);
        beam.add(centerRay);

        // שימוש נקי ב-Getters של קרן (נא ודא ששמות המתודות origin ו-direction תואמים למימוש שלך)
        Point p0 = centerRay.origin();
        Vector dir = centerRay.direction();

        // מציאת נקודת המרכז של מעגל הפיזור (במרחק 1 מראשית הקרן)
        // שימוש בפעולת add הקיימת במחלקת Point כדי למנוע שבירת כימוס
        Point centerPoint = p0.add(dir);

        // חישוב שני וקטורים מאונכים לכיוון הקרן
        Vector vX;
        try {
            // במקום לפנות ל-X ישירות (שבירת כימוס), נבדוק את ההיטל (Dot Product) על ציר ה-X
            // אם המכפלה הסקלרית קרובה ל-1, הקרן מקבילה לציר ה-X
            if (Math.abs(dir.dotProduct(Vector.AXIS_X)) > 0.9) {
                vX = dir.crossProduct(Vector.AXIS_Y).normalize();
            } else {
                vX = dir.crossProduct(Vector.AXIS_X).normalize();
            }
        } catch (IllegalArgumentException e) {
            vX = dir.crossProduct(Vector.AXIS_Y).normalize();
        }
        Vector vY = dir.crossProduct(vX).normalize();

        // 2. הגרלת נקודות על עיגול המטרה
        java.util.Random rnd = new java.util.Random();
        double nv = n.dotProduct(dir); // בדיקת הצד שממנו יצאה הקרן המקורית

        for (int i = 1; i < numOfRays; i++) {
            // הגרלת רדיוס וזווית בתוך העיגול
            double r = radius * Math.sqrt(rnd.nextDouble());
            double theta = 2 * Math.PI * rnd.nextDouble();

            double x = r * Math.cos(theta);
            double y = r * Math.sin(theta);

            // תזוזה על המישור - שימוש במתודות add ו-scale הרשמיות
            Point p = centerPoint;
            if (!Util.isZero(x)) p = p.add(vX.scale(x));
            if (!Util.isZero(y)) p = p.add(vY.scale(y));

            // יצירת וקטור כיוון חדש בעזרת מתודת subtract של מחלקת Point
            Vector rayDir = p.subtract(p0);

            // --- סינון קריטי! ---
            // מוודאים שהקרן החדשה לא "נבלעה" פנימה (שומרים על אותו צד של המשטח)
            if (n.dotProduct(rayDir) * nv > 0) {
                beam.add(new Ray(p0, rayDir));
            }
        }
        return beam;
    }

    /**
     * Calculates glossy reflections or diffused glass using Adaptive Super-Sampling.
     */
    private Color sampleGlossyAdaptive(Ray centerRay, Vector n, double radius, int level, Double3 k, Double3 kx) {
        Point p0 = centerRay.origin();
        Vector dir = centerRay.direction();
        Point centerPoint = p0.add(dir);

        Vector vX;
        try {
            if (Math.abs(dir.dotProduct(Vector.AXIS_X)) > 0.9) {
                vX = dir.crossProduct(Vector.AXIS_Y).normalize();
            } else {
                vX = dir.crossProduct(Vector.AXIS_X).normalize();
            }
        } catch (IllegalArgumentException e) {
            vX = dir.crossProduct(Vector.AXIS_Y).normalize();
        }
        Vector vY = dir.crossProduct(vX).normalize();

        double nv = n.dotProduct(dir);

        return AdaptiveSuperSampler.sample(
                centerPoint, radius * 2, radius * 2,
                vX, vY,
                _adaptiveAASDepth, _adaptiveAASThreshold,
                // The Lambda Magic for Glossy!
                p -> {
                    Vector rayDir = p.subtract(p0);
                    // מניעת יריית קרן אל תוך הגוף עצמו:
                    if (n.dotProduct(rayDir) * nv <= 0) return Color.BLACK;
                    return calcGlobalEffect(new Ray(p0, rayDir), level, k, kx);
                }
        );
    }
}