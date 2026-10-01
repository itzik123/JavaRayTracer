package scene;

import primitives.Double3;
import primitives.Texture;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads the materials of an OBJ model from a Wavefront MTL file.
 * <p>
 * Supported statements are {@code newmtl}, {@code Kd} (diffuse), {@code Ks} (specular),
 * {@code Ns} (shininess) and {@code map_Kd} (diffuse texture). Materials are optional decoration,
 * so problems are reported as warnings and never abort loading the model: an unreadable file
 * yields no materials, and a missing texture leaves its material untextured.
 *
 * @author itzik123
 */
final class MtlParser {

    /** Reports problems with the material file. */
    private static final System.Logger LOGGER = System.getLogger(MtlParser.class.getName());

    /** Utility class - not instantiable. */
    private MtlParser() {}

    /**
     * Parses a material file.
     *
     * @param filePath the path of the MTL file
     * @return the materials, by name (empty if the file cannot be read)
     */
    static Map<String, ParsedMaterial> parse(String filePath) {
        Map<String, ParsedMaterial> materials = new HashMap<>();
        File directory = new File(filePath).getAbsoluteFile().getParentFile();
        ParsedMaterial current = null;
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(new File(filePath).toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts[0].equals("newmtl")) {
                    current = new ParsedMaterial(restOf(line));
                    materials.put(current.name, current);
                } else if (current != null) {
                    switch (parts[0]) {
                        case "Kd" -> current.material.setKd(triad(parts));
                        case "Ks" -> current.material.setKs(triad(parts));
                        case "Ns" -> current.material.setShininess((int) Double.parseDouble(parts[1]));
                        case "map_Kd" -> current.texture = loadTexture(directory, textureFileName(parts), current.name);
                        default -> { } // other statements are not supported
                    }
                }
            }
        } catch (IOException | UncheckedIOException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Cannot read material file {0}: {1}", filePath, e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Malformed material file {0} at line {1}: {2}",
                    filePath, lineNumber, e.getMessage());
        }
        return materials;
    }

    /**
     * Extracts the argument of a statement.
     *
     * @param line a statement line
     * @return everything after the keyword, trimmed (names and paths may contain spaces)
     */
    static String restOf(String line) {
        String[] keywordAndRest = line.split("\\s+", 2);
        return keywordAndRest.length < 2 ? "" : keywordAndRest[1].trim();
    }

    /**
     * Reads an RGB coefficient; a single value applies to all three channels.
     *
     * @param parts the statement tokens
     * @return the coefficient
     */
    private static Double3 triad(String[] parts) {
        double r = Double.parseDouble(parts[1]);
        return parts.length < 4 ? new Double3(r)
                : new Double3(r, Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    /**
     * Extracts the file name of a texture statement, skipping options such as {@code -s 2 2}.
     * Options start with a dash and are followed by numeric or on/off arguments; everything after
     * them is the file name, which may contain spaces.
     *
     * @param parts the statement tokens
     * @return the texture file name
     */
    static String textureFileName(String[] parts) {
        int i = 1;
        while (i < parts.length - 1 && parts[i].startsWith("-")) {
            i++;
            while (i < parts.length - 1 && isOptionArgument(parts[i])) i++;
        }
        return String.join(" ", Arrays.copyOfRange(parts, i, parts.length));
    }

    /**
     * Checks whether a token is the argument of a texture option.
     *
     * @param token a statement token
     * @return {@code true} if the token is an option argument rather than part of a file name
     */
    private static boolean isOptionArgument(String token) {
        return token.equals("on") || token.equals("off") || token.matches("[-+]?\\d*\\.?\\d+([eE][-+]?\\d+)?");
    }

    /**
     * Loads a texture next to the material file. If the named file does not exist, the same name
     * with the other common extension (.png/.jpg) is tried, since exporters often mix them up.
     *
     * @param directory    the directory of the material file
     * @param fileName     the texture file name, relative to the directory
     * @param materialName the material, for the warning message
     * @return the texture, or {@code null} if it cannot be loaded
     */
    private static Texture loadTexture(File directory, String fileName, String materialName) {
        // Exporters on Windows write backslash separators, which other systems do not accept
        fileName = fileName.replaceAll("\\\\+", "/");
        File file = new File(directory, fileName);
        if (!file.exists()) {
            String lower = fileName.toLowerCase();
            String base = fileName.substring(0, Math.max(0, fileName.lastIndexOf('.')));
            if (lower.endsWith(".png")) file = new File(directory, base + ".jpg");
            else if (lower.endsWith(".jpg")) file = new File(directory, base + ".png");
        }
        if (!file.exists()) {
            LOGGER.log(System.Logger.Level.WARNING, "Texture {0} of material {1} not found", fileName, materialName);
            return null;
        }
        try {
            return new Texture(file.getPath());
        } catch (UncheckedIOException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Cannot load texture {0}: {1}", file, e.getMessage());
            return null;
        }
    }
}
