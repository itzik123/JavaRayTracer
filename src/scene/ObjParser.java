package scene;

import geometries.impl.Geometries;
import geometries.impl.TexturedTriangle;
import geometries.impl.Triangle;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Texture;
import primitives.UV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjParser {

    private Material _defaultMaterial = new Material().setKd(0.5).setKs(0.5).setShininess(30);
    private Color _defaultEmission = Color.BLACK;

    public ObjParser setMaterial(Material material) { this._defaultMaterial = material; return this; }
    public ObjParser setEmission(Color emission) { this._defaultEmission = emission; return this; }

    public Geometries parse(String filePath) {
        Geometries modelGeometries = new Geometries();

        List<Point> vertices = new ArrayList<>();
        vertices.add(new Point(0, 0, 0));

        // --- רשימת ה-UV החדשה ---
        List<UV> uvs = new ArrayList<>();
        uvs.add(new UV(0, 0)); // אינדקס 0 נשאר ריק (1-based index)

        Map<String, ParsedMaterial> materialsMap = new HashMap<>();

        Material activeMaterial = _defaultMaterial;
        Color activeEmission = _defaultEmission;
        Texture activeTexture = null; // השדה החדש!

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");

                if (parts[0].equals("mtllib")) {
                    String mtlPath = new File(filePath).getParent() + File.separator + parts[1];
                    materialsMap = MtlParser.parse(mtlPath);
                }
                else if (parts[0].equals("usemtl")) {
                    String materialName = parts[1];
                    if (materialsMap.containsKey(materialName)) {
                        ParsedMaterial pm = materialsMap.get(materialName);
                        activeEmission = pm.diffuseColor;
                        activeMaterial = pm.material;
                        activeTexture = pm.texture; // שולפים את הטקסטורה!
                    }
                }
                else if (parts[0].equals("v")) {
                    vertices.add(new Point(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])));
                }
                // --- קריאת קואורדינטות UV ---
                else if (parts[0].equals("vt")) {
                    uvs.add(new UV(Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
                }
                // --- בניית משטחים (Faces) ---
                else if (parts[0].equals("f")) {
                    List<Integer> vIndices = new ArrayList<>();
                    List<Integer> vtIndices = new ArrayList<>();

                    for (int i = 1; i < parts.length; i++) {
                        String[] subParts = parts[i].split("/");
                        vIndices.add(Integer.parseInt(subParts[0]));

                        // בדיקה אם קיים מידע על טקסטורה (למשל הפורמט הוא v/vt או v/vt/vn)
                        if (subParts.length > 1 && !subParts[1].isEmpty()) {
                            vtIndices.add(Integer.parseInt(subParts[1]));
                        }
                    }

                    // טריאנגולציה ויצירת המשולש המתאים
                    for (int i = 1; i < vIndices.size() - 1; i++) {
                        Point p1 = vertices.get(vIndices.get(0));
                        Point p2 = vertices.get(vIndices.get(i));
                        Point p3 = vertices.get(vIndices.get(i + 1));

                        // אם יש לנו טקסטורה פעילה וגם אינדקסים של UV בקובץ
                        if (activeTexture != null && vtIndices.size() >= 3) {
                            UV uv1 = uvs.get(vtIndices.get(0));
                            UV uv2 = uvs.get(vtIndices.get(i));
                            UV uv3 = uvs.get(vtIndices.get(i + 1));

                            TexturedTriangle tt = new TexturedTriangle(p1, p2, p3, uv1, uv2, uv3, activeTexture);
                            tt.setMaterial(activeMaterial);
                            modelGeometries.add(tt);
                        } else {
                            // משולש רגיל ללא תמונה
                            Triangle t = new Triangle(p1, p2, p3);
                            t.setEmission(activeEmission);
                            t.setMaterial(activeMaterial);
                            modelGeometries.add(t);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to parse OBJ file: " + filePath);
            e.printStackTrace();
        }

        return modelGeometries;
    }
}