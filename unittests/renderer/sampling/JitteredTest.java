package renderer.sampling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Unit tests for {@link Jittered}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Jittered#generatePoints(int, double, double)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class JitteredTest {

    /** Default constructor to satisfy JavaDoc generator */
    JitteredTest() { /* to satisfy JavaDoc generator */ }

    /** The sampling pattern under test */
    private static final Jittered JITTERED = new Jittered();

    /**
     * Test method for {@link Jittered#generatePoints(int, double, double)}.
     */
    @Test
    void testGeneratePoints() {
        int resolution = 4;
        double width = 8;
        double height = 2;

        // ============ Equivalence Partitions Tests ==============

        // TC01: One sample in every cell of the grid
        List<Point2D> points = JITTERED.generatePoints(resolution, width, height);
        assertEquals(resolution * resolution, points.size(), "Wrong number of samples");
        boolean[][] covered = new boolean[resolution][resolution];
        for (Point2D p : points) {
            int column = (int) ((p.x() + width / 2) / (width / resolution));
            int row = (int) ((p.y() + height / 2) / (height / resolution));
            assertTrue(column >= 0 && column < resolution && row >= 0 && row < resolution, "A sample is outside the area");
            covered[row][column] = true;
        }
        for (boolean[] row : covered)
            for (boolean cell : row) assertTrue(cell, "A cell of the grid has no sample");

        // =============== Boundary Values Tests ==================

        // TC11: A resolution of 1 gives a single sample inside the area
        Point2D single = JITTERED.generatePoints(1, width, height).getFirst();
        assertTrue(Math.abs(single.x()) <= width / 2 && Math.abs(single.y()) <= height / 2, "The sample is outside the area");
    }
}
