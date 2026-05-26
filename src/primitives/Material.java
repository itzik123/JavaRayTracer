package primitives;

/**
 * PDS (Passive Data Structure) class representing the material of a geometry.
 * Contains attenuation factors for different light interactions.
 */
public class Material {

    /**
     * Ambient light attenuation factor.
     * Initialized to 1 (Double3.ONE) by default so existing tests don't break.
     */
    public Double3 kA = Double3.ONE;

    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    public Material() {
    }

    /**
     * Sets the ambient attenuation factor (kA).
     * @param kA the ambient attenuation factor as a Double3 object
     * @return the current Material object for method chaining
     */
    public Material setKa(Double3 kA) {
        this.kA = kA;
        return this;
    }

    /**
     * Sets the ambient attenuation factor (kA).
     * @param kA the ambient attenuation factor as a single double value
     * @return the current Material object for method chaining
     */
    public Material setKa(double kA) {
        this.kA = new Double3(kA);
        return this;
    }
}