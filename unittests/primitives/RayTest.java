package primitives;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for class {@link Ray}.
 * The tests verify:
 * <ul>
 * <li>ray constructor validity</li>
 * <li>{@link Ray#getPoint(double)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP).
 * @author ityy35-bit
 */
class RayTest {

    /** Default constructor to satisfy JavaDoc generator */
    RayTest() { /* to satisfy JavaDoc generator */ }
    
    /** origin point for ray tests */
    private static final Point  POINT        = new Point(-1, 1, 1);

    /** direction vector for ray test (not normalized) */
    private static final Vector  VECTOR       = new Vector(2, 0, 1);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA         = 1e-6;
    


    /**
     * Test method for {@link Ray#Ray(Point,Vector)}.
     * Verifies correct and incorrect ray constructions.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: regular addition
        final  Ray ray = new Ray(POINT,VECTOR);
        assertEquals(1,ray.direction().lengthSquared(), DELTA,
                "ERROR: direction vector not normalized");

    }

    /**point (2,0,0) used for test*/
    private static final Point EP01 = new Point(2,0,0);
    /**point (-2,0,0) used for test*/
    private static final Point EP02 = new Point(-2,0,0);
    /**point (0,0,0) used for test*/
    private static final Point BV11 = new Point(0,0,0);
    /**ray used for test*/
    private static final Ray RAY = new Ray(new Point(0,0,0), new Vector(1,0,0));

    /**
     * Test method for {@link Ray#getPoint(double)}.
     */
    @Test
    void testGetPoint(){
        // ============ Equivalence Partitions Tests ==============

        // TC01: positive t
        assertEquals(EP01, RAY.getPoint(2), "ERROR: wrong point");

        // TC01: negative t
        assertEquals(EP02, RAY.getPoint(-2), "ERROR: wrong point");

        // =============== Boundary Values Tests ==================

        // TC11: t equals 0
        assertEquals(BV11, RAY.getPoint(0), "ERROR: wrong point");

    }


}