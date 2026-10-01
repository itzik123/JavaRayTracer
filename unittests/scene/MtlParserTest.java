package scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MtlParser}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link MtlParser#textureFileName(String[])}</li>
 * <li>{@link MtlParser#restOf(String)}</li>
 * </ul>
 * Tests follow the Equivalence Partitions (EP) and Boundary Values (BVA) methodology.
 *
 * @author itzik123
 */
class MtlParserTest {

    /** Default constructor to satisfy JavaDoc generator */
    MtlParserTest() { /* to satisfy JavaDoc generator */ }

    /**
     * Splits a statement into tokens, as the parser does.
     *
     * @param statement the statement
     * @return the tokens
     */
    private static String[] tokens(String statement) {
        return statement.split("\\s+");
    }

    /**
     * Test method for {@link MtlParser#textureFileName(String[])}.
     */
    @Test
    void testTextureFileName() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A plain file name
        assertEquals("wood.png", MtlParser.textureFileName(tokens("map_Kd wood.png")), "Wrong plain file name");

        // TC02: Options with numeric arguments are skipped
        assertEquals("wood.png", MtlParser.textureFileName(tokens("map_Kd -s 2.0 2.0 1 -o 0.5 wood.png")),
                "Options were not skipped");

        // TC03: Options with on/off arguments are skipped
        assertEquals("wood.png", MtlParser.textureFileName(tokens("map_Kd -clamp on wood.png")),
                "An on/off option was not skipped");

        // =============== Boundary Values Tests ==================

        // TC11: A file name with spaces
        assertEquals("E-45 steel detail.jpg", MtlParser.textureFileName(tokens("map_Kd E-45 steel detail.jpg")),
                "A file name with spaces was cut");

        // TC12: A file name with spaces after options
        assertEquals("my texture.png", MtlParser.textureFileName(tokens("map_Kd -bm 0.5 my texture.png")),
                "A file name with spaces after options was cut");
    }

    /**
     * Test method for {@link MtlParser#restOf(String)}.
     */
    @Test
    void testRestOf() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A name with spaces
        assertEquals("Main desk wood", MtlParser.restOf("newmtl   Main desk wood  "), "Wrong statement argument");

        // =============== Boundary Values Tests ==================

        // TC11: A statement without an argument
        assertEquals("", MtlParser.restOf("newmtl"), "A missing argument is not empty");
    }
}
