package primitives;

/**
 * Optical properties of a surface for the Phong reflection model and for recursive ray tracing.
 * <p>
 * A passive data structure: the fields are public for fast access from the ray tracer, and the
 * fluent setters exist for convenient scene construction. All attenuation factors are per RGB
 * channel.
 *
 * @author itzik123
 */
public class Material {

    /** Ambient attenuation factor. Defaults to 1 so ambient light shows unless disabled. */
    public Double3 kA = Double3.ONE;

    /** Diffuse attenuation factor. */
    public Double3 kD = Double3.ZERO;

    /** Specular attenuation factor. */
    public Double3 kS = Double3.ZERO;

    /** Phong shininess exponent - larger values give smaller, sharper highlights. */
    public int nShininess = 0;

    /** Transparency (refraction) attenuation factor. */
    public Double3 kT = Double3.ZERO;

    /** Reflection attenuation factor. */
    public Double3 kR = Double3.ZERO;

    /**
     * Radius of the disk that secondary rays are scattered over, at unit distance from the
     * surface. Zero gives perfect mirrors and clear glass; larger values give glossy reflections
     * and frosted glass.
     */
    public double blurRadius = 0.0;

    /** Number of rays in the scattered beam when {@link #blurRadius} is positive. */
    public int blurGlassRays = 1;

    /**
     * Index of refraction of a transparent solid. At 1, rays pass straight through; above it,
     * they bend by Snell's law and part of the light is reflected (Fresnel), as in real glass.
     * Meant for closed shapes, whose rays both enter and leave.
     */
    public double ior = 1;

    /** Creates a material with no diffuse, specular, reflective or transparent response. */
    public Material() {
        // all fields have defaults
    }

    /**
     * Sets the ambient attenuation factor per channel.
     *
     * @param kA the ambient attenuation factor per channel
     * @return this material
     */
    public Material setKa(Double3 kA) {
        this.kA = kA;
        return this;
    }

    /**
     * Sets the ambient attenuation factor for all channels.
     *
     * @param kA the ambient attenuation factor for all channels
     * @return this material
     */
    public Material setKa(double kA) {
        this.kA = new Double3(kA);
        return this;
    }

    /**
     * Sets the diffuse attenuation factor per channel.
     *
     * @param kD the diffuse attenuation factor per channel
     * @return this material
     */
    public Material setKd(Double3 kD) {
        this.kD = kD;
        return this;
    }

    /**
     * Sets the diffuse attenuation factor for all channels.
     *
     * @param kD the diffuse attenuation factor for all channels
     * @return this material
     */
    public Material setKd(double kD) {
        this.kD = new Double3(kD);
        return this;
    }

    /**
     * Sets the specular attenuation factor per channel.
     *
     * @param kS the specular attenuation factor per channel
     * @return this material
     */
    public Material setKs(Double3 kS) {
        this.kS = kS;
        return this;
    }

    /**
     * Sets the specular attenuation factor for all channels.
     *
     * @param kS the specular attenuation factor for all channels
     * @return this material
     */
    public Material setKs(double kS) {
        this.kS = new Double3(kS);
        return this;
    }

    /**
     * Sets the Phong shininess exponent.
     *
     * @param nShininess the Phong shininess exponent
     * @return this material
     */
    public Material setShininess(int nShininess) {
        this.nShininess = nShininess;
        return this;
    }

    /**
     * Sets the transparency attenuation factor per channel.
     *
     * @param kT the transparency attenuation factor per channel
     * @return this material
     */
    public Material setKt(Double3 kT) {
        this.kT = kT;
        return this;
    }

    /**
     * Sets the transparency attenuation factor for all channels.
     *
     * @param kT the transparency attenuation factor for all channels
     * @return this material
     */
    public Material setKt(double kT) {
        this.kT = new Double3(kT);
        return this;
    }

    /**
     * Sets the reflection attenuation factor per channel.
     *
     * @param kR the reflection attenuation factor per channel
     * @return this material
     */
    public Material setKr(Double3 kR) {
        this.kR = kR;
        return this;
    }

    /**
     * Sets the reflection attenuation factor for all channels.
     *
     * @param kR the reflection attenuation factor for all channels
     * @return this material
     */
    public Material setKr(double kR) {
        this.kR = new Double3(kR);
        return this;
    }

    /**
     * Sets the scattering radius of secondary rays (0 for sharp effects).
     *
     * @param blurRadius the scattering radius of secondary rays (0 for sharp effects)
     * @return this material
     * @throws IllegalArgumentException if the radius is negative
     */
    public Material setBlurRadius(double blurRadius) {
        if (blurRadius < 0) throw new IllegalArgumentException("Blur radius must be non-negative");
        this.blurRadius = blurRadius;
        return this;
    }

    /**
     * Sets the number of rays in a scattered beam.
     *
     * @param blurGlassRays the number of rays in a scattered beam
     * @return this material
     * @throws IllegalArgumentException if the count is not positive
     */
    public Material setBlurGlassRays(int blurGlassRays) {
        if (blurGlassRays < 1) throw new IllegalArgumentException("Beam must contain at least one ray");
        this.blurGlassRays = blurGlassRays;
        return this;
    }

    /**
     * Sets the index of refraction (1 for air, about 1.5 for glass, 2.4 for diamond).
     *
     * @param ior the index of refraction
     * @return this material
     * @throws IllegalArgumentException if the index is below 1
     */
    public Material setIor(double ior) {
        if (ior < 1) throw new IllegalArgumentException("Index of refraction must be at least 1");
        this.ior = ior;
        return this;
    }
}
