package renderer.postprocessing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * An ordered pipeline of post-processing filters: the output of each filter is the input of the
 * next one.
 *
 * @author itzik123
 */
public class PostProcessingEngine {

    /** The filters, in application order. */
    private final List<PostProcessFilter> _filters;

    /**
     * Creates an empty pipeline.
     */
    public PostProcessingEngine() {
        _filters = new ArrayList<>();
    }

    /**
     * Creates a pipeline from a list of filters.
     *
     * @param filters the filters, in application order
     */
    public PostProcessingEngine(List<PostProcessFilter> filters) {
        _filters = new ArrayList<>(filters);
    }

    /**
     * Appends a filter to the pipeline.
     *
     * @param filter the filter
     * @return this engine
     */
    public PostProcessingEngine addFilter(PostProcessFilter filter) {
        _filters.add(filter);
        return this;
    }

    /**
     * Runs an image through all the filters.
     *
     * @param image the source image, which is left unchanged
     * @return the processed image ({@code image} itself if there are no filters)
     */
    public BufferedImage process(BufferedImage image) {
        BufferedImage current = image;
        for (PostProcessFilter filter : _filters) current = filter.apply(current);
        return current;
    }
}
