package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Interface for external light sources.
 */
public interface LightSource {
    /**
     * Gets the light intensity at a specific point.
     * @param p the illuminated point
     * @return the color intensity at the point
     */
    public Color getIntensity(Point p);

    /**
     * Gets the normalized directional vector from the light source to the illuminated point.
     * @param p the illuminated point
     * @return the normalized direction vector
     */
    public Vector getL(Point p);

    /**
     * Returns the distance from the light source to point p
     * @param p the illuminated point
     * @return the distance
     */
    double getDistance(Point p);
}