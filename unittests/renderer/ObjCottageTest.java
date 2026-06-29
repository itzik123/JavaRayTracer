package renderer;

import geometries.impl.Geometries;
import geometries.impl.Plane;
import org.junit.jupiter.api.Test;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import primitives.*;
import renderer.postprocessing.ColorInversionFilter;
import renderer.postprocessing.PostProcessFilter;
import scene.ObjParser;
import scene.Scene;
import renderer.postprocessing.FXAAFilter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class ObjCottageTest {

    /**
     * פונקציית עזר לייצור מצלמה "נקייה" לחלוטין לכל צילום.
     * מונעת זליגת נתונים בין המצלמות ומבטיחה שחישובי הווקטורים יתחילו מאפס.
     */
    private Camera buildCleanCamera(Scene scene, Point location, Point target) {
        return Camera.getBuilder()
                .setLocation(location)
                .setDirection(target.subtract(location)) // חישוב כיוון המבט אל עבר המטרה
                .setVpDistance(100)
                .setVpSize(150, 150)
                .setResolution(1080, 1080)
                .setAntiAliasing(25, new Jittered())
                .setMultithreading(-2)
                .setDebugPrint(1)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                //.addPostProcessFilter(new FXAAFilter().setJitterStrength(0.05f))
                .build();
    }

    @Test
    public void renderCottageAngles() {
        // 1. הגדרת סצנה
        Scene scene = new Scene("Textured Cottage Showcase")
                .setBackground(new Color(135, 206, 235))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(20)));

        // 2. תאורה
        scene.lights.add(new DirectionalLight(new Color(255, 240, 220).reduce(4), new Vector(-1, -0.8, -1)));

        // 3. הוספת הרצפה
        Material floorMaterial = new Material().setKd(0.8).setKs(0.1).setShininess(10);
        scene.geometries.add(new Plane(new Point(0, -0.1, 0), new Vector(0, 1, 0))
                .setEmission(new Color(70, 70, 75))
                .setMaterial(floorMaterial));

        // 4. טעינת הבקתה
        System.out.println("Parsing cottage model...");
        Geometries cottage = new ObjParser().parse("models/cottage.obj");
        scene.geometries.add(cottage);

        // נקודת המרכז המדויקת שחילצת מנתוני המודל!
        Point center = new Point(0.2, 3.36, 0.3);

        // --- צילום 1: זווית קדמית-ימנית ---
        System.out.println("Rendering Angle 1 (Front-Right)...");
        Camera cam1 = buildCleanCamera(scene, new Point(14, 5, 18), center);
        cam1.renderImage();
        cam1.writeToImage("Cottage1");

        // --- צילום 2: זווית נמוכה דרמטית משמאל ---
        System.out.println("Rendering Angle 2 (Low-Left)...");
        // נסתכל טיפה מעל המרכז (Y=4.5) כדי לתת לבית תחושה גדולה ועוצמתית מלמטה
        Camera cam2 = buildCleanCamera(scene, new Point(-14, 1.5, 18), new Point(0.2, 4.5, 0.3));
        cam2.renderImage();
        cam2.writeToImage("Cottage2");

        // --- צילום 3: מבט ממעוף הציפור ---
        System.out.println("Rendering Angle 3 (Top-Down)...");
        // ממוקמת בגובה 25 אבל משוכה מעט קדימה (Z=8) כדי לראות גם את החזית ולא רק את הגג
        Camera cam3 = buildCleanCamera(scene, new Point(0.2, 25, 8), center);
        cam3.renderImage();
        cam3.writeToImage("Cottage3");

        System.out.println("All renderings completed successfully!");
    }

    @Test
    public void testInvertExistingImage() {
        // נתיב לתמונת המקור (ודא שיש תמונה כזו בתיקייה שלך לפני ההרצה)
        String inputImagePath = "images/AA_Cage_Test_1_Rays.png";

        // נתיב לקובץ שייוצר
        String outputImagePath = "images/AA_Cage_Test_1_Rays_FXAA.png";

        try {
            // 1. טעינת התמונה הקיימת מהדיסק
            File inputFile = new File(inputImagePath);
            if (!inputFile.exists()) {
                fail("Input image not found at: " + inputFile.getAbsolutePath());
            }
            BufferedImage originalImage = ImageIO.read(inputFile);

            // 2. יצירת הפילטר והפעלתו על התמונה
            FXAAFilter inversionFilter = new FXAAFilter().setJitterStrength(0.2f);
            BufferedImage invertedImage = inversionFilter.apply(originalImage);

            // 3. שמירת התמונה החדשה לדיסק
            File outputFile = new File(outputImagePath);

            // יצירת תיקיית images במידה והיא לא קיימת
            outputFile.getParentFile().mkdirs();

            // כתיבת הקובץ (שומרים בפורמט jpg או png בהתאם לסיומת)
            ImageIO.write(invertedImage, "png", outputFile);

            System.out.println("Success! Inverted image saved to: " + outputFile.getAbsolutePath());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void scoutArrayHousesBoundingBox() {
        System.out.println("Starting Scout Mission: Parsing 10 houses...");

        // יצירת סצנה ריקה רק לטובת בדיקת הנתונים
        scene.Scene scoutScene = new scene.Scene("Scout Scene");
        scene.ObjParser parser = new scene.ObjParser();

        // טעינת 10 הבתים בלולאה
        for (int i = 1; i <= 10; i++) {
            // עיצוב המספר שיהיה בפורמט 01, 02 וכו'
            String houseName = String.format("Residential Buildings %03d.obj", i);
            System.out.println("\n--- Parsing " + houseName + " ---");

            // ודא שהנתיב תואם למיקום התיקייה אצלך
            Geometries house = parser.parse("obj/" + houseName);
            scoutScene.geometries.add(house);
        }

        System.out.println("\nAll houses parsed successfully! Waiting for Bounding Box data...");
    }
}