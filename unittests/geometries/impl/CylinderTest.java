package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;
import primitives.Ray;

/**
 * Unit tests for class {@link Cylinder}.
 * The tests verify:
 * <ul>
 * <li>{@link Cylinder#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author itzik123
 */
class CylinderTest {
    /** Default constructor to satisfy JavaDoc generator */
    CylinderTest() { /* to satisfy JavaDoc generator */ }

    /** Vertex (0,0,0) used in Cylinder tests */
    private static final Point  POINT_ORIGIN    = new Point(0, 0, 0);
    /** direction vector used in tests */
    private static final Vector VECTOR          = new Vector(0, 0, 1);

    /** Vertex (1,0,1) used in Cylinder tests (Point on the side surface) */
    private static final Point  POINT_EP1       = new Point(1, 0, 1);
    /** Vertex (0.5,0,0) used in Cylinder tests (Point on the bottom base) */
    private static final Point  POINT_EP2       = new Point(0.5, 0, 0);
    /** Vertex (0.5,0,3) used in Cylinder tests (Point on the top base) */
    private static final Point  POINT_EP3       = new Point(0.5, 0, 3);

    /** Vertex (1,0,0) used in Cylinder tests (Point on the edge of the bottom base) */
    private static final Point  POINT_BVA1      = new Point(1, 0, 0);
    /** Vertex (1,0,3) used in Cylinder tests (Point on the edge of the top base) */
    private static final Point  POINT_BVA2      = new Point(1, 0, 3);
    /** Vertex (0,0,0) used in Cylinder tests (Point at the center of the bottom base) */
    private static final Point  POINT_BVA3      = new Point(0, 0, 0);
    /** Vertex (0,0,3) used in Cylinder tests (Point at the center of the top base) */
    private static final Point  POINT_BVA4      = new Point(0, 0, 3);

    /** normal vector for the side surface */
    private static final Vector VECTOR_NORMAL1  = new Vector(1, 0, 0);
    /** normal vector for the bottom base */
    private static final Vector VECTOR_NORMAL2  = new Vector(0, 0, -1);
    /** normal vector for the top base */
    private static final Vector VECTOR_NORMAL3  = new Vector(0, 0, 1);

    /**
     * test method for {@link Cylinder#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        Cylinder cylinder = new Cylinder(1, new Ray(POINT_ORIGIN, VECTOR), 3);

        // ============ Equivalence Partitions Tests ==============

        // TC01: test a normal on the side surface of the cylinder
        assertEquals(VECTOR_NORMAL1, cylinder.getNormal(POINT_EP1),
                "ERROR: Failed returning the correct normal on the side surface of the cylinder");

        // TC02: test a normal on the bottom base of the cylinder
        assertEquals(VECTOR_NORMAL2, cylinder.getNormal(POINT_EP2),
                "ERROR: Failed returning the correct normal on the bottom base of the cylinder");

        // TC03: test a normal on the top base of the cylinder
        assertEquals(VECTOR_NORMAL3, cylinder.getNormal(POINT_EP3),
                "ERROR: Failed returning the correct normal on the top base of the cylinder");

        // =============== Boundary Values Tests ==================

        // TC11: test a normal on the edge of the bottom base
        assertEquals(VECTOR_NORMAL2, cylinder.getNormal(POINT_BVA1),
                "ERROR: Failed returning the correct normal on the edge of the bottom base");

        // TC12: test a normal on the edge of the top base
        assertEquals(VECTOR_NORMAL3, cylinder.getNormal(POINT_BVA2),
                "ERROR: Failed returning the correct normal on the edge of the top base");

        // TC13: test a normal exactly at the center of the bottom base (origin)
        assertEquals(VECTOR_NORMAL2, cylinder.getNormal(POINT_BVA3),
                "ERROR: Failed returning the correct normal exactly at the center of the bottom base");

        // TC14: test a normal exactly at the center of the top base
        assertEquals(VECTOR_NORMAL3, cylinder.getNormal(POINT_BVA4),
                "ERROR: Failed returning the correct normal exactly at the center of the top base");
    }
}