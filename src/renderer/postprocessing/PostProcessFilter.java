package renderer.postprocessing;

import java.awt.image.BufferedImage;

/**
 * Interface for all post-processing image filters.
 * Applies a specific algorithm on a 2D image.
 */
public interface PostProcessFilter {

    /**
     * Applies the filter algorithm to the given image.
     * @param image the original image
     * @return a new image with the filter applied
     */
    BufferedImage apply(BufferedImage image);
}