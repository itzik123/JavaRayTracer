package lighting;

import primitives.Color;

/**
 * Represents the ambient light in the scene.
 * This class is immutable.
 * @author itzik123
 */
public class AmbientLight {

    /** The intensity color of the ambient light. */
    private final Color _intensity;

    /** Constant representing no ambient light (pitch black). */
    public static final AmbientLight NONE = new AmbientLight(Color.BLACK);

    /**
     * Constructor to initialize the ambient light with a specific intensity.
     *
     * @param intensity the color intensity of the ambient light
     */
    public AmbientLight(Color intensity) {
        _intensity = intensity;
    }

    /**
     * Gets the intensity color of the ambient light.
     *
     * @return the intensity color
     */
    public Color getIntensity() {
        return _intensity;
    }
}