package renderer;

import org.junit.jupiter.api.Test;
import geometries.impl.*;
import lighting.*;
import primitives.*;
import scene.Scene;

import static java.lang.System.load;
import static scene.SceneXMLParser.parse;

/**
 * Test class demonstrating the Anti-Aliasing (Super-sampling) improvement.
 */
public class AntiAliasingTest {

    @Test
    public void testAntiAliasingShowcase() {
        // יצירת סצנה עם רקע כהה ליצירת קונטרסט גבוה שמדגיש את הפיקסליזציה
        Scene scene = new Scene("Anti-Aliasing Showcase")
                .setBackground(new Color(10, 10, 15))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(5)));

        // הגדרת חומרים
        Material matShiny = new Material().setKd(0.5).setKs(0.5).setShininess(60).setKr(0.2);
        Material matMatte = new Material().setKd(0.8).setKs(0.2).setShininess(30);
        Material matGlass = new Material().setKd(0.2).setKs(0.2).setShininess(30).setKt(0.8);

        // --- הוספת 10 גופים גיאומטריים ---
        scene.geometries.add(
                // 1. משטח (רצפה) מבריק
                new Plane(new Point(0, -10, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(20, 20, 20)).setMaterial(matShiny),

                // 2-4. משולשים היוצרים רקע אלכסוני (אלכסונים הם הטובים ביותר להדגמת Aliasing)
                new Triangle(new Point(-30, -10, -80), new Point(0, 30, -80), new Point(30, -10, -80))
                        .setEmission(new Color(50, 0, 0)).setMaterial(matMatte),
                new Triangle(new Point(-50, -10, -70), new Point(-20, 20, -70), new Point(0, -10, -70))
                        .setEmission(new Color(0, 50, 0)).setMaterial(matMatte),
                new Triangle(new Point(0, -10, -70), new Point(20, 20, -70), new Point(50, -10, -70))
                        .setEmission(new Color(0, 0, 50)).setMaterial(matMatte),

                // 5. כדור זכוכית מרכזי
                new Sphere(new Point(0, 0, -50), 10)
                        .setEmission(new Color(0, 0, 0)).setMaterial(matGlass),

                // 6-10. חמישה כדורים צבעוניים שמקיפים את המרכז
                new Sphere(new Point(-15, 0, -45), 4).setEmission(new Color(255, 100, 100)).setMaterial(matShiny),
                new Sphere(new Point(15, 0, -45), 4).setEmission(new Color(100, 255, 100)).setMaterial(matShiny),
                new Sphere(new Point(0, 15, -45), 4).setEmission(new Color(100, 100, 255)).setMaterial(matShiny),
                new Sphere(new Point(-10, -10, -40), 3).setEmission(new Color(255, 255, 100)).setMaterial(matShiny),
                new Sphere(new Point(10, -10, -40), 3).setEmission(new Color(255, 100, 255)).setMaterial(matShiny)
        );

        // --- הוספת 3 מקורות אור ---
        scene.lights.add(
                new DirectionalLight(new Color(50, 50, 50), new Vector(1, -1, -1)) // אור שמש כללי
        );
        scene.lights.add(
                new PointLight(new Color(100, 100, 0), new Point(-20, 20, -20)) // תאורה נקודתית צהובה
                        .setKl(0.0001).setKq(0.00005)
        );
        scene.lights.add(
                new SpotLight(new Color(200, 200, 200), new Point(0, 30, -10), new Vector(0, -1, -3)) // ספוט לבן ממוקד
                        .setKl(0.0001).setKq(0.00005).setNarrowBeam(1)
        );

        // בניית המצלמה (ללא בנייה סופית - Build) כדי לשכפל אותה לטסטים
        Camera.Builder cameraBuilder = Camera.getBuilder()
                .setLocation(new Point(0, 10, 20))
                .setDirection(new Point(0, 0, -50))
                .setVpDistance(100)
                .setVpSize(150, 150)
                .setResolution(600, 600)
                .setRayTracer(scene, RayTracerType.SIMPLE);

        // --- הרצה 1: ללא החלקת עקומות (Aliasing Off) ---
        long start1 = System.currentTimeMillis();
        cameraBuilder.build()
                .renderImage()
                .writeToImage("AntiAliasing_Showcase_Off");
        long time1 = System.currentTimeMillis() - start1;
        System.out.println("Render time WITHOUT Anti-Aliasing: " + time1 / 1000.0 + " seconds");

        // --- הרצה 2: עם החלקת עקומות (Anti-Aliasing On) ---
        // שימוש בתבנית Jittered עם רזולוציה 9x9 (81 קרניים לפיקסל)
        long start2 = System.currentTimeMillis();

        // 1. הגדרת הפרמטרים ובניית המצלמה ושמירתה למשתנה
        Camera camera = cameraBuilder.setAntiAliasing(9, new Jittered())
                .setMultithreading(-2)
                .setDebugPrint(0.1)
                .build();

        camera.renderImage();

        System.out.println();

        camera.writeToImage("AntiAliasing_Showcase_On");

        long time2 = System.currentTimeMillis() - start2;
        System.out.println("Render time WITH Anti-Aliasing (9x9 Jittered): " + time2 / 1000.0 + " seconds");
    }

    @Test
    void testCrystalGallerySideView() throws Exception {
        Camera.getBuilder()
                // שים לב לעדכון הנתיב + קריאה מפורשת למחלקה (למקרה שאין Import סטטי)
                .setRayTracer(scene.SceneXMLParser.parse("stage8CrystalGallery.xml"), RayTracerType.SIMPLE)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620).setVpDistance(1000)
                .setResolution(1000, 1000)
                .setAntiAliasing(9, new Jittered())
                .setMultithreading(-2) // מפעיל את כל הליבות של המעבד שלך
                .setDebugPrint(0.1)    // מדפיס אחוזי התקדמות כל 10%
                .build()
                .renderImage()
                .writeToImage("xml_crystalGallery_sideView_glossy_diffuse");
    }
}