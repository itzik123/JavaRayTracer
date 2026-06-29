package primitives;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Represents a 2D texture image loaded into memory.
 * Provides a method to sample colors using U,V mapping coordinates.
 */
public class Texture {
    private BufferedImage _image;
    private int _width;
    private int _height;

    /**
     * Loads a texture from an image file.
     * @param filePath the path to the image file (e.g., .png or .jpg)
     */
    public Texture(String filePath) {
        try {
            _image = ImageIO.read(new File(filePath));
            if (_image != null) {
                _width = _image.getWidth();
                _height = _image.getHeight();
            }
        } catch (IOException e) {
            System.err.println("Error loading texture: " + filePath);
            e.printStackTrace();
        }
    }

    /**
     * Gets the pixel color at the specified (U, V) coordinates.
     * @param u horizontal coordinate (0.0 to 1.0)
     * @param v vertical coordinate (0.0 to 1.0)
     * @return the Color of the pixel
     */
    public Color getColor(double u, double v) {
        if (_image == null) return Color.BLACK;

        // 1. חיתוך (Clamp)
        u = Math.max(0.0, Math.min(1.0, u));
        v = Math.max(0.0, Math.min(1.0, v));

        // 2. תרגום לפיקסלים (עם היפוך ציר Y)
        int x = (int) (u * (_width - 1));
        int y = (int) ((1.0 - v) * (_height - 1));

        int rgb = _image.getRGB(x, y);

        // 3. חילוץ ערוצי ה-RGB מתוך המספר השלם (32-bit ARGB)
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        // עכשיו הבנאי שלך יקבל את זה בשמחה!
        return new Color(r, g, b);
    }
}