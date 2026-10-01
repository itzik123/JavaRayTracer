package primitives;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * Unit tests for {@link Texture}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Texture#Texture(String)}</li>
 * <li>{@link Texture#getColor(double, double)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class TextureTest {

    /** Default constructor to satisfy JavaDoc generator */
    TextureTest() { /* to satisfy JavaDoc generator */ }

    /** The color of the top-left quadrant of the test image */
    private static final Color TOP_LEFT = new Color(255, 0, 0);
    /** The color of the top-right quadrant of the test image */
    private static final Color TOP_RIGHT = new Color(0, 255, 0);
    /** The color of the bottom-left quadrant of the test image */
    private static final Color BOTTOM_LEFT = new Color(0, 0, 255);
    /** The color of the bottom-right quadrant of the test image */
    private static final Color BOTTOM_RIGHT = new Color(255, 255, 255);

    /** A temporary directory for the test image */
    @TempDir
    Path tempDir;

    /**
     * Writes a 10x10 image made of four solid-color quadrants.
     *
     * @param directory the directory to write to
     * @return the path of the image
     * @throws IOException if the image cannot be written
     */
    private static String writeQuadrantImage(Path directory) throws IOException {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 10; x++)
            for (int y = 0; y < 10; y++) {
                Color color = y < 5 ? (x < 5 ? TOP_LEFT : TOP_RIGHT) : (x < 5 ? BOTTOM_LEFT : BOTTOM_RIGHT);
                image.setRGB(x, y, color.getColor().getRGB());
            }
        File file = directory.resolve("quadrants.png").toFile();
        ImageIO.write(image, "png", file);
        return file.getPath();
    }

    /**
     * Test method for {@link Texture#Texture(String)}.
     */
    @Test
    void testConstructor() {
        // =============== Boundary Values Tests ==================

        // TC11: A missing file
        assertThrows(UncheckedIOException.class, () -> new Texture(tempDir.resolve("missing.png").toString()),
                "Loading a missing texture did not throw");
    }

    /**
     * Test method for {@link Texture#getColor(double, double)}.
     *
     * @throws IOException if the test image cannot be written
     */
    @Test
    void testGetColor() throws IOException {
        Texture texture = new Texture(writeQuadrantImage(tempDir));

        // ============ Equivalence Partitions Tests ==============

        // TC01: A point in each quadrant; v grows upwards
        assertEquals(TOP_LEFT, texture.getColor(0.25, 0.75), "Wrong texel in the top-left quadrant");
        assertEquals(TOP_RIGHT, texture.getColor(0.75, 0.75), "Wrong texel in the top-right quadrant");
        assertEquals(BOTTOM_LEFT, texture.getColor(0.25, 0.25), "Wrong texel in the bottom-left quadrant");
        assertEquals(BOTTOM_RIGHT, texture.getColor(0.75, 0.25), "Wrong texel in the bottom-right quadrant");

        // =============== Boundary Values Tests ==================

        // TC11: The corners of the texture
        assertEquals(BOTTOM_LEFT, texture.getColor(0, 0), "Wrong texel at (0,0)");
        assertEquals(TOP_RIGHT, texture.getColor(1, 1), "Wrong texel at (1,1)");

        // TC12: Coordinates outside [0,1] are clamped
        assertEquals(TOP_RIGHT, texture.getColor(5, 5), "Coordinates above 1 were not clamped");
        assertEquals(BOTTOM_LEFT, texture.getColor(-5, -5), "Coordinates below 0 were not clamped");
    }
}
