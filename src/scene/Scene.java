package scene;

import geometries.impl.Geometries;
import lighting.AmbientLight;
import primitives.Color;

/**
 * Represents a scene holding all the elements for rendering.
 * Acts as a Passive Data Structure (PDS) where all fields are public.
 * Uses method chaining for convenient initialization.
 *
 * @author itzik123
 */
public class Scene {

    /** The name of the scene. */
    public String name;

    /** The background color of the scene. Default is strictly black. */
    public Color background = Color.BLACK;

    /** The ambient light of the scene. Default is no light (NONE). */
    public AmbientLight ambientLight = AmbientLight.NONE;

    /** The 3D geometric bodies in the scene. Default is an empty collection. */
    public Geometries geometries = new Geometries();

    /**
     * Constructor to initialize a scene with a given name.
     *
     * @param name the name of the scene
     */
    public Scene(String name) {
        this.name = name;
    }

    /**
     * Sets the name of the scene.
     *
     * @param name the new name
     * @return the current Scene object for chaining
     */
    public Scene setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the background color of the scene.
     *
     * @param background the background color
     * @return the current Scene object for chaining
     */
    public Scene setBackground(Color background) {
        this.background = background;
        return this;
    }

    /**
     * Sets the ambient light of the scene.
     *
     * @param ambientLight the ambient light
     * @return the current Scene object for chaining
     */
    public Scene setAmbientLight(AmbientLight ambientLight) {
        this.ambientLight = ambientLight;
        return this;
    }

    /**
     * Sets the geometries of the scene.
     *
     * @param geometries the collection of geometric bodies
     * @return the current Scene object for chaining
     */
    public Scene setGeometries(Geometries geometries) {
        this.geometries = geometries;
        return this;
    }
}