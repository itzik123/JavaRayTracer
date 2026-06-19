package renderer.postprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * A basic post-processing filter that inverts the colors of the image.
 * Used for testing the post-processing pipeline.
 */
public class ColorInversionFilter implements PostProcessFilter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // יצירת תמונה חדשה כדי לא לפגוע בתמונה המקורית (Immutable behavior)
        BufferedImage result = new BufferedImage(width, height, image.getType());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // שליפת הצבע מהתמונה המקורית
                int rgba = image.getRGB(x, y);
                Color color = new Color(rgba, true);

                // חישוב הצבע ההפוך
                color = new Color(255 - color.getRed(),
                        255 - color.getGreen(),
                        255 - color.getBlue());

                // כתיבה לתמונה החדשה
                result.setRGB(x, y, color.getRGB());
            }
        }
        return result;
    }
}