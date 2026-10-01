package renderer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import primitives.Color;

import java.io.File;

/**
 * Unit tests for {@link ImageWriter}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link ImageWriter#ImageWriter(int, int)}</li>
 * <li>{@link ImageWriter#writeToImage(String)}</li>
 * </ul>
 *
 * @author itzik123
 */
class ImageWriterTest {

    /** Default constructor to satisfy JavaDoc generator */
    ImageWriterTest() { /* to satisfy JavaDoc generator */ }

    /** The image width */
    private static final int WIDTH = 801;
    /** The image height */
    private static final int HEIGHT = 501;
    /** The grid spacing, in pixels */
    private static final int GRID_INTERVAL = 50;
    /** The grid color */
    private static final Color GRID_COLOR = new Color(100, 50, 50);
    /** The background color */
    private static final Color BACKGROUND_COLOR = new Color(10, 50, 200);

    /**
     * Test method for {@link ImageWriter#ImageWriter(int, int)}.
     */
    @Test
    void testConstructor() {
        // =============== Boundary Values Tests ==================

        // TC11: Zero width
        assertThrows(IllegalArgumentException.class, () -> new ImageWriter(0, 1), "Created an image with zero width");

        // TC12: Zero height
        assertThrows(IllegalArgumentException.class, () -> new ImageWriter(1, 0), "Created an image with zero height");
    }

    /**
     * Test method for {@link ImageWriter#writeToImage(String)}: draws a grid over a solid
     * background and saves it.
     */
    @Test
    void testWriteToImage() {
        ImageWriter imageWriter = new ImageWriter(WIDTH, HEIGHT);
        for (int i = 0; i < HEIGHT; ++i)
            for (int j = 0; j < WIDTH; ++j)
                imageWriter.writePixel(j, i, i % GRID_INTERVAL == 0 || j % GRID_INTERVAL == 0 ? GRID_COLOR : BACKGROUND_COLOR);
        imageWriter.writeToImage("test01");

        // TC01: The image file was created
        assertTrue(new File(System.getProperty("user.dir"), "images/test01.png").isFile(), "The image file was not created");
    }
}
