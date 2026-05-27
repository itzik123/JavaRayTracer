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


    /** Diffuse attenuation factor */
    public Double3 kD = Double3.ZERO;

    /** Specular attenuation factor */
    public Double3 kS = Double3.ZERO;

    /** Shininess factor (power of specular reflection) */
    public int nShininess = 0;

    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    public Material() {
    }

    /**
     * Sets the diffuse attenuation factor (kD).
     * @param kD the diffuse attenuation factor as a Double3 object
     * @return the current Material object
     */
    public Material setKd(Double3 kD) {
        this.kD = kD;
        return this;
    }

    /**
     * Sets the diffuse attenuation factor (kD).
     * @param kD the diffuse attenuation factor as a single double value
     * @return the current Material object
     */
    public Material setKd(double kD) {
        this.kD = new Double3(kD);
        return this;
    }

    /**
     * Sets the specular attenuation factor (kS).
     * @param kS the specular attenuation factor as a Double3 object
     * @return the current Material object
     */
    public Material setKs(Double3 kS) {
        this.kS = kS;
        return this;
    }

    /**
     * Sets the specular attenuation factor (kS).
     * @param kS the specular attenuation factor as a single double value
     * @return the current Material object
     */
    public Material setKs(double kS) {
        this.kS = new Double3(kS);
        return this;
    }

    /**
     * Sets the shininess factor of the material.
     * @param nShininess the shininess factor
     * @return the current Material object
     */
    public Material setShininess(int nShininess) {
        this.nShininess = nShininess;
        return this;
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