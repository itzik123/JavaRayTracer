package primitives;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Unit tests for {@link AABB}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link AABB#findIntersectionT(Ray)}</li>
 * <li>{@link AABB#isOverlapping(AABB)}</li>
 * <li>{@link AABB#union(AABB)}</li>
 * <li>{@link AABB#enclosing(java.util.Collection)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class AABBTest {

    /** Default constructor to satisfy JavaDoc generator */
    AABBTest() { /* to satisfy JavaDoc generator */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** The unit box [0,1]³ */
    private static final AABB BOX = new AABB(Point.ZERO, new Point(1, 1, 1));

    /**
     * Test method for {@link AABB#findIntersectionT(Ray)}.
     */
    @Test
    void testFindIntersectionT() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray starts outside and crosses the box
        assertEquals(1, BOX.findIntersectionT(new Ray(new Point(-1, 0.5, 0.5), Vector.AXIS_X)), DELTA,
                "Wrong entry distance for a crossing ray");

        // TC02: Ray starts inside the box
        assertEquals(0, BOX.findIntersectionT(new Ray(new Point(0.5, 0.5, 0.5), Vector.AXIS_X)), DELTA,
                "Entry distance of a ray starting inside is not zero");

        // TC03: Ray misses the box
        assertEquals(AABB.NO_HIT, BOX.findIntersectionT(new Ray(new Point(-1, 2, 0.5), Vector.AXIS_X)),
                "A missing ray was reported as a hit");

        // TC04: The box is behind the ray
        assertEquals(AABB.NO_HIT, BOX.findIntersectionT(new Ray(new Point(2, 0.5, 0.5), Vector.AXIS_X)),
                "A box behind the ray was reported as a hit");

        // TC05: Diagonal ray crosses the box
        assertEquals(Math.sqrt(3), BOX.findIntersectionT(new Ray(new Point(-1, -1, -1), new Vector(1, 1, 1))), DELTA,
                "Wrong entry distance for a diagonal ray");

        // =============== Boundary Values Tests ==================

        // TC11: Ray parallel to a face, outside the box
        assertEquals(AABB.NO_HIT, BOX.findIntersectionT(new Ray(new Point(-1, 1.5, 0.5), Vector.AXIS_X)),
                "A parallel ray outside the box was reported as a hit");
    }

    /**
     * Test method for {@link AABB#isOverlapping(AABB)}.
     */
    @Test
    void testIsOverlapping() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Partially overlapping boxes
        assertTrue(BOX.isOverlapping(new AABB(new Point(0.5, 0.5, 0.5), new Point(2, 2, 2))),
                "Overlapping boxes were not detected");

        // TC02: Separate boxes
        assertFalse(BOX.isOverlapping(new AABB(new Point(2, 2, 2), new Point(3, 3, 3))),
                "Separate boxes were reported as overlapping");

        // TC03: A box inside the other
        assertTrue(BOX.isOverlapping(new AABB(new Point(0.2, 0.2, 0.2), new Point(0.8, 0.8, 0.8))),
                "A contained box was not detected");

        // =============== Boundary Values Tests ==================

        // TC11: Boxes touching on a face
        assertTrue(BOX.isOverlapping(new AABB(new Point(1, 0, 0), new Point(2, 1, 1))),
                "Touching boxes were not reported as overlapping");

        // TC12: No box
        assertFalse(BOX.isOverlapping(null), "Overlap with no box was reported");
    }

    /**
     * Test method for {@link AABB#union(AABB)}.
     */
    @Test
    void testUnion() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Union of two separate boxes
        AABB other = new AABB(new Point(-1, 2, 0), new Point(0, 3, 0.5));
        assertEquals(new AABB(new Point(-1, 0, 0), new Point(1, 3, 1)), BOX.union(other), "union() returned a wrong box");

        // =============== Boundary Values Tests ==================

        // TC11: Union with no box
        assertEquals(BOX, BOX.union(null), "union() with no box changed the box");
    }

    /**
     * Test method for {@link AABB#enclosing(java.util.Collection)}.
     */
    @Test
    void testEnclosing() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Box enclosing a set of points
        List<Point> points = List.of(new Point(1, -2, 3), new Point(-1, 2, 0), new Point(0, 0, 5));
        assertEquals(new AABB(new Point(-1, -2, 0), new Point(1, 2, 5)), AABB.enclosing(points),
                "enclosing() returned a wrong box");

        // =============== Boundary Values Tests ==================

        // TC11: A single point gives a degenerate box
        assertEquals(new AABB(Point.ZERO, Point.ZERO), AABB.enclosing(List.of(Point.ZERO)),
                "enclosing() of a single point is not that point");
    }
}
