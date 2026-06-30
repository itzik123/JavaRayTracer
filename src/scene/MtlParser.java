package scene;

import primitives.Color;
import primitives.Texture;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MtlParser {

    public static Map<String, ParsedMaterial> parse(String filePath) {
        Map<String, ParsedMaterial> materials = new HashMap<>();
        ParsedMaterial currentMaterial = null;

        // שמירת נתיב התיקייה כדי שנדע מאיפה לטעון את ה-PNG/JPG
        String directoryPath = new File(filePath).getParent() + File.separator;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");

                if (parts[0].equals("newmtl")) {
                    currentMaterial = new ParsedMaterial(parts[1]);
                    materials.put(parts[1], currentMaterial);
                }
                else if (parts[0].equals("Kd") && currentMaterial != null) {
                    int r = (int) (Double.parseDouble(parts[1]) * 255);
                    int g = (int) (Double.parseDouble(parts[2]) * 255);
                    int b = (int) (Double.parseDouble(parts[3]) * 255);
                    currentMaterial.diffuseColor = new Color(r, g, b);
                }
                else if (parts[0].equals("Ks") && currentMaterial != null) {
                    currentMaterial.material.setKs(Double.parseDouble(parts[1]));
                }
                else if (parts[0].equals("Ns") && currentMaterial != null) {
                    currentMaterial.material.setShininess((int) Double.parseDouble(parts[1]));
                }
                else if (parts[0].equals("map_Kd") && currentMaterial != null) {
                    String imageFileName = parts[1];
                    String fullPath = directoryPath + imageFileName;

                    // --- מנגנון הגנה חכם לטקסטורות ---
                    try {
                        currentMaterial.texture = new Texture(fullPath);
                    } catch (Exception e) {
                        // במידה וטעינת הטקסטורה נכשלת (למשל סיומת לא נכונה או קובץ חסר)
                        try {
                            // מנסה להחליף סיומת מ-png ל-jpg או להפך
                            if (imageFileName.toLowerCase().endsWith(".png")) {
                                String altPath = fullPath.substring(0, fullPath.lastIndexOf('.')) + ".jpg";
                                currentMaterial.texture = new Texture(altPath);
                            } else if (imageFileName.toLowerCase().endsWith(".jpg")) {
                                String altPath = fullPath.substring(0, fullPath.lastIndexOf('.')) + ".png";
                                currentMaterial.texture = new Texture(altPath);
                            } else {
                                System.err.println("Warning: Missing texture file: " + fullPath + " - Rendering without it.");
                            }
                        } catch (Exception ex) {
                            System.err.println("Warning: Missing texture file and fallbacks failed: " + fullPath + " - Rendering without it.");
                        }
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Warning: Could not parse MTL file: " + filePath);
        }

        return materials;
    }
}