package primitives;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Jittered sampling pattern: divides the area into a grid and places one
 * random sample point within each cell.
 */
public class Jittered implements SamplingPattern {

    private static final Random RANDOM = new Random();

    @Override
    public List<Point2D> generatePoints(int resolution, double width, double height) {
        List<Point2D> points = new LinkedList<>();

        double cellWidth = width / resolution;
        double cellHeight = height / resolution;
        double startX = -width / 2;
        double startY = -height / 2;

        for (int i = 0; i < resolution; i++) {
            for (int j = 0; j < resolution; j++) {
                double cellX = startX + j * cellWidth;
                double cellY = startY + i * cellHeight;

                double jitterX = cellX + RANDOM.nextDouble() * cellWidth;
                double jitterY = cellY + RANDOM.nextDouble() * cellHeight;

                points.add(new Point2D(jitterX, jitterY));
            }
        }
        return points;
    }
}