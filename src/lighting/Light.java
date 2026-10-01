package lighting;

import primitives.Color;

/**
 * Base class of all lights, holding the emitted intensity.
 *
 * @author itzik123
 */
abstract class Light {

    /** The intensity emitted by the light. */
    protected final Color _intensity;

    /**
     * Creates a light.
     *
     * @param intensity the intensity emitted by the light
     */
    protected Light(Color intensity) {
        _intensity = intensity;
    }

    /**
     * Returns the intensity emitted by the light.
     *
     * @return the intensity emitted by the light
     */
    public Color getIntensity() {
        return _intensity;
    }
}
