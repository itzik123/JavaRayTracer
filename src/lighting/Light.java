package lighting;

import primitives.Color;

/**
 * Abstract class representing a general light source.
 * @author itzik123
 */
abstract class Light {

    /** The original intensity of the light */
    protected final Color _intensity;

    /**
     * Protected constructor to initialize the light intensity.
     * @param intensity the color intensity of the light
     */
    protected Light(Color intensity) {
        this._intensity = intensity;
    }

    /**
     * Getter for the light's original intensity.
     * @return the intensity color
     */
    public Color getIntensity() {
        return _intensity;
    }
}