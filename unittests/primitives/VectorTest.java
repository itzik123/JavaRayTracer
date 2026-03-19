package primitives;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for class {@link Vector}.
 * The tests verify:
 * <ul>
 * <li>{@link Vector#Vector(double, double, double)}</li>
 * <li>{@link Vector#add(Vector)}</li>
 * <li>{@link Vector#scale(double)}</li>
 * <li>{@link Vector#dotProduct(Vector)}</li>
 * <li>{@link Vector#crossProduct(Vector)}</li>
 * <li>{@link Vector#lengthSquared()}</li>
 * <li>{@link Vector#length()}</li>
 * <li>{@link Vector#normalize()}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
class VectorTest {

    /** Default constructor to satisfy JavaDoc generator */
    VectorTest() { /* to satisfy JavaDoc generator */ }

    /** First general vector used in tests */
    private static final Vector V1 = new Vector(1, 2, 3);
    /** Second vector, orthogonal to V1 */
    private static final Vector V2 = new Vector(0, 3, -2);
    /** Third vector, parallel to V1 but in opposite direction */
    private static final Vector V3 = new Vector(-2, -4, -6);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Test method for {@link Vector#Vector(double, double, double)}.
     * Verifies correct and incorrect vector constructions.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: constructing a valid vector
        assertDoesNotThrow(() -> new Vector(1, 2, 3),
                "ERROR: Failed constructing a correct vector");

        // =============== Boundary Values Tests ==================

        // TC11: constructing an invalid vector
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0),
                "ERROR: The program should throw an exception");
    }

    /**
     * test method for {@link Vector#add(Vector)}
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: normal addition
        assertEquals(new Vector(1, 5, 1), V1.add(V2),
                "ERROR: Failed adding two vectors");

        // =============== Boundary Values Tests ==================

        // TC11: substruct the vector by is negative
        assertThrows(IllegalArgumentException.class, () -> V1.add(new Vector(-1, -2, -3)),
                "ERROR: The program should throw an exception");
    }

    /**
     * test method for {@link Vector#scale(double)}
     */
    @Test
    void testScale() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Scaling a vector by a positive scalar
        assertEquals(new Vector(3, 6, 9), V1.scale(3),
                "ERROR: scale() with positive scalar gave wrong result");

        // TC02: Scaling a vector by a negative scalar
        assertEquals(V3, V1.scale(-2),
                "ERROR: scale() with negative scalar gave wrong result");

        // =============== Boundary Values Tests ==================

        // TC11: scaling a vector by zero
        assertThrows(IllegalArgumentException.class, () -> V1.scale(0),
                "ERROR: The program should throw an exception");
    }

    /**
     * test method for {@link Vector#dotProduct(Vector)}
     */
    @Test
    void testDotProduct() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Dot product of two general vectors
        assertEquals(-28, V1.dotProduct(V3), DELTA,
                "ERROR: dotProduct gave the wrong result");

        // =============== Boundary Values Tests ==================

        // TC11: Dot product of two orthogonal vectors
        assertEquals(0, V2.dotProduct(V3), DELTA,
                "ERROR: dotProduct gave the wrong result with orthogonal vectors");
    }

    /**
     * test method for {@link Vector#crossProduct(Vector)}
     */
    @Test
    void testCrossProduct() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Cross product of two general vectors
        Vector cp = V1.crossProduct(V2);

        // Ensure the length of the cross product is proper
        assertEquals(V1.length() * V2.length(), cp.length(), DELTA,
                "ERROR: cross product wrong result length");

        // Ensure the cross product is orthogonal to both original vectors
        assertEquals(0, cp.dotProduct(V1), DELTA,
                "ERROR: cross product result is not orthogonal to its first operand");
        assertEquals(0, cp.dotProduct(V2), DELTA,
                "ERROR: cross product result is not orthogonal to its second operand");
        // =============== Boundary Values Tests ==================

        // TC11: Cross product of two parallel vectors
        assertThrows(IllegalArgumentException.class, () -> V1.dotProduct(V3),
                "ERROR: cross product with parallel vectors does not throw an exception");
    }

    /**
     * test method for {@link Vector#lengthSquared()}
     */
    @Test
    void testLengthSquared() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: regular vector length squared
        assertEquals(14, V1.lengthSquared(), DELTA,
                "ERROR : Calculate the wrong length");
    }

    /**
     * test method for {@link Vector#length()}
     */
    @Test
    void testLength() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: regular vector length
        assertEquals(5, new Vector(0, 3, 4).length(), DELTA,
                "ERROR : Calculate the wrong length");
    }

    /**
     * test method for {@link Vector#normalize()}
     */
    @Test
    void testNormalize() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Normalizing a vector
        Vector v = V1.normalize();

        // Ensure the normalized vector is a unit vector
        assertEquals(1d, v.length(), DELTA,
                "ERROR: the normalized vector is not a unit vector");

        // Ensure the normalized vector is parallel to the original one
        assertThrows(IllegalArgumentException.class, () -> V1.crossProduct(v),
                "ERROR: the normalized vector is not parallel to the original one");

        // Ensure the normalized vector is in the same direction (dot product is positive)
        assertEquals(V1.length(), V1.dotProduct(v), DELTA,
                "ERROR: the normalized vector is opposite to the original one");
    }
}