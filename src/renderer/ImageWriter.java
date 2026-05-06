package renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import primitives.Color;

/**
 * Writes pixel data to an image file.
 * <p>
 * This class wraps a {@link BufferedImage}, allows setting individual pixel
 * colors, and exports the result as a PNG file.
 * @author Dan Zilberstein
 */
final class ImageWriter {
   /**
    * Output directory for generated image files, relative to the working
    * directory.
    */
   private static final String FOLDER_PATH = System.getProperty("user.dir") + "/images";

   /** Internal image buffer (matrix of pixel colors) */
   private final BufferedImage _image;

   /**
    * Creates an image writer for the given resolution.
    * @param  nX                       the horizontal resolution, in pixels
    * @param  nY                       the vertical resolution, in pixels
    * @throws IllegalArgumentException if {@code nX} or {@code nY} is not positive
    */
   ImageWriter(int nX, int nY) {
      if (nX <= 0 || nY <= 0)
         throw new IllegalArgumentException("Image resolution must be positive");
      _image = new BufferedImage(nX, nY, BufferedImage.TYPE_INT_RGB);
   }

   /**
    * Writes the buffered image to a PNG file in the images directory.
    * @param  fileName              the output file name, without the {@code .png}
    *                               extension
    * @throws IllegalStateException if the image cannot be written
    */
   void writeToImage(String fileName) {
      try {
         File folder = new File(FOLDER_PATH);
         if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Could not create output directory: " + FOLDER_PATH);
         }
         File file = new File(folder, fileName + ".png");
         ImageIO.write(_image, "png", file);
      } catch (IOException e) {
         throw new IllegalStateException("I/O error while writing image to " + FOLDER_PATH, e);
      }
   }

   /**
    * Writes a color to the specified pixel.
    * @param xIndex the pixel x-coordinate
    * @param yIndex the pixel y-coordinate
    * @param color  the color to write
    */
   void writePixel(int xIndex, int yIndex, Color color) { _image.setRGB(xIndex, yIndex, color.getColor().getRGB()); }

}
