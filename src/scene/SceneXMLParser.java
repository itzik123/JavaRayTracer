package scene;

import geometries.api.Geometry;
import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Loads a {@link Scene} from an XML file.
 * <p>
 * The root {@code <scene>} element may carry {@code name} and {@code background-color}
 * attributes. Every descendant element whose tag is a known light or geometry type is added to
 * the scene; container elements such as {@code <geometries>} and unknown tags are ignored.
 * Vectors, points and colors are written as three space-separated numbers:
 *
 * <pre>{@code
 * <scene name="Demo" background-color="0 0 0">
 *     <ambient-light color="20 20 20"/>
 *     <spot-light color="800 500 0" position="-50 -50 25" direction="1 1 -0.5" kL="0.001" kQ="0.0001"/>
 *     <sphere center="0 0 -50" radius="50" emission="0 0 127" kD="0.5" kS="0.5" nShininess="300"/>
 * </scene>
 * }</pre>
 *
 * Geometries accept {@code emission} and the material attributes {@code kA}, {@code kD},
 * {@code kS}, {@code nShininess}, {@code kT}, {@code kR}, {@code blurRadius} and
 * {@code blurGlassRays}; attenuation factors are either one number or three.
 *
 * @author itzik123
 */
public final class SceneXMLParser {

    /** Adds the object described by an element to the scene, by element tag. */
    private static final Map<String, BiConsumer<Element, Scene>> ELEMENT_HANDLERS = Map.of(
            "ambient-light", (el, scene) -> scene.setAmbientLight(new AmbientLight(color(el, "color"))),
            "directional-light", (el, scene) -> scene.lights.add(
                    new DirectionalLight(color(el, "color"), vector(el, "direction"))),
            "point-light", (el, scene) -> scene.lights.add(
                    attenuate(new PointLight(color(el, "color"), point(el, "position")), el)),
            "spot-light", SceneXMLParser::addSpotLight,
            "sphere", (el, scene) -> add(scene, el, new Sphere(point(el, "center"), number(el, "radius"))),
            "triangle", (el, scene) -> add(scene, el,
                    new Triangle(point(el, "p0"), point(el, "p1"), point(el, "p2"))),
            "polygon", (el, scene) -> add(scene, el, new Polygon(polygonVertices(el))),
            "plane", (el, scene) -> add(scene, el, new Plane(point(el, "point"), vector(el, "normal"))),
            "tube", (el, scene) -> add(scene, el, new Tube(number(el, "radius"), axis(el))),
            "cylinder", (el, scene) -> add(scene, el,
                    new Cylinder(number(el, "radius"), axis(el), number(el, "height"))));

    /** Utility class - not instantiable. */
    private SceneXMLParser() {}

