package renderer;

import geometries.impl.Plane;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.ObjParser;

/**
 * Final performance testing class for MP2.
 * Measures render times across different configurations of acceleration and multi-threading.
 */
public class FinalPerformanceTest {

    private static Scene SCENE;
    private static final Point CAMERA_LOC = new Point(8, 7, 8);
    private static final Point TARGET = new Point(0, 1.58, -0.67);

    // --- בניית הסצנה פעם אחת בלבד עבור כל הטסטים ---
    @BeforeAll
    public static void buildScene() {
        System.out.println("Building shared scene and parsing OBJ... Please wait.");
        SCENE = new Scene("Laptop Performance Test")
                .setBackground(new Color(10, 15, 25))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(10)));

        SCENE.lights.add(new DirectionalLight(
                new Color(255, 245, 230).reduce(5),
                new Vector(1, -1, 1)));

        SCENE.lights.add(new PointLight(new Color(150, 150, 200), new Point(0, 15, 0))
                .setKl(0.01).setKq(0.001));

        ObjParser parser = new ObjParser();
        SCENE.geometries.add(parser.parse("models/Laptop_High-Polay_HP_BI_2_obj.obj"));

        Material glassTableMat = new Material()
                .setKd(0.05).setKs(0.8).setShininess(100)
                .setKt(0.7)
                .setKr(0.2)
                .setBlurRadius(0.05)
                .setBlurGlassRays(24);

        SCENE.geometries.add(new geometries.impl.Polygon(
                new Point(-7, 0.01, 4.33),
                new Point(7, 0.01, 4.33),
                new Point(7, 0.01, -5.67),
                new Point(-7, 0.01, -5.67))
                .setEmission(new Color(5, 10, 15))
                .setMaterial(glassTableMat));

        System.out.println("Scene is ready!");
    }

    // --- המצלמה הבסיסית שתשמש כקוד בסיס (Base Configuration) לכל הטסטים ---
    private Camera.Builder getBaseCameraBuilder() {
        return Camera.getBuilder()
                .setLocation(CAMERA_LOC)
                .setDirection(TARGET.subtract(CAMERA_LOC), Vector.AXIS_Y)
                .setVpSize(10, 10).setVpDistance(10)
                .setResolution(4000, 4000) // רזולוציה אחידה למדידה
                .setDebugPrint(0.1)
                // שיפור MP1 חייב להיות דלוק תמיד (בהנחה ש-Jittered זמין)
                .setAntiAliasing(25, new primitives.Jittered());
    }

    // =======================================================================
    // 1. שני שיפורי ההאצה כבויים + ריבוי תהליכונים כבוי (Baseline)
    // =======================================================================
    @Test
    public void test01_NoAccel_NoMT() {
        System.out.println("\n--- Test 1: No Accel, No MT ---");
        Camera camera = getBaseCameraBuilder()
                .setRayTracer(SCENE, RayTracerType.SIMPLE) // ללא גריד
                .setAdaptiveSuperSampling(0, 0) // ללא ASS
                .setMultithreading(0) // ללא MT
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Test01_NoAccel_NoMT");
    }

    // =======================================================================
    // 2. שני שיפורי ההאצה כבויים + ריבוי תהליכונים פעיל (MT Isolation)
    // =======================================================================
    @Test
    public void test02_NoAccel_WithMT() {
        System.out.println("\n--- Test 2: No Accel, With MT ---");
        Camera camera = getBaseCameraBuilder()
                .setRayTracer(SCENE, RayTracerType.SIMPLE) // ללא גריד
                .setAdaptiveSuperSampling(0, 0) // ללא ASS
                .setMultithreading(-2) // MT פעיל אוטומטית (-2)
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Test02_NoAccel_WithMT");
    }

    // =======================================================================
    // 3. שיפור האצה 1 פעיל (Regular Grid), השני כבוי + ריבוי תהליכונים כבוי
    // =======================================================================
    @Test
    public void test03_RegularGrid_NoASS_NoMT() {
        System.out.println("\n--- Test 3: Regular Grid ON, ASS OFF, No MT ---");
        Camera camera = getBaseCameraBuilder()
                .setGridResolution(50) // קביעת 50x50x50 ווקסלים
                .setRayTracer(SCENE, RayTracerType.REGULAR_GRID) // רשת אחידה פעילה
                .setAdaptiveSuperSampling(0, 0) // ASS כבוי
                .setMultithreading(0) // MT כבוי
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Test03_RegularGrid_NoASS_NoMT");
    }

    // =======================================================================
    // 4. שיפור האצה 2 פעיל (ASS), הראשון כבוי + ריבוי תהליכונים כבוי
    // =======================================================================
    @Test
    public void test04_NoGrid_WithASS_NoMT() {
        System.out.println("\n--- Test 4: Grid OFF, ASS ON, No MT ---");
        Camera camera = getBaseCameraBuilder()
                .setRayTracer(SCENE, RayTracerType.SIMPLE) // ללא גריד
                .setAdaptiveSuperSampling(3, 5.0) // ASS פעיל! (עומק 3, טולרנס 5)
                .setMultithreading(0) // MT כבוי
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Test04_NoGrid_WithASS_NoMT");
    }

    // =======================================================================
    // 5. שיפור האצה 1 פעיל (Adaptive Grid), השני כבוי + ריבוי תהליכונים כבוי
    // =======================================================================
    @Test
    public void test05_AdaptiveGrid_NoASS_NoMT() {
        System.out.println("\n--- Test 5: Adaptive Grid ON, ASS OFF, No MT ---");
        Camera camera = getBaseCameraBuilder()
                .setAdaptiveGridParameters(15, 6) // עד 15 גופים לתא, עומק מקסימלי 6
                .setRayTracer(SCENE, RayTracerType.ADAPTIVE_GRID) // רשת מסתגלת פעילה
                .setAdaptiveSuperSampling(0, 0) // ASS כבוי
                .setMultithreading(0) // MT כבוי
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Test05_AdaptiveGrid_NoASS_NoMT");
    }

    // =======================================================================
    // 6. כל התותחים עובדים: Adaptive Grid + ASS + MT
    // =======================================================================
    @Test
    public void test06_AllFeatures_WithMT() {
        System.out.println("\n--- Test 6: Adaptive Grid ON, ASS ON, With MT ---");
        Camera camera = getBaseCameraBuilder()
                .setAdaptiveGridParameters(15, 6) // רשת מסתגלת
                .setRayTracer(SCENE, RayTracerType.ADAPTIVE_GRID)
                .setAdaptiveSuperSampling(3, 5.0) // ASS פעיל
                .setMultithreading(-2) // MT פעיל
                .build();

        long startTime = System.currentTimeMillis();
        camera.renderImage();
        long endTime = System.currentTimeMillis();

        System.out.println("Render Time: " + (endTime - startTime) / 1000.0 + " seconds");
        camera.writeToImage("Test06_AllFeatures_WithMT");
    }
}