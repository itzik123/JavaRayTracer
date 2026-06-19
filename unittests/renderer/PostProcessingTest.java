package renderer;

import geometries.impl.*;
import lighting.*;
import org.junit.jupiter.api.Test;
import primitives.*;
import renderer.postprocessing.BloomFilter;
import renderer.postprocessing.ColorGradingFilter;
import renderer.postprocessing.FXAAFilter;
import scene.Scene;

/**
 * Fixed Advanced test showcasing the true potential of the Bloom Post-Processing filter.
 */
public class PostProcessingTest {

    @Test
    public void testBloomFixed() {
        // רקע כהה
        Scene scene = new Scene("Bloom Showcase Fixed")
                .setBackground(new Color(10, 10, 15))
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255).reduce(20)));

        // רצפת מראה
        Material mirrorFloor = new Material().setKd(0.2).setKs(0.5).setShininess(50).setKr(0.5);

        // חומר מבריק מאוד לכדורים (כדי לייצר השתקפות אור בוהקת - Specular)
        Material glossy = new Material().setKd(0.5).setKs(0.8).setShininess(60);

        scene.geometries.add(
                new Plane(new Point(0, -15, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(5, 5, 5)).setMaterial(mirrorFloor)
        );

        // הוספת הכדורים - הפעם פליטת האור (Emission) מתונה, ואנחנו מסתמכים על התאורה החיצונית
        scene.geometries.add(
                new Sphere(new Point(-30, 5, -50), 12)
                        .setEmission(new Color(200, 50, 50)).setMaterial(glossy), // אדום
                new Sphere(new Point(0, 5, -50), 12)
                        .setEmission(new Color(50, 200, 50)).setMaterial(glossy), // ירוק
                new Sphere(new Point(30, 5, -50), 12)
                        .setEmission(new Color(50, 50, 200)).setMaterial(glossy)  // כחול
        );

        // מקור אור חיצוני סופר-עוצמתי מלמעלה שיאיר את הסצנה וייצר נקודות "שרופות" על הכדורים
        scene.lights.add(new PointLight(new Color(500, 500, 500), new Point(0, 50, 20))
                .setKl(0.00001).setKq(0.000005));

        Camera.Builder cameraBuilder = Camera.getBuilder()
                .setLocation(new Point(0, 20, 80))
                .setDirection(new Point(0, 0, -50))
                .setVpDistance(100)
                .setVpSize(150, 150)
                .setResolution(2000, 2000)
                .setMultithreading(-2)
                .setRayTracer(scene, RayTracerType.SIMPLE);

        // --- תמונה 1: רינדור רגיל (ללא Bloom) ---
        Camera cameraNoBloom = cameraBuilder.build();
        cameraNoBloom.renderImage();
        cameraNoBloom.writeToImage("Bloom_01_Off_Fixed");

        // --- תמונה 2: רינדור עם Bloom ---
        Camera cameraWithBloom = cameraBuilder
                .addPostProcessFilter(new BloomFilter()
                        .setThreshold(0.65f)   // סף שיתפוס את מוקד האור והברק
                        .setBlurRadius(20)     // טשטוש רחב
                        .setIntensity(1.0f))   // עוצמה מלאה
                .build();

        cameraWithBloom.renderImage();
        cameraWithBloom.writeToImage("Bloom_02_On_Fixed");

        Camera cameraFinal = cameraBuilder
                // 1. קודם כל Bloom עדין ורך
                .addPostProcessFilter(new BloomFilter()
                        .setThreshold(0.7f)
                        .setBlurRadius(20)
                        .setIntensity(0.85f))
                // 2. תיקוני צבע המותאמים לתמונה שחתוכה ל-255
                .addPostProcessFilter(new ColorGradingFilter()
                        .setExposure(1.0f)      // ללא שינוי חשיפה אגרסיבי
                        .setContrast(1.05f)     // ניגודיות עדינה מאוד
                        .setSaturation(1.15f)   // חיזוק צבעים קל
                        .setGamma(1.2f))        // <--- הקסם! מבהיר את הצללים בלי לפגוע בלבן המוחלט של ה-Bloom
                // 3. החלקת קצוות לסיום
                .addPostProcessFilter(new FXAAFilter().setJitterStrength(0.08f))
                .build();

        cameraFinal.renderImage();
        cameraFinal.writeToImage("Integration_Showcase_Perfect");
    }
}