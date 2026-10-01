package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import primitives.Color;
import primitives.Point;
import primitives.Texture;
import primitives.UV;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Unit tests for {@link TexturedTriangle}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link TexturedTriangle#getEmission(Point)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 * <p>
 * The texture is split into four solid quadrants, and the triangle maps its right angle to the
 * bottom-left corner of the texture, so every region of the triangle has a known color.
 *
 * @author itzik123
 */
class TexturedTriangleTest {

    /** Default constructor to satisfy JavaDoc generator */
    TexturedTriangleTest() { /* to satisfy JavaDoc generator */ }

    /** The color of the top-left quadrant of the texture */
    private static final Color TOP_LEFT = new Color(255, 0, 0);
    /** The color of the bottom-left quadrant of the texture */
    private static final Color BOTTOM_LEFT = new Color(0, 0, 255);
    /** The color of the bottom-right quadrant of the texture */
    private static final Color BOTTOM_RIGHT = new Color(255, 255, 255);

    /** A temporary directory for the texture image */
    @TempDir
    Path tempDir;

    /** The triangle under test */
    private TexturedTriangle _triangle;

    /**
     * Creates the texture and the triangle (0,0,0)-(1,0,0)-(0,1,0) mapped to UV (0,0)-(1,0)-(0,1).
     *
     * @throws IOException if the texture cannot be written
     */
    @BeforeEach
    void setUp() throws IOException {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 10; x++)
            for (int y = 0; y < 10; y++)
                image.setRGB(x, y, (y < 5 ? TOP_LEFT : x < 5 ? BOTTOM_LEFT : BOTTOM_RIGHT).getColor().getRGB());
        File file = tempDir.resolve("texture.png").toFile();
        ImageIO.write(image, "png", file);

        _triangle = new TexturedTriangle(Point.ZERO, new Point(1, 0, 0), new Point(0, 1, 0),
                new UV(0, 0), new UV(1, 0), new UV(0, 1), new Texture(file.getPath()));
    }

    /**
     * Test method for {@link TexturedTriangle#getEmission(Point)}.
     */
    @Test
    void testGetEmission() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: An interior point near the second vertex (UV (0.8, 0.1) - bottom-right)
        assertEquals(BOTTOM_RIGHT, _triangle.getEmission(new Point(0.8, 0.1, 0)), "Wrong texel near the second vertex");

        // TC02: An interior point near the third vertex (UV (0.1, 0.8) - top-left)
        assertEquals(TOP_LEFT, _triangle.getEmission(new Point(0.1, 0.8, 0)), "Wrong texel near the third vertex");

        // TC03: An interior point near the first vertex (UV (0.1, 0.1) - bottom-left)
        assertEquals(BOTTOM_LEFT, _triangle.getEmission(new Point(0.1, 0.1, 0)), "Wrong texel near the first vertex");

        // =============== Boundary Values Tests ==================

        // TC11: The vertices themselves
        assertEquals(BOTTOM_LEFT, _triangle.getEmission(Point.ZERO), "Wrong texel at the first vertex");
        assertEquals(BOTTOM_RIGHT, _triangle.getEmission(new Point(1, 0, 0)), "Wrong texel at the second vertex");
        assertEquals(TOP_LEFT, _triangle.getEmission(new Point(0, 1, 0)), "Wrong texel at the third vertex");
    }
}
