package primitives;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A 2D image sampled with (u, v) texture coordinates.
 *
 * @author itzik123
 */
public class Texture {
    /** The texture image. */
    private final BufferedImage _image;
    /** Largest valid pixel column. */
    private final int _maxX;
    /** Largest valid pixel row. */
    private final int _maxY;

    /**
     * Loads a texture from an image file.
     *
     * @param filePath the path of a PNG or JPEG image
     * @throws UncheckedIOException if the file cannot be read or is not a supported image
     */
    public Texture(String filePath) {
        try {
            _image = ImageIO.read(new File(filePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read texture " + filePath, e);
        }
        if (_image == null) throw new UncheckedIOException(new IOException("Unsupported image format: " + filePath));
        _maxX = _image.getWidth() - 1;
        _maxY = _image.getHeight() - 1;
    }

    /**
     * Samples the texture with nearest-neighbor filtering. Coordinates outside [0, 1] are
     * clamped; {@code v} grows upwards, as in the OBJ convention.
     *
     * @param u the horizontal texture coordinate
     * @param v the vertical texture coordinate
     * @return the texel color
     */
    public Color getColor(double u, double v) {
        int x = (int) (clamp(u) * _maxX);
        int y = (int) ((1.0 - clamp(v)) * _maxY);
        int rgb = _image.getRGB(x, y);
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    /**
     * Clamps a texture coordinate to the texture.
     *
     * @param value a texture coordinate
     * @return the coordinate clamped to [0, 1]
     */
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
