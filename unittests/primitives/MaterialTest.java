package primitives;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Material}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Material#setIor(double)}</li>
 * <li>{@link Material#setBlurRadius(double)}</li>
 * <li>{@link Material#setBlurGlassRays(int)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class MaterialTest {

    /** Default constructor to satisfy JavaDoc generator */
    MaterialTest() { /* to satisfy JavaDoc generator */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-10;

    /**
     * Test method for {@link Material#setIor(double)}.
     */
    @Test
    void testSetIor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: The index of refraction of glass
        assertEquals(1.5, new Material().setIor(1.5).ior, DELTA, "Wrong index of refraction");

        // =============== Boundary Values Tests ==================

        // TC11: By default rays pass straight through
        assertEquals(1, new Material().ior, DELTA, "Wrong default index of refraction");

        // TC12: An index of 1 is allowed
        assertEquals(1, new Material().setIor(1).ior, DELTA, "An index of 1 was not accepted");

        // TC13: An index below 1
        assertThrows(IllegalArgumentException.class, () -> new Material().setIor(0.9), "An index below 1 did not throw");
    }

    /**
     * Test method for {@link Material#setBlurRadius(double)}.
     */
    @Test
    void testSetBlurRadius() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A positive radius
        assertEquals(0.1, new Material().setBlurRadius(0.1).blurRadius, DELTA, "Wrong blur radius");

        // =============== Boundary Values Tests ==================

        // TC11: A zero radius - sharp effects
        assertEquals(0, new Material().setBlurRadius(0).blurRadius, DELTA, "A zero blur radius was not accepted");

        // TC12: A negative radius
        assertThrows(IllegalArgumentException.class, () -> new Material().setBlurRadius(-0.1),
                "A negative blur radius did not throw");
    }

    /**
     * Test method for {@link Material#setBlurGlassRays(int)}.
     */
    @Test
    void testSetBlurGlassRays() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Several rays
        assertEquals(16, new Material().setBlurGlassRays(16).blurGlassRays, "Wrong number of rays");

        // =============== Boundary Values Tests ==================

        // TC11: A single ray
        assertEquals(1, new Material().setBlurGlassRays(1).blurGlassRays, "A single ray was not accepted");

        // TC12: No rays
        assertThrows(IllegalArgumentException.class, () -> new Material().setBlurGlassRays(0), "Zero rays did not throw");
    }
}
