package renderer;

/**
 * Hands out pixels to rendering threads and reports the rendering progress.
 * <p>
 * Pixels are handed out one at a time, in row-major order, so threads naturally balance the
 * load between cheap and expensive regions of the image.
 *
 * @author Dan Zilberstein
 */
class PixelManager {

    /**
     * A pixel to render.
     *
     * @param row the pixel row
     * @param col the pixel column
     */
    record Pixel(int row, int col) {
    }

    /** Printing format of the progress percentage. */
    private static final String PRINT_FORMAT = "%5.1f%%%n";

    /** The number of pixel rows. */
    private final int _maxRows;
    /** The number of pixel columns. */
    private final int _maxCols;
    /** The total number of pixels. */
    private final long _totalPixels;
    /** The progress printing step, in tenths of a percent (0 disables printing). */
    private final long _printInterval;

    /** Guards the allocation of the next pixel. */
    private final Object _nextLock = new Object();
    /** Guards the progress counters. */
    private final Object _progressLock = new Object();

    /** The row of the last allocated pixel. */
    private int _row = 0;
    /** The column of the last allocated pixel. */
    private int _col = -1;
    /** The number of completed pixels. */
    private long _donePixels = 0;
    /** The last printed progress, in tenths of a percent. */
    private long _lastPrinted = 0;

    /**
     * Creates a pixel manager for an image.
     *
     * @param maxRows       the number of pixel rows
     * @param maxCols       the number of pixel columns
     * @param printInterval the progress printing interval, as a percentage step (0 disables it)
     */
    PixelManager(int maxRows, int maxCols, double printInterval) {
        _maxRows = maxRows;
        _maxCols = maxCols;
        _totalPixels = (long) maxRows * maxCols;
        _printInterval = (long) (printInterval * 10);
        if (_printInterval != 0) System.out.printf(PRINT_FORMAT, 0d);
    }

    /**
     * Allocates the next pixel to render.
     *
     * @return the next pixel, or {@code null} when all pixels have been allocated
     */
    Pixel nextPixel() {
        synchronized (_nextLock) {
            if (_row == _maxRows) return null;
            if (++_col < _maxCols) return new Pixel(_row, _col);
            _col = 0;
            return ++_row < _maxRows ? new Pixel(_row, _col) : null;
        }
    }

    /**
     * Records the completion of a pixel and prints the progress when it crosses the next step.
     */
    void pixelDone() {
        if (_printInterval == 0) return;
        synchronized (_progressLock) {
            long progress = 1000L * ++_donePixels / _totalPixels;
            if (progress - _lastPrinted >= _printInterval) {
                _lastPrinted = progress;
                System.out.printf(PRINT_FORMAT, progress / 10d);
            }
        }
    }
}
