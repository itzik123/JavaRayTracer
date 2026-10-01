package renderer;

import primitives.Color;
import renderer.postprocessing.PostProcessingEngine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * An image buffer that pixels are rendered into and that is saved as a PNG file.
 * <p>
 * Images are written to the {@code images} directory under the working directory.
 *
 * @author Dan Zilberstein
 */
final class ImageWriter {

    /** The output directory for rendered images. */
    private static final File OUTPUT_DIRECTORY = new File(System.getProperty("user.dir"), "images");

    /** The pixel buffer. */
    private final BufferedImage _image;

    /**
     * Creates a black image.
     *
     * @param nX the width, in pixels
     * @param nY the height, in pixels
     * @throws IllegalArgumentException if a dimension is not positive
     */
    ImageWriter(int nX, int nY) {
        if (nX <= 0 || nY <= 0) throw new IllegalArgumentException("Image resolution must be positive");
        _image = new BufferedImage(nX, nY, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Sets the color of a pixel. Safe to call from several threads for different pixels.
     *
     * @param xIndex the pixel column
     * @param yIndex the pixel row
     * @param color  the color; channels above 255 are clamped
     */
    void writePixel(int xIndex, int yIndex, Color color) {
        _image.setRGB(xIndex, yIndex, color.getColor().getRGB());
    }

    /**
     * Saves the image as a PNG file.
     *
     * @param fileName the file name, without extension
     * @throws UncheckedIOException if the file cannot be written
     */
    void writeToImage(String fileName) {
        writeToImage(fileName, null);
    }

    /**
     * Post-processes a copy of the image and saves it as a PNG file. The buffer itself is left
     * unchanged, so saving twice does not apply the filters twice.
     *
     * @param fileName       the file name, without extension
     * @param postProcessing the filters to apply, or {@code null}
     * @throws UncheckedIOException if the file cannot be written
     */
    void writeToImage(String fileName, PostProcessingEngine postProcessing) {
        BufferedImage output = postProcessing == null ? _image : postProcessing.process(_image);
        try {
            if (!OUTPUT_DIRECTORY.isDirectory() && !OUTPUT_DIRECTORY.mkdirs())
                throw new IOException("Cannot create directory " + OUTPUT_DIRECTORY);
            ImageIO.write(output, "png", new File(OUTPUT_DIRECTORY, fileName + ".png"));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write image " + fileName, e);
        }
    }
}
