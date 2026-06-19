package renderer.postprocessing;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages and executes a pipeline of post-processing filters.
 */
public class PostProcessingEngine {

    /** List of active filters in the pipeline */
    private final List<PostProcessFilter> _filters = new LinkedList<>();

    /**
     * Adds a filter to the processing pipeline.
     * Uses method chaining.
     * @param filter the filter to add
     * @return this engine
     */
    public PostProcessingEngine addFilter(PostProcessFilter filter) {
        _filters.add(filter);
        return this;
    }

    /**
     * Runs the image through all configured filters sequentially.
     * @param originalImage the image to process
     * @return the fully processed final image
     */
    public BufferedImage process(BufferedImage originalImage) {
        if (_filters.isEmpty()) {
            return originalImage;
        }

        BufferedImage currentImage = originalImage;
        for (PostProcessFilter filter : _filters) {
            currentImage = filter.apply(currentImage);
        }
        return currentImage;
    }
}