package scene;

import geometries.impl.Geometries;
import lighting.AmbientLight;
import lighting.LightSource;
import primitives.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything that makes up a scene: geometry, lights and background.
 * <p>
 * A passive data structure with public fields and fluent setters for convenient construction.
 *
 * @author itzik123
 */
public class Scene {

    /** The name of the scene. */
    public String name;

    /** The color of rays that hit nothing. */
    public Color background = Color.BLACK;

    /** The ambient light. */
    public AmbientLight ambientLight = AmbientLight.NONE;

    /** The geometry of the scene. */
    public Geometries geometries = new Geometries();

    /** The light sources of the scene. */
    public List<LightSource> lights = new ArrayList<>();

    /**
     * Creates an empty scene.
     *
     * @param name the name of the scene
     */
    public Scene(String name) {
        this.name = name;
    }

    /**
     * Sets the name of the scene.
     *
     * @param name the name of the scene
     * @return this scene
     */
    public Scene setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the color of rays that hit nothing.
     *
     * @param background the color of rays that hit nothing
     * @return this scene
     */
    public Scene setBackground(Color background) {
        this.background = background;
        return this;
    }

    /**
     * Sets the ambient light.
     *
     * @param ambientLight the ambient light
     * @return this scene
     */
    public Scene setAmbientLight(AmbientLight ambientLight) {
        this.ambientLight = ambientLight;
        return this;
    }

    /**
     * Sets the geometry of the scene.
     *
     * @param geometries the geometry of the scene
     * @return this scene
     */
    public Scene setGeometries(Geometries geometries) {
        this.geometries = geometries;
        return this;
    }

    /**
     * Sets the light sources of the scene.
     *
     * @param lights the light sources of the scene
     * @return this scene
     */
    public Scene setLights(List<LightSource> lights) {
        this.lights = lights;
        return this;
    }
}
