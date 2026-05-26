package scene;

import geometries.api.Geometry;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import org.w3c.dom.*;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
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

        NodeList ambientNodes = root.getElementsByTagName("ambient-light");
        if (ambientNodes.getLength() > 0) {
            String colorStr = ((Element) ambientNodes.item(0)).getAttribute("color");
            scene.setAmbientLight(new AmbientLight(parseColor(colorStr)));
        }

        NodeList sphereNodes = root.getElementsByTagName("sphere");
        for (int i = 0; i < sphereNodes.getLength(); i++) {
            Element el = (Element) sphereNodes.item(i);
            Sphere sphere = new Sphere(
                    parsePoint(el.getAttribute("center")),
                    Double.parseDouble(el.getAttribute("radius")));
            applyGeometryAttributes(sphere, el);
            scene.geometries.add(sphere);
        }

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
     * Applies common geometry attributes like emission and material (kA).
     */
    private static void applyGeometryAttributes(Geometry geom, Element el) {
        if (el.hasAttribute("emission")) {
            geom.setEmission(parseColor(el.getAttribute("emission")));
        }
        if (el.hasAttribute("kA")) {
            geom.setMaterial(new Material().setKa(parseDouble3(el.getAttribute("kA"))));
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

    private static Double3 parseDouble3(String str) {
        String[] parts = str.split(" ");
        if (parts.length == 1) {
            return new Double3(Double.parseDouble(parts[0]));
        }
        return new Double3(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }
}