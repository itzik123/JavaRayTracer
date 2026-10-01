package renderer.sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Jittered (stratified) sampling: the area is divided into a regular grid and one sample is placed
 * at a random position inside every cell. This keeps the even coverage of a grid while trading
 * its regular aliasing patterns for much less noticeable noise.
 *
 * @author itzik123
 */
public class Jittered implements SamplingPattern {

    /** Creates a jittered sampling pattern. */
    public Jittered() {
    }

    @Override
    public List<Point2D> generatePoints(int resolution, double width, double height) {
        List<Point2D> points = new ArrayList<>(resolution * resolution);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double cellWidth = width / resolution;
        double cellHeight = height / resolution;
        double startX = -width / 2;
        double startY = -height / 2;

        for (int i = 0; i < resolution; i++) {
            for (int j = 0; j < resolution; j++) {
                double x = startX + (j + random.nextDouble()) * cellWidth;
                double y = startY + (i + random.nextDouble()) * cellHeight;
                points.add(new Point2D(x, y));
            }
        }
        return points;
    }
}
