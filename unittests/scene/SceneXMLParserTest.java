package scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import geometries.api.Geometry;
import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import primitives.Color;
import primitives.Double3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit tests for {@link SceneXMLParser}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link SceneXMLParser#parse(String)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class SceneXMLParserTest {

    /** Default constructor to satisfy JavaDoc generator */
    SceneXMLParserTest() { /* to satisfy JavaDoc generator */ }

    /** A scene using every supported element, with nested containers */
    private static final String FULL_SCENE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <scene name="Full" background-color="10 20 30">
                <ambient-light color="5 5 5"/>
                <lights>
                    <directional-light color="100 100 100" direction="0 -1 0"/>
                    <point-light color="200 0 0" position="0 10 0" kL="0.01" kQ="0.001"/>
                    <spot-light color="0 200 0" position="0 10 0" direction="0 -1 0" narrow-beam="4"/>
                </lights>
                <geometries>
                    <sphere center="0 0 -50" radius="10" emission="0 0 255" kD="0.5" kS="0.2 0.3 0.4" nShininess="50"/>
                    <triangle p0="0 0 0" p1="1 0 0" p2="0 1 0"/>
                    <plane point="0 -20 0" normal="0 1 0" kR="0.5" blurRadius="0.1" blurGlassRays="8"/>
                    <tube axis-origin="0 0 0" axis-dir="1 0 0" radius="2"/>
                    <cylinder axis-origin="0 0 0" axis-dir="0 1 0" radius="1" height="5" kT="0.7"/>
                    <polygon p0="0 0 0" p1="2 0 0" p2="2 2 0" p3="0 2 0"/>
                </geometries>
            </scene>
            """;

    /** A temporary directory for the scene files */
    @TempDir
    Path tempDir;

    /**
     * Writes a scene file.
     *
     * @param content the XML content
     * @return the path of the file
     * @throws IOException if the file cannot be written
     */
    private String write(String content) throws IOException {
        Path file = tempDir.resolve("scene.xml");
        Files.writeString(file, content);
        return file.toString();
    }

    /**
     * Test method for {@link SceneXMLParser#parse(String)}.
     *
     * @throws IOException if a scene file cannot be written
     */
    @Test
    void testParse() throws IOException {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A scene with every supported element
        Scene scene = SceneXMLParser.parse(write(FULL_SCENE));
        assertEquals("Full", scene.name, "Wrong scene name");
        assertEquals(new Color(10, 20, 30), scene.background, "Wrong background color");
        assertEquals(new Color(5, 5, 5), scene.ambientLight.getIntensity(), "Wrong ambient light");

        assertEquals(3, scene.lights.size(), "Wrong number of lights");
        assertInstanceOf(DirectionalLight.class, scene.lights.get(0), "Wrong type of the first light");
        assertInstanceOf(PointLight.class, scene.lights.get(1), "Wrong type of the second light");
        assertInstanceOf(SpotLight.class, scene.lights.get(2), "Wrong type of the third light");

        var geometries = scene.geometries.flatten();
        assertEquals(6, geometries.size(), "Wrong number of geometries");
        assertInstanceOf(Sphere.class, geometries.get(0), "Wrong type of the first geometry");
        assertInstanceOf(Triangle.class, geometries.get(1), "Wrong type of the second geometry");
        assertInstanceOf(Plane.class, geometries.get(2), "Wrong type of the third geometry");
        assertInstanceOf(Tube.class, geometries.get(3), "Wrong type of the fourth geometry");
        assertInstanceOf(Cylinder.class, geometries.get(4), "Wrong type of the fifth geometry");
        assertInstanceOf(Polygon.class, geometries.get(5), "Wrong type of the sixth geometry");

        // TC02: Material attributes with one value and with three values
        Geometry sphere = (Geometry) geometries.get(0);
        assertEquals(new Color(0, 0, 255), sphere.getEmission(null), "Wrong emission");
        assertEquals(new Double3(0.5), sphere.getMaterial().kD, "Wrong single-value attenuation factor");
        assertEquals(new Double3(0.2, 0.3, 0.4), sphere.getMaterial().kS, "Wrong three-value attenuation factor");
        assertEquals(50, sphere.getMaterial().nShininess, "Wrong shininess");
        assertEquals(8, ((Geometry) geometries.get(2)).getMaterial().blurGlassRays, "Wrong blur ray count");

        // =============== Boundary Values Tests ==================

        // TC11: An empty scene
        Scene empty = SceneXMLParser.parse(write("<scene/>"));
        assertEquals(0, empty.geometries.flatten().size(), "An empty scene has geometries");
        assertEquals(Color.BLACK, empty.background, "An empty scene has a background color");

        // TC12: A missing file
        assertThrows(SceneLoadException.class, () -> SceneXMLParser.parse(tempDir.resolve("missing.xml").toString()),
                "A missing file did not throw");

        // TC13: A malformed number
        String malformed = write("<scene><sphere center=\"0 0 x\" radius=\"1\"/></scene>");
        assertThrows(SceneLoadException.class, () -> SceneXMLParser.parse(malformed), "A malformed number did not throw");

        // TC14: A vector with a wrong number of components
        String shortVector = write("<scene><sphere center=\"0 0\" radius=\"1\"/></scene>");
        assertThrows(SceneLoadException.class, () -> SceneXMLParser.parse(shortVector), "A short vector did not throw");

        // TC15: A document type declaration, which could smuggle external entities (XXE)
        String doctype = write("""
                <?xml version="1.0"?>
                <!DOCTYPE scene [<!ENTITY secret SYSTEM "file:///etc/passwd">]>
                <scene name="&secret;"/>
                """);
        assertThrows(SceneLoadException.class, () -> SceneXMLParser.parse(doctype), "A DOCTYPE was accepted");
    }
}
