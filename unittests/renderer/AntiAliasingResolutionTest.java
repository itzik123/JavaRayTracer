package renderer;

import geometries.impl.*;
import lighting.*;
import org.junit.jupiter.api.Test;
import primitives.*;
import scene.Scene;

/**
 * Advanced Benchmark for Anti-Aliasing featuring high-frequency details,
 * thin diagonal objects, refractions, and reflections.
 */
public class AntiAliasingResolutionTest {

    @Test
    public void testAACageScene() {
        Scene scene = new Scene("AA Advanced Showcase")
                .setBackground(new Color(15, 15, 20))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(10)));

        // --- הגדרת חומרים מתוחכמים ---
        Material mirrorFloor = new Material().setKd(0.2).setKs(0.5).setShininess(40).setKr(0.5); // חצי מראה
        Material redShiny = new Material().setKd(0.4).setKs(0.6).setShininess(80).setKr(0.2); // מתכת אדומה מבריקה
        Material pureGlass = new Material().setKd(0.0).setKs(0.8).setShininess(60).setKt(0.9); // זכוכית שקופה כמעט לגמרי
        Material matteBars = new Material().setKd(0.8).setKs(0.2).setShininess(20); // פלסטיק מאט לסורגים

        // --- הרכבת הסצנה ---

        // 1. רצפת מראה כהה המדגישה השתקפויות של אלכסונים
        scene.geometries.add(new Plane(new Point(0, -20, 0), new Vector(0, 1, 0))
                .setEmission(new Color(10, 10, 10)).setMaterial(mirrorFloor));

        // 2. כדור מתכתי אדום (ימין)
        scene.geometries.add(new Sphere(new Point(18, -2, -30), 18)
                .setEmission(new Color(100, 20, 20)).setMaterial(redShiny));

        // 3. כדור זכוכית (שמאל) - ישבור את קרני האור ויעוות את הסורגים
        scene.geometries.add(new Sphere(new Point(-18, -2, -30), 18)
                .setEmission(new Color(0, 0, 10)).setMaterial(pureGlass));

        // 4. "הכלוב" - סדרה של סורגים (גלילים) דקים ואלכסוניים בחזית הסצנה
        // קווים אלכסוניים יוצרים את ה-Aliasing החזק ביותר.
        for (int i = -3; i <= 3; i++) {
            scene.geometries.add(new Cylinder(0.6,
                    new Ray(new Point(-50, i * 10 - 5, 20), new Vector(1, 0.4, 0)), 100)
                    .setEmission(new Color(40, 40, 40)).setMaterial(matteBars));
        }

        // --- תאורה פוטנציאלית לצללים מעניינים ---
        // ספוט-לייט חזק מצד שמאל למעלה, שזורק את הצל של הסורגים על הכדורים ועל הרצפה
        scene.lights.add(new SpotLight(new Color(400, 400, 400), new Point(-40, 50, 60), new Vector(1, -1, -1))
                .setKl(0.00001).setKq(0.000005));

        // תאורת מילוי כחולה חלשה מאחור כדי לתת נפח לזכוכית
        scene.lights.add(new PointLight(new Color(0, 50, 150), new Point(0, 10, -80))
                .setKl(0.0001).setKq(0.0001));

        // --- הגדרות מצלמה (800x800 כדי להבליט את הפיקסלים) ---
        Camera.Builder cameraBuilder = Camera.getBuilder()
                .setLocation(new Point(0, 10, 80))
                .setDirection(new Point(0, 0, -30))
                .setVpDistance(100)
                .setVpSize(100, 100)
                .setResolution(800, 800)
                .setMultithreading(-2) // שימוש בכל הליבות
                .setDebugPrint(0.1)    // הדפסת אחוזים בקונסולה
                .setRayTracer(scene, RayTracerType.SIMPLE);

        // מערך רזולוציות: 1 (ללא שיפור), 9 קרניים, 49 קרניים, 121 קרניים.
        int[] resolutions = {1, 3, 7, 11};

        for (int res : resolutions) {
            int totalRays = res * res;
            String testName = "AA_Cage_Test_" + totalRays + "_Rays";

            System.out.println("=========================================");
            System.out.println("Rendering: " + testName);

            if (res == 1) {
                cameraBuilder.setAntiAliasing(1, null);
            } else {
                cameraBuilder.setAntiAliasing(res, new Jittered());
            }

            long start = System.currentTimeMillis();

            Camera camera = cameraBuilder.build();
            camera.renderImage();

            System.out.println(); // שורת רווח כדי למנוע דריסה של האחוזים

            camera.writeToImage(testName);

            long time = System.currentTimeMillis() - start;
            System.out.println("Render time (" + totalRays + " rays): " + time / 1000.0 + " seconds\n");
        }
    }
}