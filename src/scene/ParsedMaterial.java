package scene;

import primitives.Color;
import primitives.Material;
import primitives.Texture;

public class ParsedMaterial {
    public String name;
    public Color diffuseColor = Color.BLACK;
    public Material material = new Material().setKd(0.8).setKs(0.2).setShininess(20);

    // השדה החדש! יחזיק את תמונת ה-PNG אם היא קיימת
    public Texture texture = null;

    public ParsedMaterial(String name) {
        this.name = name;
    }
}