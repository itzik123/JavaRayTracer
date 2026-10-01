package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * A light that illuminates the scene from a direction and casts shadows.
 *
 * @author itzik123
 */
public interface LightSource {

    /**
     * Calculates the intensity that reaches a point, after attenuation.
     *
     * @param p the illuminated point
     * @return the intensity at the point
     */
    Color getIntensity(Point p);

    /**
     * Calculates the unit direction of the light arriving at a point.
     *
     * @param p the illuminated point
     * @return the unit vector from the light towards the point
     */
    Vector getL(Point p);

    /**
     * Calculates the distance between the light and a point; shadow rays look for occluders only
     * within this distance.
     *
     * @param p the illuminated point
     * @return the distance, or positive infinity for lights at infinity
     */
    double getDistance(Point p);

    /**
     * Returns the radius of the light's emitting disk. A light with a radius casts soft shadows,
     * whose penumbra widens with the radius.
     *
     * @return the radius of the light, or 0 for an ideal light with hard shadows
     */
    default double getRadius() {
        return 0;
    }

    /**
     * Returns the number of shadow rays sampled over the light's disk at a directly visible
     * point. Points seen in reflections and refractions sample a single random ray.
     *
     * @return the number of shadow rays
     */
    default int getShadowRays() {
        return 1;
    }
}