    /**
     * Parses a scene file.
     *
     * @param path the path of the XML file
     * @return the scene
     * @throws SceneLoadException if the file cannot be read or describes an invalid scene
     */
    public static Scene parse(String path) {
        try {
            Element root = parseDocument(new File(path)).getDocumentElement();
            Scene scene = new Scene(root.getAttribute("name"));
            if (root.hasAttribute("background-color")) scene.setBackground(color(root, "background-color"));

            NodeList elements = root.getElementsByTagName("*");
            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                var handler = ELEMENT_HANDLERS.get(element.getTagName());
                if (handler != null) handler.accept(element, scene);
            }
            return scene;
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new SceneLoadException("Cannot read scene file " + path, e);
        } catch (IllegalArgumentException e) {
            throw new SceneLoadException("Invalid scene in " + path + ": " + e.getMessage(), e);
        }
    }

    /**
     * Parses an XML file with external entities and DTDs disabled, which protects against XXE
     * attacks when loading untrusted files.
     *
     * @param file the XML file
     * @return the parsed document
     * @throws ParserConfigurationException if the secure parser cannot be configured
     * @throws SAXException                 if the XML is malformed
     * @throws IOException                  if the file cannot be read
     */
    private static Document parseDocument(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder().parse(file);
    }

    /**
     * Adds a spot light, with its optional attenuation and beam focus, to the scene.
     *
     * @param el    the {@code <spot-light>} element
     * @param scene the scene
     */
    private static void addSpotLight(Element el, Scene scene) {
        SpotLight light = new SpotLight(color(el, "color"), point(el, "position"), vector(el, "direction"));
        attenuate(light, el);
        if (el.hasAttribute("narrow-beam")) light.setNarrowBeam(Integer.parseInt(el.getAttribute("narrow-beam").trim()));
        scene.lights.add(light);
    }

    /**
     * Applies the optional attenuation attributes {@code kC}, {@code kL} and {@code kQ}.
     *
     * @param light the light
     * @param el    the light element
     * @return the light
     */
    private static PointLight attenuate(PointLight light, Element el) {
        if (el.hasAttribute("kC")) light.setKc(number(el, "kC"));
        if (el.hasAttribute("kL")) light.setKl(number(el, "kL"));
        if (el.hasAttribute("kQ")) light.setKq(number(el, "kQ"));
        return light;
    }

    /**
     * Applies the emission and material attributes of an element and adds the geometry.
     *
     * @param scene    the scene
     * @param el       the geometry element
     * @param geometry the geometry
     */
    private static void add(Scene scene, Element el, Geometry geometry) {
        if (el.hasAttribute("emission")) geometry.setEmission(color(el, "emission"));

        Material material = new Material();
        boolean hasMaterial = false;
        if (el.hasAttribute("kA")) { material.setKa(double3(el, "kA")); hasMaterial = true; }
        if (el.hasAttribute("kD")) { material.setKd(double3(el, "kD")); hasMaterial = true; }
        if (el.hasAttribute("kS")) { material.setKs(double3(el, "kS")); hasMaterial = true; }
        if (el.hasAttribute("kT")) { material.setKt(double3(el, "kT")); hasMaterial = true; }
        if (el.hasAttribute("kR")) { material.setKr(double3(el, "kR")); hasMaterial = true; }
        if (el.hasAttribute("nShininess")) { material.setShininess((int) number(el, "nShininess")); hasMaterial = true; }
        if (el.hasAttribute("blurRadius")) { material.setBlurRadius(number(el, "blurRadius")); hasMaterial = true; }
        if (el.hasAttribute("blurGlassRays")) { material.setBlurGlassRays((int) number(el, "blurGlassRays")); hasMaterial = true; }
        if (hasMaterial) geometry.setMaterial(material);

        scene.geometries.add(geometry);
    }

    /**
     * Reads the vertices {@code p0, p1, ...} of a polygon element.
     *
     * @param el the polygon element
     * @return the vertices
     */
    private static Point[] polygonVertices(Element el) {
        List<Point> vertices = new ArrayList<>();
        for (int i = 0; el.hasAttribute("p" + i); i++) vertices.add(point(el, "p" + i));
        return vertices.toArray(Point[]::new);
    }

    /**
     * Reads the axis of a tube or cylinder from its {@code axis-origin} and {@code axis-dir}.
     *
     * @param el the element
     * @return the axis ray
     */
    private static Ray axis(Element el) {
        return new Ray(point(el, "axis-origin"), vector(el, "axis-dir"));
    }

    /**
     * Reads a numeric attribute.
     *
     * @param el        an element
     * @param attribute the attribute name
     * @return the attribute as a number
     */
    private static double number(Element el, String attribute) {
        return Double.parseDouble(el.getAttribute(attribute).trim());
    }

    /**
     * Reads an attenuation-factor attribute.
     *
     * @param el        an element
     * @param attribute the attribute name
     * @return the attribute as a triad of numbers; a single number is used for all three
     */
    private static Double3 double3(Element el, String attribute) {
        double[] values = numbers(el, attribute, 1, 3);
        return values.length == 1 ? new Double3(values[0]) : new Double3(values[0], values[1], values[2]);
    }

    /**
     * Reads a point attribute.
     *
     * @param el        an element
     * @param attribute the attribute name
     * @return the attribute as a point
     */
    private static Point point(Element el, String attribute) {
        double[] v = numbers(el, attribute, 3);
        return new Point(v[0], v[1], v[2]);
    }

    /**
     * Reads a vector attribute.
     *
     * @param el        an element
     * @param attribute the attribute name
     * @return the attribute as a vector
     */
    private static Vector vector(Element el, String attribute) {
        double[] v = numbers(el, attribute, 3);
        return new Vector(v[0], v[1], v[2]);
    }

    /**
     * Reads a color attribute.
     *
     * @param el        an element
     * @param attribute the attribute name
     * @return the attribute as an RGB color
     */
    private static Color color(Element el, String attribute) {
        double[] v = numbers(el, attribute, 3);
        return new Color(v[0], v[1], v[2]);
    }

    /**
     * Parses a space-separated list of numbers.
     *
     * @param el            an element
     * @param attribute     the attribute name
     * @param allowedCounts the allowed numbers of values
     * @return the values
     * @throws IllegalArgumentException if the attribute is missing or has a wrong number of values
     */
    private static double[] numbers(Element el, String attribute, int... allowedCounts) {
        String[] parts = el.getAttribute(attribute).trim().split("\\s+");
        for (int count : allowedCounts)
            if (parts.length == count) {
                double[] values = new double[count];
                for (int i = 0; i < count; i++) values[i] = Double.parseDouble(parts[i]);
                return values;
            }
        throw new IllegalArgumentException("<" + el.getTagName() + "> attribute '" + attribute
                + "' has an invalid value: '" + el.getAttribute(attribute) + "'");
    }
}
