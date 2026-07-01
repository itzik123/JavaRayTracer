package renderer;

import geometries.impl.Geometries;
import geometries.impl.Plane;
import lighting.PointLight;
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
    public void testLaptopRender() {
        Scene scene = new Scene("Laptop OBJ Scene")
                .setBackground(new Color(135, 206, 235)) // רקע תכלת
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(5)));

        // תאורת שמש טיפה יותר חזקה כדי להאיר היטב את המקלדת והמסך
        scene.lights.add(new DirectionalLight(new Color(255, 250, 240).reduce(2), new Vector(1, -1, 1)));

        System.out.println("Parsing OBJ file...");
        scene.ObjParser parser = new scene.ObjParser();
        scene.geometries.add(parser.parse("models/Laptop_High-Polay_HP_BI_2_obj.obj"));

        // 1. חישוב נקודת המרכז המדויקת של המחשב לפי ה-Bounding Box שלך
        Point target = new Point(0, 1.58, -0.67);

        // 2. המחשב קטן (5x5). נמקם את המצלמה קרוב מאוד! (8 יחידות ימינה, 7 למעלה, 8 אחורה)
        Point cameraLoc = new Point(8, 7, 8);

        System.out.println("Rendering image...");
        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(cameraLoc)
                .setDirection(target.subtract(cameraLoc), Vector.AXIS_Y)
                // 3. מסגור (View Plane) קטן שמתאים לגודל הלפטופ
                .setVpSize(10, 10).setVpDistance(10)
                .setResolution(600, 600) // רזולוציה גבוהה לפרטים הקטנים של הטקסטורה
                .setMultithreading(-2)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage("Laptop_Render_Perfect_Fit");

        System.out.println("Done!");
    }

    @Test
    public void testLaptopOnGlassTable() {
        Scene scene = new Scene("Laptop on Glass Table")
                .setBackground(new Color(10, 15, 25)) // רקע דרמטי וכהה להבלטת המסך
                // תאורה סביבתית חלשה מאוד כדי לא לשרוף את התמונה
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(10)));

        // --- התיקון לחשיפת היתר: החלשנו את השמש משמעותית! ---
        scene.lights.add(new DirectionalLight(
                new Color(255, 245, 230).reduce(5), // חלש בהרבה מהטסט הקודם
                new Vector(1, -1, 1)));

        // תאורת ספוט עדינה שתוסיף השתקפות על השולחן
        scene.lights.add(new PointLight(new Color(150, 150, 200), new Point(0, 15, 0))
                .setKl(0.01).setKq(0.001));

        System.out.println("Parsing OBJ file...");
        scene.ObjParser parser = new scene.ObjParser();
        scene.geometries.add(parser.parse("models/Laptop_High-Polay_HP_BI_2_obj.obj"));

        // --- הוספת שולחן הזכוכית טושטש ---
        Material glassTableMat = new Material()
                .setKd(0.05).setKs(0.8).setShininess(100)
                .setKt(0.7) // שקוף למחצה
                .setKr(0.2) // משתקף למחצה
                .setBlurRadius(0.15) // יוצר את פיזור הקרניים למראה זכוכית עבה (Diffuse Glass)
                .setBlurGlassRays(12); // כמות הדגימות באלומה

        // המחשב ממוקם בערך על Y=0. הקמנו את המישור ממש מתחתיו.
        scene.geometries.add(new geometries.impl.Plane(
                new Point(0, 0.01, 0),
                new Vector(0, 1, 0))
                .setEmission(new Color(5, 10, 15)) // גוון כחלחל-כהה לזכוכית
                .setMaterial(glassTableMat));

        Point target = new Point(0, 1.58, -0.67);
        Point cameraLoc = new Point(8, 7, 8);

        System.out.println("Rendering image...");
        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(cameraLoc)
                .setDirection(target.subtract(cameraLoc), Vector.AXIS_Y)
                .setVpSize(10, 10).setVpDistance(10)
                .setResolution(30, 30)
                .setMultithreading(-2)
                .setDebugPrint(0.1)
                .build()
                .renderImage()
                .writeToImage("Laptop_On_Glass_Table");

        System.out.println("Done!");
    }

    @Test
    public void testLaptopOnGlassTableFinal() {
        Scene scene = new Scene("Laptop on Glass Desk")
                .setBackground(new Color(10, 15, 25)) // רקע סטודיו כהה ואלגנטי
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(10)));

        // תאורת שמש מוחלשת שלא שורפת את המסך
        scene.lights.add(new DirectionalLight(new Color(255, 250, 240).reduce(5), new Vector(1, -1, 1)));

        // ספוט לייט עדין כדי לתת השתקפות על השולחן
        scene.lights.add(new PointLight(new Color(100, 100, 150), new Point(0, 10, 2))
                .setKl(0.01).setKq(0.001));

        System.out.println("Parsing OBJ file...");
        scene.ObjParser parser = new scene.ObjParser();
        scene.geometries.add(parser.parse("models/Laptop_High-Polay_HP_BI_2_obj.obj"));

        // --- חומר זכוכית חלבית לשולחן ---
        Material glassTableMat = new Material()
                .setKd(0.05).setKs(0.8).setShininess(100)
                .setKt(0.7)
                .setKr(0.2)
                .setBlurRadius(0.05)
                .setBlurGlassRays(12);

        // --- שולחן זכוכית (פוליגון מלבני ומיושר במקום מישור אינסופי) ---
        // המחשב יושב סביב X=0, Z=-0.67. בנינו שולחן ברוחב 14 ובעומק 10.
        scene.geometries.add(new geometries.impl.Polygon(
                new Point(-7, 0.01, 4.33),
                new Point(7, 0.01, 4.33),
                new Point(7, 0.01, -5.67),
                new Point(-7, 0.01, -5.67))
                .setEmission(new Color(5, 10, 15))
                .setMaterial(glassTableMat));

        // --- הנדסת המצלמה ---
        Point target = new Point(0, 1.58, -0.67);

        // וקטור הכיוון המקורי כפול 0.65 כדי להתקרב בדיוק על אותו ציר!
        Vector angleOffset = new Vector(8, 5.42, 8.67).scale(0.8);
        Point cameraLoc = target.add(angleOffset);

        System.out.println("Rendering final masterpiece...");
        Camera.getBuilder()
                .setAdaptiveGridParameters(15, 8)
                .setRayTracer(scene, RayTracerType.ADAPTIVE_GRID)
                .setLocation(cameraLoc)
                .setDirection(target.subtract(cameraLoc), Vector.AXIS_Y)
                .setVpSize(8, 8).setVpDistance(10)
                .setResolution(4000, 4000)
                .setMultithreading(-1)
                .setAdaptiveSuperSampling(3, 5.0)
                .setDebugPrint(0.1)
                .setAntiAliasing(25, new Jittered())
                .build()
                .renderImage()
                .writeToImage("Laptop_Final_Masterpiece_ASS_AG");

        System.out.println("Done!");
    }

    @Test
    public void testLaptopOnGlassTableFinalAngle() {
        Scene scene = new Scene("Laptop on Glass Desk")
                .setBackground(new Color(10, 15, 25)) // רקע סטודיו כהה ואלגנטי
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(10)));

        // תאורת שמש מוחלשת שלא שורפת את המסך
        scene.lights.add(new DirectionalLight(new Color(255, 250, 240).reduce(5), new Vector(1, -1, 1)));

        // ספוט לייט עדין כדי לתת השתקפות על השולחן
        scene.lights.add(new PointLight(new Color(100, 100, 150), new Point(0, 10, 2))
                .setKl(0.01).setKq(0.001));

        System.out.println("Parsing OBJ file...");
        scene.ObjParser parser = new scene.ObjParser();
        scene.geometries.add(parser.parse("models/Laptop_High-Polay_HP_BI_2_obj.obj"));

        // --- חומר זכוכית חלבית לשולחן ---
        Material glassTableMat = new Material()
                .setKd(0.05).setKs(0.8).setShininess(100)
                .setKt(0.7)
                .setKr(0.2);
                //.setBlurRadius(0.05)
                //.setBlurGlassRays(12);

        // --- שולחן זכוכית (פוליגון מלבני ומיושר במקום מישור אינסופי) ---
        scene.geometries.add(new geometries.impl.Polygon(
                new Point(-7, 0.01, 4.33),
                new Point(7, 0.01, 4.33),
                new Point(7, 0.01, -5.67),
                new Point(-7, 0.01, -5.67))
                .setEmission(new Color(5, 10, 15))
                .setMaterial(glassTableMat));


        // --- הנדסת המצלמה ---
        // 1. הסטת המטרה ממרכז המקלדת אל מרכז המסך (גבוה יותר, עמוק יותר, ומעט שמאלה)
        Point target = new Point(-0.5, 2.8, -2.0);

        // 2. זווית נמוכה יותר וזום-אין ממוקד כדי למלא את הפריים ולחתוך את קצה השולחן למטה
        Vector angleOffset = new Vector(-9, 3.2, 7.5).scale(1.15);
        Point cameraLoc = target.add(angleOffset);

        System.out.println("Rendering final masterpiece...");
        Camera.getBuilder()
                .setAdaptiveGridParameters(15, 8)
                .setRayTracer(scene, RayTracerType.ADAPTIVE_GRID)
                .setLocation(cameraLoc)
                .setDirection(target.subtract(cameraLoc), Vector.AXIS_Y)
                .setVpSize(8, 8).setVpDistance(10)
                .setResolution(4000, 4000)
                .setMultithreading(-1)
                .setAdaptiveSuperSampling(3, 5.0)
                .setDebugPrint(0.1)
                .setAntiAliasing(25, new Jittered())
                .build()
                .renderImage()
                .writeToImage("Laptop_Final_Masterpiece2_ASS_AG");

        System.out.println("Done!");
    }
}