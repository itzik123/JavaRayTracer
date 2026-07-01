package renderer;

import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.ObjParser;

/**
 * A generic skeleton for performance testing any custom 3D model (OBJ file).
 * Includes an easy zoom-in/zoom-out mechanism.
 */
public class FinalPerformanceTest3 {

    // =======================================================================
    // ⚙️ משתנים שצריך לשנות עבור המודל שלך
    // =======================================================================

    // 1. נתיב לקובץ המודל שבתוך תיקיית models
    private static final String MODEL_FILE_PATH = "models/E 45 Aircraft_obj.obj";

    // 1. למרכז את האובייקט: הרמנו קצת את נקודת המבט כדי שהחללית תשב טוב בפריים
    private static final Point TARGET_POINT = new Point(-0.5, 0.5, 0);
    // 2. הזווית: הפכנו את Z למינוס כדי להסתכל מקדימה, והנמכנו את Y לזווית יותר "דרמטית"
    private static final Vector CAMERA_ANGLE = new Vector(1, 0.2, -1.5).normalize();

    // 3. זום אין: להקטין את המספר מ-10 כדי להתקרב לאובייקט
    private static final double CAMERA_DISTANCE = 7.0;


    private static Scene SCENE;

    // --- בניית הסצנה פעם אחת בלבד ---
    @BeforeAll
    public static void buildScene() {
        System.out.println("Loading custom model: " + MODEL_FILE_PATH);
        SCENE = new Scene("Custom Model Performance Test")
                .setBackground(new Color(25, 45, 85).reduce(2)) // רקע שמיים כחול-כהה
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(10)));

        // תאורה כללית כדי שהמודל לא יהיה חשוך לגמרי
        SCENE.lights.add(new DirectionalLight(new Color(150, 150, 150), new Vector(1, -1, -1)));
        SCENE.lights.add(new PointLight(new Color(255, 255, 255), new Point(-100, 100, 100)).setKl(0.001).setKq(0.0001));

        // טעינת המודל!
        ObjParser parser = new ObjParser();
        SCENE.geometries.add(parser.parse(MODEL_FILE_PATH));

        System.out.println("Model loaded and Scene is ready!");
    }

    // --- המצלמה הבסיסית עם מנגנון הזום ---
    private Camera.Builder getBaseCameraBuilder() {
        // המיקום מחושב אוטומטית: מתחילים במטרה, והולכים אחורה בכיוון הזווית, למרחק המבוקש
        Point cameraLoc = TARGET_POINT.add(CAMERA_ANGLE.scale(CAMERA_DISTANCE));
        Vector direction = TARGET_POINT.subtract(cameraLoc); // וקטור כיוון למטרה

        return Camera.getBuilder()
                .setLocation(cameraLoc)
                .setDirection(direction, Vector.AXIS_Y)
                .setVpSize(10, 10).setVpDistance(10) // יחסי גדלים סטנדרטיים למצלמה מרוחקת
                .setResolution(800, 800)
                .setDebugPrint(0.1)
                // שיפור MP1 (יריית 25 קרניים) חייב להיות דלוק כבסיס!
                .setAntiAliasing(25, new primitives.Jittered());
    }

    // =======================================================================
    // 1. שני שיפורי ההאצה כבויים + ריבוי תהליכונים כבוי (Baseline)
    // =======================================================================
    @Test
    public void test01_NoAccel_NoMT() {
        System.out.println("\n--- Custom Model: No Accel, No MT ---");
        Camera camera = getBaseCameraBuilder()
                .setRayTracer(SCENE, RayTracerType.SIMPLE)
                .setAdaptiveSuperSampling(0, 0)
                .setMultithreading(0)
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Custom_01_NoAccel_NoMT");
    }

    // =======================================================================
    // 2. שני שיפורי ההאצה כבויים + ריבוי תהליכונים פעיל (MT Isolation)
    // =======================================================================
    @Test
    public void test02_NoAccel_WithMT() {
        System.out.println("\n--- Custom Model: No Accel, With MT ---");
        Camera camera = getBaseCameraBuilder()
                .setRayTracer(SCENE, RayTracerType.SIMPLE)
                .setAdaptiveSuperSampling(0, 0)
                .setMultithreading(-2)
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Custom_02_NoAccel_WithMT");
    }

    // =======================================================================
    // 3. שיפור האצה 1 פעיל (Regular Grid), השני כבוי + ריבוי תהליכונים כבוי
    // =======================================================================
    @Test
    public void test03_RegularGrid_NoASS_NoMT() {
        System.out.println("\n--- Custom Model: Regular Grid ON, ASS OFF, No MT ---");
        Camera camera = getBaseCameraBuilder()
                .setGridResolution(50)
                .setRayTracer(SCENE, RayTracerType.REGULAR_GRID)
                .setAdaptiveSuperSampling(0, 0)
                .setMultithreading(0)
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Custom_03_RegularGrid_NoASS_NoMT");
    }

    // =======================================================================
    // 4. שיפור האצה 2 פעיל (ASS), הראשון כבוי + ריבוי תהליכונים כבוי
    // =======================================================================
    @Test
    public void test04_NoGrid_WithASS_NoMT() {
        System.out.println("\n--- Custom Model: Grid OFF, ASS ON, No MT ---");
        Camera camera = getBaseCameraBuilder()
                .setRayTracer(SCENE, RayTracerType.SIMPLE)
                .setAdaptiveSuperSampling(3, 5.0)
                .setMultithreading(0)
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Custom_04_NoGrid_WithASS_NoMT");
    }

    // =======================================================================
    // 5. שיפור האצה 1 פעיל (Adaptive Grid), השני כבוי + ריבוי תהליכונים כבוי
    // =======================================================================
    @Test
    public void test05_AdaptiveGrid_NoASS_NoMT() {
        System.out.println("\n--- Custom Model: Adaptive Grid ON, ASS OFF, No MT ---");
        Camera camera = getBaseCameraBuilder()
                .setAdaptiveGridParameters(15, 6)
                .setRayTracer(SCENE, RayTracerType.ADAPTIVE_GRID)
                .setAdaptiveSuperSampling(0, 0)
                .setMultithreading(0)
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Custom_05_AdaptiveGrid_NoASS_NoMT");
    }

    // =======================================================================
    // 6. כל התותחים עובדים: Adaptive Grid + ASS + MT
    // =======================================================================
    @Test
    public void test06_AllFeatures_WithMT() {
        System.out.println("\n--- Custom Model: Adaptive Grid ON, ASS ON, With MT ---");
        Camera camera = getBaseCameraBuilder()
                .setAdaptiveGridParameters(15, 6)
                .setRayTracer(SCENE, RayTracerType.ADAPTIVE_GRID)
                .setAdaptiveSuperSampling(3, 5.0)
                .setMultithreading(-2)
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Custom_06_AllFeatures_WithMT");
    }
}