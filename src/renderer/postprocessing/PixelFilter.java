package renderer.postprocessing;

import java.awt.image.BufferedImage;

/**
 * Base class of filters that work on the packed RGB pixels of an image (Template Method).
 * <p>
 * Reading the pixels out of the source image and wrapping the result in a new image is the same
 * for every filter, so only the pixel transformation is left to subclasses.
 *
 * @author itzik123
 */
public abstract class PixelFilter implements PostProcessFilter {

    /** Creates a pixel filter. */
    protected PixelFilter() {
    }

    @Override
    public final BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, width, height, process(pixels, width, height), 0, width);
        return result;
    }

    /**
     * Transforms the pixels of an image.
     *
     * @param pixels the source pixels in row-major order, packed as {@code 0xRRGGBB}; must not be
     *               modified
     * @param width  the image width
     * @param height the image height
     * @return the transformed pixels, in the same layout
     */
    protected abstract int[] process(int[] pixels, int width, int height);
}
