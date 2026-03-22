package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for class {@link Sphere}.
 * The tests verify:
 * <ul>
 * <li>{@link Sphere#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP)
 * @author itzik123
 */
class SphereTest {

    /** Default constructor to satisfy JavaDoc generator */
    SphereTest() { /* to satisfy JavaDoc generator */ }

    /** Vertex (0,0,0) used in Sphere tests */
    private static final Point  POINT_ORIGIN      = new Point(0, 0, 0);
    /** Vertex (1,0,0) used in Sphere tests */
    private static final Point  POINT_EP          = new Point(1, 0, 0);
    /** normal vector used in tests */
    private static final Vector VECTOR            = new Vector(1, 0, 0);

    /**
     * test method for {@link Sphere#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        Sphere sphere = new Sphere(POINT_ORIGIN, 1);

        // TC01: test a normal on the sphere
        assertEquals(VECTOR.normalize(), sphere.getNormal(POINT_EP),
                "ERROR: Failed returning the right normal");
    }

}