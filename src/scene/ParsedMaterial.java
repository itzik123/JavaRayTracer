package scene;

import primitives.Material;
import primitives.Texture;

/**
 * A material read from an MTL file: its optical properties and optional diffuse texture.
 *
 * @author itzik123
 */
final class ParsedMaterial {

    /** The material name, as referenced by {@code usemtl}. */
    final String name;
    /** The optical properties; defaults suit untextured, slightly glossy surfaces. */
    final Material material = new Material().setKd(0.8).setKs(0.2).setShininess(20);
    /** The diffuse texture ({@code map_Kd}), or {@code null}. */
    Texture texture;

    /**
     * Creates a material with default properties.
     *
     * @param name the material name
     */
    ParsedMaterial(String name) {
        this.name = name;
    }
}
