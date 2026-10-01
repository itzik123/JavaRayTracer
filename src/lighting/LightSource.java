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
}
