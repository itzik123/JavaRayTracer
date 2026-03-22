package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import primitives.Point;
import geometries.impl.Triangle;
import primitives.Vector;

/**
 * Unit tests for class {@link Triangle}.
 * The tests verify:
 * <ul>
 * <li>{@link Triangle#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP)
 * @author ityy35-bit
 */
class TriangleTest {

    /** Default constructor to satisfy JavaDoc generator */
    TriangleTest() { /* to satisfy JavaDoc generator */ }

    /** Vertex (1,1,1) used in triangle tests */
    private static final Point  POINT_EP       = new Point(1, 1, 1);
    /** Vertex (1,0,0) used in triangle tests */
    private static final Point  POINT_X       = new Point(1, 0, 0);
    /** Vertex (0,1,0) used in triangle tests */
    private static final Point  POINT_Y       = new Point(0, 1, 0);
    /** Vertex (0,0,1) used in triangle tests */
    private static final Point  POINT_Z       = new Point(0, 0, 1);
    /** normal vector used in tests */
    private static final Vector VECTOR = new Vector(1, 1, 1);
    /**
     * test method for {@link Triangle#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        Triangle triangle = new Triangle(POINT_X, POINT_Y, POINT_Z);

        // TC01: test a normal on the triangle
        assertEquals(VECTOR.normalize(), triangle.getNormal(POINT_EP) ,
                "ERROR: Failed returning the right normal");
    }

}