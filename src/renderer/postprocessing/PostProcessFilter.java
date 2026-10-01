package renderer.postprocessing;

import java.awt.image.BufferedImage;

/**
 * A 2D image filter applied to the rendered image before it is saved.
 *
 * @author itzik123
 */
@FunctionalInterface
public interface PostProcessFilter {

    /**
     * Applies the filter.
     *
     * @param image the source image, which is left unchanged
     * @return a new, filtered image
     */
    BufferedImage apply(BufferedImage image);
}
