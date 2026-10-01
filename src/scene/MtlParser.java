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
                    // Material names may contain spaces
                    String name = line.substring("newmtl".length()).trim();
                    currentMaterial = new ParsedMaterial(name);
                    materials.put(name, currentMaterial);
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
                    String imageFileName = line.substring("map_Kd".length()).trim();
                    String fullPath = directoryPath + imageFileName;

                    File imgFile = new File(fullPath);
                    if (!imgFile.exists()) {
                        if (imageFileName.toLowerCase().endsWith(".png")) {
                            imgFile = new File(fullPath.substring(0, fullPath.lastIndexOf('.')) + ".jpg");
                        } else if (imageFileName.toLowerCase().endsWith(".jpg")) {
                            imgFile = new File(fullPath.substring(0, fullPath.lastIndexOf('.')) + ".png");
                        }
                    }

                    if (imgFile.exists()) {
                        currentMaterial.texture = new Texture(imgFile.getAbsolutePath());
                        // החיישן הראשון שלנו! ידווח אם התמונה נטענה בהצלחה לזיכרון
                        System.out.println("✅ SUCCESS: Loaded texture [" + imgFile.getName() + "] for material [" + currentMaterial.name + "]");
                    } else {
                        System.err.println("❌ ERROR: Texture file not found: " + imageFileName);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Warning: Could not parse MTL file: " + filePath);
        }

        return materials;
    }
}