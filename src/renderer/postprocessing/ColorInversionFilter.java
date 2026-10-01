package renderer.postprocessing;

/**
 * Inverts every color channel, producing a photographic negative.
 *
 * @author itzik123
 */
public class ColorInversionFilter extends PixelFilter {

    /** Creates a color inversion filter. */
    public ColorInversionFilter() {
    }

    @Override
    protected int[] process(int[] pixels, int width, int height) {
        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) result[i] = ~pixels[i] & 0xFFFFFF;
        return result;
    }
}
