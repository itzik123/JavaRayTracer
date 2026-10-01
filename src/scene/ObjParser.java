package scene;

import geometries.impl.Geometries;
import geometries.impl.TexturedTriangle;
import geometries.impl.Triangle;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Texture;
import primitives.UV;
import primitives.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Loads a triangle mesh from a Wavefront OBJ file, with its MTL materials and textures.
 * <p>
 * Supported statements are {@code v}, {@code vt}, {@code f} (any polygon, fan-triangulated,
 * with absolute or negative relative indices), {@code mtllib} and {@code usemtl}. Faces with
 * texture coordinates and a textured material become {@link TexturedTriangle}s; all others become
 * plain {@link Triangle}s. Degenerate faces are skipped.
 * <p>
 * The parser is configured fluently before calling {@link #parse(String)}:
 *
 * <pre>{@code
 * Geometries model = new ObjParser().setScale(40).setOffset(new Vector(0, -10, 0)).parse("models/desk.obj");
 * }</pre>
 *
 * @author itzik123
 */
public class ObjParser {

    /** Separates the tokens of a statement. */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** The material of faces that have no MTL material. */
    private Material _defaultMaterial = new Material().setKd(0.5).setKs(0.5).setShininess(30);
    /** The emission of faces that have no MTL material. */
    private Color _defaultEmission = Color.BLACK;
    /** The uniform scale applied to the vertices. */
    private double _scale = 1;
    /** The translation applied to the vertices after scaling, or {@code null}. */
    private Vector _offset;
    /** Textured materials whose faces get texture coordinates by planar projection. */
    private final Set<String> _planarUvMaterials = new HashSet<>();

    /** Creates a parser with a default material, no emission and no transformation. */
    public ObjParser() {
    }

    /**
     * Sets the material of faces that have no MTL material.
     *
     * @param material the material of faces that have no MTL material
     * @return this parser
     */
    public ObjParser setMaterial(Material material) {
        _defaultMaterial = material;
        return this;
    }

    /**
     * Sets the emission of faces that have no MTL material.
     *
     * @param emission the emission of faces that have no MTL material
     * @return this parser
     */
    public ObjParser setEmission(Color emission) {
        _defaultEmission = emission;
        return this;
    }

    /**
     * Sets the uniform scale applied to the vertices.
     *
     * @param scale the uniform scale applied to the vertices
     * @return this parser
     */
    public ObjParser setScale(double scale) {
        _scale = scale;
        return this;
    }

    /**
     * Sets the translation applied to the vertices after scaling.
     *
     * @param offset the translation applied to the vertices after scaling
     * @return this parser
     */
    public ObjParser setOffset(Vector offset) {
        _offset = offset;
        return this;
    }

    /**
     * Makes the textures of some materials be mapped by projecting them onto the XY plane,
     * stretched over the bounding rectangle of all the faces that use the material. Useful for
     * flat surfaces such as screens, which exporters often leave without texture coordinates.
     *
     * @param materialNames the names of the materials
     * @return this parser
     */
    public ObjParser setPlanarUvMaterials(String... materialNames) {
        _planarUvMaterials.addAll(List.of(materialNames));
        return this;
    }

    /**
     * Loads a model.
     *
     * @param filePath the path of the OBJ file
     * @return the triangles of the model
     * @throws SceneLoadException if the file cannot be read or is malformed
     */
    public Geometries parse(String filePath) {
        File file = new File(filePath).getAbsoluteFile();
        Session session = new Session(file.getParentFile());
        int lineNumber = 0;
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                session.readStatement(line.trim());
            }
        } catch (IOException e) {
            throw new SceneLoadException("Cannot read model file " + filePath, e);
        } catch (RuntimeException e) {
            throw new SceneLoadException("Malformed model file " + filePath + " at line " + lineNumber, e);
        }
        session.addPlanarMappedFaces();
        return session._model;
    }

    /**
     * The state of loading one file.
     */
    private final class Session {
        /** The directory of the model, where material files are looked up. */
        private final File _directory;
        /** The loaded triangles. */
        private final Geometries _model = new Geometries();
        /** The vertices; index 0 is a placeholder since OBJ indices start at 1. */
        private final List<Point> _vertices = new ArrayList<>(List.of(Point.ZERO));
        /** The texture coordinates; index 0 is a placeholder since OBJ indices start at 1. */
        private final List<UV> _uvs = new ArrayList<>(List.of(new UV(0, 0)));
        /** The materials of the loaded material libraries, by name. */
        private final Map<String, ParsedMaterial> _materials = new HashMap<>();
        /** Faces of planar-mapped materials, collected until their full extent is known. */
        private final Map<ParsedMaterial, List<Point[]>> _planarFaces = new LinkedHashMap<>();

        /** The active MTL material, or {@code null}. */
        private ParsedMaterial _active;
        /** The material of new faces. */
        private Material _material = _defaultMaterial;
        /** The emission of new untextured faces. */
        private Color _emission = _defaultEmission;

        /**
         * Starts loading a model.
         *
         * @param directory the directory of the model
         */
        Session(File directory) {
            _directory = directory;
        }

        /**
         * Processes one statement of the OBJ file.
         *
         * @param line the trimmed line
         */
        void readStatement(String line) {
            if (line.isEmpty() || line.startsWith("#")) return;
            String[] parts = WHITESPACE.split(line);
            switch (parts[0]) {
                case "v" -> _vertices.add(transform(
                        Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])));
                case "vt" -> _uvs.add(new UV(Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
                case "f" -> readFace(parts);
                case "mtllib" -> _materials.putAll(MtlParser.parse(new File(_directory, MtlParser.restOf(line)).getPath()));
                case "usemtl" -> useMaterial(MtlParser.restOf(line));
                default -> { } // normals, groups and smoothing are not needed for rendering
            }
        }

        /**
         * Scales and translates a vertex.
         *
         * @param x the X coordinate in the file
         * @param y the Y coordinate in the file
         * @param z the Z coordinate in the file
         * @return the vertex in scene coordinates
         */
        private Point transform(double x, double y, double z) {
            x *= _scale;
            y *= _scale;
            z *= _scale;
            return _offset == null ? new Point(x, y, z)
                    : new Point(x + _offset.getX(), y + _offset.getY(), z + _offset.getZ());
        }

        /**
         * Switches the active material. Unknown names keep the current material.
         *
         * @param name the material name
         */
        private void useMaterial(String name) {
            ParsedMaterial material = _materials.get(name);
            if (material == null) return;
            _active = material;
            _material = material.material;
            _emission = Color.BLACK; // MTL colors are reflectances, not light sources
        }

        /**
         * Triangulates a face as a fan around its first vertex and adds the triangles.
         *
         * @param parts the statement tokens, each {@code v}, {@code v/vt}, {@code v//vn} or
         *              {@code v/vt/vn}
         */
        private void readFace(String[] parts) {
            int count = parts.length - 1;
            Point[] points = new Point[count];
            UV[] uvs = new UV[count];
            boolean hasUvs = true;
            for (int i = 0; i < count; i++) {
                String[] indices = parts[i + 1].split("/");
                points[i] = _vertices.get(resolve(Integer.parseInt(indices[0]), _vertices.size()));
                if (indices.length > 1 && !indices[1].isEmpty())
                    uvs[i] = _uvs.get(resolve(Integer.parseInt(indices[1]), _uvs.size()));
                else hasUvs = false;
            }

            Texture texture = _active == null ? null : _active.texture;
            boolean planar = texture != null && _planarUvMaterials.contains(_active.name);
            for (int i = 1; i < count - 1; i++) {
                if (planar) {
                    _planarFaces.computeIfAbsent(_active, m -> new ArrayList<>())
                            .add(new Point[]{points[0], points[i], points[i + 1]});
                    continue;
                }
                try {
                    if (texture != null && hasUvs)
                        _model.add(new TexturedTriangle(points[0], points[i], points[i + 1],
                                uvs[0], uvs[i], uvs[i + 1], texture).setMaterial(_material));
                    else
                        _model.add(new Triangle(points[0], points[i], points[i + 1])
                                .setEmission(_emission).setMaterial(_material));
                } catch (IllegalArgumentException e) {
                    // degenerate face (coincident or collinear vertices) - nothing to render
                }
            }
        }

        /**
         * Converts an OBJ index to a list index. Positive indices are absolute (1-based);
         * negative indices count back from the last element read so far.
         *
         * @param index the index in the file
         * @param size  the size of the list, including the placeholder
         * @return the list index
         */
        private int resolve(int index, int size) {
            int resolved = index < 0 ? size + index : index;
            if (resolved <= 0 || resolved >= size) throw new IndexOutOfBoundsException("Invalid index " + index);
            return resolved;
        }

        /**
         * Adds the collected faces of planar-mapped materials, with texture coordinates projected
         * from their XY extent.
         */
        void addPlanarMappedFaces() {
            for (var entry : _planarFaces.entrySet()) {
                List<Point[]> faces = entry.getValue();
                double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
                double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
                for (Point[] face : faces)
                    for (Point p : face) {
                        minX = Math.min(minX, p.getX());
                        maxX = Math.max(maxX, p.getX());
                        minY = Math.min(minY, p.getY());
                        maxY = Math.max(maxY, p.getY());
                    }
                double width = maxX > minX ? maxX - minX : 1;
                double height = maxY > minY ? maxY - minY : 1;

                ParsedMaterial material = entry.getKey();
                for (Point[] face : faces) {
                    UV[] uvs = new UV[3];
                    for (int i = 0; i < 3; i++)
                        uvs[i] = new UV((face[i].getX() - minX) / width, (face[i].getY() - minY) / height);
                    try {
                        _model.add(new TexturedTriangle(face[0], face[1], face[2], uvs[0], uvs[1], uvs[2],
                                material.texture).setMaterial(material.material));
                    } catch (IllegalArgumentException e) {
                        // degenerate face - nothing to render
                    }
                }
            }
        }
    }
}
