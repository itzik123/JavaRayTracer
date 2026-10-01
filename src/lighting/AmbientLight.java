package lighting;

import primitives.Color;

/**
 * Uniform background light that reaches every surface regardless of position and direction.
 *
 * @author itzik123
 */
public class AmbientLight extends Light {

    /** No ambient light at all. */
    public static final AmbientLight NONE = new AmbientLight(Color.BLACK);

    /**
     * Creates an ambient light.
     *
     * @param intensity the intensity of the ambient light
     */
    public AmbientLight(Color intensity) {
        super(intensity);
    }
}
