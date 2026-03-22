package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;
import primitives.Ray;

/**
 * Unit tests for class {@link Tube}.
 * The tests verify:
 * <ul>
 * <li>{@link Tube#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author ityy35-bit
 */
class TubeTest {

    /** Default constructor to satisfy JavaDoc generator */
    TubeTest() { /* to satisfy JavaDoc generator */ }

    /** Vertex (0,0,0) used in Tube tests */
    private static final Point  POINT_ORIGIN    = new Point(0, 0, 0);
    /** Vertex (0,1,0) used in Tube tests */
    private static final Point  POINT_BVA       = new Point(0, 1, 0);
    /** Vertex (0,1,-1) used in Tube tests */
    private static final Point  POINT_EP1       = new Point(0, 1, -1);
    /** Vertex (0,1,1) used in Tube tests */
    private static final Point  POINT_EP2       = new Point(0, 1, 1);
    /** direction vector used in tests */
    private static final Vector VECTOR          = new Vector(0, 0, 1);
    /** normal vector used in tests */
    private static final Vector VECTOR_NORMAL   = new Vector(0, 1, 0);

    /**
     * test method for {@link Tube#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        Tube tube = new Tube(1, new Ray(POINT_ORIGIN,VECTOR));
        // ============ Equivalence Partitions Tests ==============

        // TC01: test a normal in front of the ray
        assertEquals(VECTOR_NORMAL, tube.getNormal(POINT_EP2),
                "ERROR: Failed returning the normal in front of the ray");

        // TC02: test a normal behind the ray
        assertEquals(VECTOR_NORMAL, tube.getNormal(POINT_EP1),
                "ERROR: Failed returning the normal behind the ray");

        // =============== Boundary Values Tests ==================

        // TC11: test a normal next to the origin
        assertEquals(VECTOR_NORMAL, tube.getNormal(POINT_BVA),
                "ERROR: Failed returning the normal behind the ray");

    }

}