package primitives;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Vector}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link Vector#Vector(double, double, double)}</li>
 * <li>{@link Vector#add(Vector)}</li>
 * <li>{@link Vector#subtract(Point)}</li>
 * <li>{@link Vector#scale(double)}</li>
 * <li>{@link Vector#dotProduct(Vector)}</li>
 * <li>{@link Vector#crossProduct(Vector)}</li>
 * <li>{@link Vector#lengthSquared()}</li>
 * <li>{@link Vector#length()}</li>
 * <li>{@link Vector#normalize()}</li>
 * <li>{@link Vector#orthogonal()}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class VectorTest {

    /** Default constructor to satisfy JavaDoc generator */
    VectorTest() { /* to satisfy JavaDoc generator */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** A general vector (1,2,3) */
    private static final Vector V1 = new Vector(1, 2, 3);
    /** A vector (0,3,-2), orthogonal to {@link #V1} */
    private static final Vector V2 = new Vector(0, 3, -2);
    /** A vector (-2,-4,-6), parallel to {@link #V1} in the opposite direction */
    private static final Vector V3 = new Vector(-2, -4, -6);
    /** The vector (-1,-2,-3), opposite to {@link #V1} */
    private static final Vector V1_OPPOSITE = new Vector(-1, -2, -3);

    /**
     * Test method for {@link Vector#Vector(double, double, double)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Constructing a non-zero vector
        assertDoesNotThrow(() -> new Vector(1, 2, 3), "Failed constructing a correct vector");

        // =============== Boundary Values Tests ==================

        // TC11: Constructing the zero vector
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0),
                "Constructing the zero vector did not throw");

        // TC12: Constructing the zero vector from a triad
        assertThrows(IllegalArgumentException.class, () -> new Vector(Double3.ZERO),
                "Constructing the zero vector from a triad did not throw");
    }

    /**
     * Test method for {@link Vector#add(Vector)}.
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Adding two general vectors
        assertEquals(new Vector(1, 5, 1), V1.add(V2), "add() returned a wrong vector");

        // TC02: Adding a vector that is parallel and longer in the opposite direction
        assertEquals(V1_OPPOSITE, V1.add(V3), "add() of opposite vectors returned a wrong vector");

        // =============== Boundary Values Tests ==================

        // TC11: Adding the opposite vector creates the zero vector
        assertThrows(IllegalArgumentException.class, () -> V1.add(V1_OPPOSITE),
                "add() of a vector and its opposite did not throw");
    }

    /**
     * Test method for {@link Vector#subtract(Point)}.
     */
    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Subtracting two general vectors
        assertEquals(new Vector(3, 6, 9), V1.subtract(V3), "subtract() returned a wrong vector");

        // =============== Boundary Values Tests ==================

        // TC11: Subtracting a vector from itself creates the zero vector
        assertThrows(IllegalArgumentException.class, () -> V1.subtract(V1),
                "subtract() of a vector from itself did not throw");
    }

    /**
     * Test method for {@link Vector#scale(double)}.
     */
    @Test
    void testScale() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Scaling by a positive scalar
        assertEquals(new Vector(3, 6, 9), V1.scale(3), "scale() by a positive scalar returned a wrong vector");

        // TC02: Scaling by a negative scalar
        assertEquals(V3, V1.scale(-2), "scale() by a negative scalar returned a wrong vector");

        // =============== Boundary Values Tests ==================

        // TC11: Scaling by zero creates the zero vector
        assertThrows(IllegalArgumentException.class, () -> V1.scale(0), "scale() by zero did not throw");
    }

    /**
     * Test method for {@link Vector#dotProduct(Vector)}.
     */
    @Test
    void testDotProduct() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Dot product of two general vectors
        assertEquals(-28, V1.dotProduct(V3), DELTA, "dotProduct() returned a wrong value");

        // TC02: Dot product of a vector with itself is its squared length
        assertEquals(V1.lengthSquared(), V1.dotProduct(V1), DELTA, "dotProduct() with itself is not lengthSquared()");

        // =============== Boundary Values Tests ==================

        // TC11: Dot product of orthogonal vectors
        assertEquals(0, V2.dotProduct(V3), DELTA, "dotProduct() of orthogonal vectors is not zero");
    }

    /**
     * Test method for {@link Vector#crossProduct(Vector)}.
     */
    @Test
    void testCrossProduct() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Cross product of two general vectors
        Vector cp = V1.crossProduct(V2);
        assertEquals(V1.length() * V2.length(), cp.length(), DELTA, "crossProduct() has a wrong length");
        assertEquals(0, cp.dotProduct(V1), DELTA, "crossProduct() is not orthogonal to its first operand");
        assertEquals(0, cp.dotProduct(V2), DELTA, "crossProduct() is not orthogonal to its second operand");

        // TC02: The cross product is anti-commutative
        assertEquals(cp, V2.crossProduct(V1).scale(-1), "crossProduct() is not anti-commutative");

        // =============== Boundary Values Tests ==================

        // TC11: Cross product of parallel vectors creates the zero vector
        assertThrows(IllegalArgumentException.class, () -> V1.crossProduct(V3),
                "crossProduct() of parallel vectors did not throw");
    }

    /**
     * Test method for {@link Vector#lengthSquared()}.
     */
    @Test
    void testLengthSquared() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Squared length of a general vector
        assertEquals(14, V1.lengthSquared(), DELTA, "lengthSquared() returned a wrong value");
    }

    /**
     * Test method for {@link Vector#length()}.
     */
    @Test
    void testLength() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Length of a general vector
        assertEquals(5, new Vector(0, 3, 4).length(), DELTA, "length() returned a wrong value");
    }

    /**
     * Test method for {@link Vector#normalize()}.
     */
    @Test
    void testNormalize() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Normalizing a general vector
        Vector u = V1.normalize();
        assertEquals(1, u.length(), DELTA, "normalize() did not return a unit vector");
        assertThrows(IllegalArgumentException.class, () -> V1.crossProduct(u),
                "normalize() changed the direction of the vector");
        assertEquals(V1.length(), V1.dotProduct(u), DELTA, "normalize() reversed the vector");
    }

    /**
     * Test method for {@link Vector#orthogonal()}.
     */
    @Test
    void testOrthogonal() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A general vector
        Vector o1 = V1.orthogonal();
        assertEquals(1, o1.length(), DELTA, "orthogonal() did not return a unit vector");
        assertEquals(0, o1.dotProduct(V1), DELTA, "orthogonal() is not orthogonal to a general vector");

        // =============== Boundary Values Tests ==================

        // TC11: A vector along the X axis, which cannot be crossed with the X axis itself
        Vector o2 = Vector.AXIS_X.orthogonal();
        assertEquals(0, o2.dotProduct(Vector.AXIS_X), DELTA, "orthogonal() is not orthogonal to the X axis");
    }
}
