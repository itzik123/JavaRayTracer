package renderer.postprocessing;

import java.awt.image.BufferedImage;
import java.util.function.IntBinaryOperator;

/**
 * Builds small images for the post-processing tests.
 *
 * @author itzik123
 */
final class TestImages {

    /** Utility class - not instantiable. */
    private TestImages() {}

    /**
     * Creates an image from a pixel function.
     *
     * @param width  the image width
     * @param height the image height
     * @param pixel  the packed RGB color of each pixel, by column and row
     * @return the image
     */
    static BufferedImage image(int width, int height, IntBinaryOperator pixel) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) image.setRGB(x, y, pixel.applyAsInt(x, y));
        return image;
    }

    /**
     * Creates an image of one color.
     *
     * @param width  the image width
     * @param height the image height
     * @param rgb    the packed RGB color
     * @return the image
     */
    static BufferedImage uniform(int width, int height, int rgb) {
        return image(width, height, (x, y) -> rgb);
    }

    /**
     * Creates a gray level pixel.
     *
     * @param level the gray level, 0-255
     * @return the packed RGB color
     */
    static int gray(int level) {
        return level << 16 | level << 8 | level;
    }

    /**
     * Reads the color of a pixel.
     *
     * @param image an image
     * @param x     a column
     * @param y     a row
     * @return the packed RGB color of the pixel, without alpha
     */
    static int rgbAt(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) & 0xFFFFFF;
    }

    /**
     * Checks whether two images have the same pixels.
     *
     * @param a the first image
     * @param b the second image
     * @return {@code true} if the images have the same size and pixels
     */
    static boolean samePixels(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) return false;
        for (int y = 0; y < a.getHeight(); y++)
            for (int x = 0; x < a.getWidth(); x++)
                if (rgbAt(a, x, y) != rgbAt(b, x, y)) return false;
        return true;
    }
}
