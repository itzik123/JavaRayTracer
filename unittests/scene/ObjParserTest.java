package scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import geometries.api.Geometry;
import geometries.api.Intersectable;
import geometries.impl.TexturedTriangle;
import geometries.impl.Triangle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import primitives.AABB;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Unit tests for {@link ObjParser}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link ObjParser#parse(String)}</li>
 * <li>{@link ObjParser#setScale(double)} and {@link ObjParser#setOffset(Vector)}</li>
 * <li>{@link ObjParser#setPlanarUvMaterials(String...)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class ObjParserTest {

    /** Default constructor to satisfy JavaDoc generator */
    ObjParserTest() { /* to satisfy JavaDoc generator */ }

    /** A unit square in the XY plane, as one quad face */
    private static final String SQUARE = """
            # unit square
            v 0 0 0
            v 1 0 0
            v 1 1 0
            v 0 1 0
            f 1 2 3 4
            """;

    /** A temporary directory for the model files */
    @TempDir
    Path tempDir;

    /**
     * Writes a file into the temporary directory.
     *
     * @param name    the file name
     * @param content the file content
     * @return the path of the file
     * @throws IOException if the file cannot be written
     */
    private String write(String name, String content) throws IOException {
        Path file = tempDir.resolve(name);
        Files.writeString(file, content);
        return file.toString();
    }

    /**
     * Test method for {@link ObjParser#parse(String)}.
     *
     * @throws IOException if a model file cannot be written
     */
    @Test
    void testParse() throws IOException {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A quad is triangulated into two triangles with the default material
        Material material = new Material().setKd(0.3);
        List<Intersectable> square = new ObjParser().setMaterial(material).setEmission(new Color(1, 2, 3))
                .parse(write("square.obj", SQUARE)).flatten();
        assertEquals(2, square.size(), "Wrong number of triangles");
        assertSame(material, ((Geometry) square.getFirst()).getMaterial(), "The default material was not used");
        assertEquals(new Color(1, 2, 3), ((Geometry) square.getFirst()).getEmission(null), "The default emission was not used");

        // TC02: Faces with texture and normal indices, and relative (negative) indices
        String indexed = write("indexed.obj", """
                v 0 0 0
                v 1 0 0
                v 0 1 0
                vt 0 0
                vt 1 0
                vt 0 1
                f 1/1/1 2/2/1 3/3/1
                f -3//1 -2//1 -1//1
                """);
        assertEquals(2, new ObjParser().parse(indexed).flatten().size(), "Indexed faces were not read");

        // TC03: Materials and textures from an MTL library
        writeTexture("wood.png");
        write("materials.mtl", """
                newmtl wood
                Kd 0.1 0.2 0.3
                Ks 0.5
                Ns 40
                map_Kd -s 1 1 wood.png
                newmtl plain
                Kd 0.9 0.9 0.9
                """);
        String textured = write("textured.obj", """
                mtllib materials.mtl
                v 0 0 0
                v 1 0 0
                v 0 1 0
                vt 0 0
                vt 1 0
                vt 0 1
                usemtl wood
                f 1/1 2/2 3/3
                usemtl plain
                f 1 2 3
                """);
        List<Intersectable> model = new ObjParser().parse(textured).flatten();
        assertInstanceOf(TexturedTriangle.class, model.get(0), "A face with a textured material has no texture");
        assertEquals(new Double3(0.1, 0.2, 0.3), ((Geometry) model.get(0)).getMaterial().kD, "Wrong diffuse factor");
        assertEquals(40, ((Geometry) model.get(0)).getMaterial().nShininess, "Wrong shininess");
        assertInstanceOf(Triangle.class, model.get(1), "A face without texture is missing");
        assertEquals(Color.BLACK, ((Geometry) model.get(1)).getEmission(null), "An MTL material emits light");

        // =============== Boundary Values Tests ==================

        // TC11: Degenerate faces are skipped
        String degenerate = write("degenerate.obj", SQUARE + "v 2 0 0\nf 1 2 5\n");
        assertEquals(2, new ObjParser().parse(degenerate).flatten().size(), "A degenerate face was not skipped");

        // TC12: A missing file
        assertThrows(SceneLoadException.class, () -> new ObjParser().parse(tempDir.resolve("missing.obj").toString()),
                "A missing file did not throw");

        // TC13: A face referring to a vertex that does not exist
        String broken = write("broken.obj", "v 0 0 0\nv 1 0 0\nf 1 2 3\n");
        assertThrows(SceneLoadException.class, () -> new ObjParser().parse(broken), "An invalid index did not throw");

        // TC14: A missing material library keeps the default material
        String noLibrary = write("nolib.obj", "mtllib missing.mtl\n" + SQUARE);
        assertEquals(2, new ObjParser().parse(noLibrary).flatten().size(), "A missing material library aborted loading");
    }

    /**
     * Test method for {@link ObjParser#setScale(double)} and {@link ObjParser#setOffset(Vector)}.
     *
     * @throws IOException if the model file cannot be written
     */
    @Test
    void testTransform() throws IOException {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The vertices are scaled, then translated
        var model = new ObjParser().setScale(2).setOffset(new Vector(10, 0, 0)).parse(write("square.obj", SQUARE));
        assertEquals(new AABB(new Point(10, 0, 0), new Point(12, 2, 0)), model.getBoundingBox(), "Wrong transformed model");
    }

    /**
     * Test method for {@link ObjParser#setPlanarUvMaterials(String...)}.
     *
     * @throws IOException if a model file cannot be written
     */
    @Test
    void testPlanarUvMaterials() throws IOException {
        writeTexture("screen.png");
        write("screen.mtl", "newmtl Display\nmap_Kd screen.png\n");
        String screen = write("screen.obj", "mtllib screen.mtl\nusemtl Display\n" + SQUARE);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Faces of a planar-mapped material get a texture without texture coordinates
        List<Intersectable> mapped = new ObjParser().setPlanarUvMaterials("Display").parse(screen).flatten();
        assertEquals(2, mapped.size(), "Wrong number of triangles");
        assertInstanceOf(TexturedTriangle.class, mapped.getFirst(), "A planar-mapped face has no texture");

        // =============== Boundary Values Tests ==================

        // TC11: Without planar mapping, faces without texture coordinates are untextured
        List<Intersectable> unmapped = new ObjParser().parse(screen).flatten();
        assertEquals(Triangle.class, unmapped.getFirst().getClass(), "A face without coordinates was textured");
    }

    /**
     * Writes a small texture image into the temporary directory.
     *
     * @param name the file name
     * @throws IOException if the image cannot be written
     */
    private void writeTexture(String name) throws IOException {
        ImageIO.write(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB), "png", tempDir.resolve(name).toFile());
    }
}
