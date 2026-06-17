package scene;

import geometries.api.Geometry;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.w3c.dom.*;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Parser for Scene objects from XML files.
 * Implements SRP by separating XML parsing logic from the Scene entity.
 */
public class SceneXMLParser {

    /**
     * Parses an XML file and creates a Scene object.
     * @param path path to the XML file
     * @return a fully initialized Scene object
     * @throws Exception if parsing fails
     */
    public static Scene parse(String path) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(new File(path));
        Element root = doc.getDocumentElement();

        Scene scene = new Scene(root.getAttribute("name"));
        if (root.hasAttribute("background-color"))
            scene.setBackground(parseColor(root.getAttribute("background-color")));

        // Ambient Light
        NodeList ambientNodes = root.getElementsByTagName("ambient-light");
        if (ambientNodes.getLength() > 0) {
            String colorStr = ((Element) ambientNodes.item(0)).getAttribute("color");
            scene.setAmbientLight(new AmbientLight(parseColor(colorStr)));
        }

        // Directional Lights
        NodeList dirLightNodes = root.getElementsByTagName("directional-light");
        for (int i = 0; i < dirLightNodes.getLength(); i++) {
            Element el = (Element) dirLightNodes.item(i);
            scene.lights.add(new DirectionalLight(
                    parseColor(el.getAttribute("color")),
                    parseVector(el.getAttribute("direction"))
            ));
        }

        // Point Lights
        NodeList pointLightNodes = root.getElementsByTagName("point-light");
        for (int i = 0; i < pointLightNodes.getLength(); i++) {
            Element el = (Element) pointLightNodes.item(i);
            PointLight pl = new PointLight(
                    parseColor(el.getAttribute("color")),
                    parsePoint(el.getAttribute("position"))
            );
            if (el.hasAttribute("kC")) pl.setKc(Double.parseDouble(el.getAttribute("kC")));
            if (el.hasAttribute("kL")) pl.setKl(Double.parseDouble(el.getAttribute("kL")));
            if (el.hasAttribute("kQ")) pl.setKq(Double.parseDouble(el.getAttribute("kQ")));
            scene.lights.add(pl);
        }

        // Spot Lights
        NodeList spotLightNodes = root.getElementsByTagName("spot-light");
        for (int i = 0; i < spotLightNodes.getLength(); i++) {
            Element el = (Element) spotLightNodes.item(i);
            SpotLight sl = (SpotLight) new SpotLight(
                    parseColor(el.getAttribute("color")),
                    parsePoint(el.getAttribute("position")),
                    parseVector(el.getAttribute("direction"))
            );
            if (el.hasAttribute("kC")) sl.setKc(Double.parseDouble(el.getAttribute("kC")));
            if (el.hasAttribute("kL")) sl.setKl(Double.parseDouble(el.getAttribute("kL")));
            if (el.hasAttribute("kQ")) sl.setKq(Double.parseDouble(el.getAttribute("kQ")));

            if (el.hasAttribute("narrow-beam")) {
                sl.setNarrowBeam(Integer.parseInt(el.getAttribute("narrow-beam")));
            }

            scene.lights.add(sl);
        }

        // Spheres
        NodeList sphereNodes = root.getElementsByTagName("sphere");
        for (int i = 0; i < sphereNodes.getLength(); i++) {
            Element el = (Element) sphereNodes.item(i);
            Sphere sphere = new Sphere(
                    parsePoint(el.getAttribute("center")),
                    Double.parseDouble(el.getAttribute("radius")));
            applyGeometryAttributes(sphere, el);
            scene.geometries.add(sphere);
        }

        // Triangles
        NodeList triNodes = root.getElementsByTagName("triangle");
        for (int i = 0; i < triNodes.getLength(); i++) {
            Element el = (Element) triNodes.item(i);
            Triangle triangle = new Triangle(
                    parsePoint(el.getAttribute("p0")),
                    parsePoint(el.getAttribute("p1")),
                    parsePoint(el.getAttribute("p2")));
            applyGeometryAttributes(triangle, el);
            scene.geometries.add(triangle);
        }

        return scene;
    }

    /**
     * Applies common geometry attributes like emission and material (kA, kD, kS, nShininess).
     */
    private static void applyGeometryAttributes(Geometry geom, Element el) {
        if (el.hasAttribute("emission")) {
            geom.setEmission(parseColor(el.getAttribute("emission")));
        }

        Material material = new Material();
        boolean hasMaterial = false;

        if (el.hasAttribute("kA")) {
            material.setKa(parseDouble3(el.getAttribute("kA")));
            hasMaterial = true;
        }
        if (el.hasAttribute("kD")) {
            material.setKd(parseDouble3(el.getAttribute("kD")));
            hasMaterial = true;
        }
        if (el.hasAttribute("kS")) {
            material.setKs(parseDouble3(el.getAttribute("kS")));
            hasMaterial = true;
        }
        if (el.hasAttribute("nShininess")) {
            material.setShininess(Integer.parseInt(el.getAttribute("nShininess")));
            hasMaterial = true;
        }
        if (el.hasAttribute("kT")) {
            material.setKt(parseDouble3(el.getAttribute("kT")));
            hasMaterial = true;
        }
        if (el.hasAttribute("kR")) {
            material.setKr(parseDouble3(el.getAttribute("kR")));
            hasMaterial = true;
        }
        if (hasMaterial) {
            geom.setMaterial(material);
        }
    }

    private static Color parseColor(String str) {
        String[] parts = str.split(" ");
        return new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    private static Point parsePoint(String str) {
        String[] parts = str.split(" ");
        return new Point(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }

    private static Vector parseVector(String str) {
        String[] parts = str.split(" ");
        return new Vector(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }

    private static Double3 parseDouble3(String str) {
        String[] parts = str.split(" ");
        if (parts.length == 1) {
            return new Double3(Double.parseDouble(parts[0]));
        }
        return new Double3(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }
}