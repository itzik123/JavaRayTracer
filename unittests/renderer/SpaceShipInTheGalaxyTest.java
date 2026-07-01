package renderer;

import geometries.impl.Geometries;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.ObjParser;
import scene.Scene;

public class SpaceShipInTheGalaxyTest {
    /**
     * Produce an epic, procedurally generated galaxy scene V6.
     * Features: Zoomed-out camera, vibrant colors, white central halo, and background star clusters.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testMilkyWayEpicGalaxyV6() {
        Scene scene = new Scene("Milky Way Galaxy V6")
                .setAmbientLight(new AmbientLight(new Color(2, 2, 6))); // רקע חשוך יותר

        Material starMat = new Material().setKd(0.2).setKs(0.8).setShininess(400);
        Material dustMat = new Material().setKd(0.8).setKs(0.2).setShininess(10);

        java.util.Random rnd = new java.util.Random(42);

        // 1. זוויות להטיה
        double tiltX = Math.toRadians(65);
        double tiltZ = Math.toRadians(45);

        double cosX = Math.cos(tiltX);
        double sinX = Math.sin(tiltX);
        double cosZ = Math.cos(tiltZ);
        double sinZ = Math.sin(tiltZ);

        // --- 1. השמש המרכזית, הילה וענן הליבה ---
        Point centerPoint = applyRotation(0, 0, 0, cosX, sinX, cosZ, sinZ, -900);

        // השמש הגדולה
        scene.geometries.add(new Sphere(centerPoint, 38D)
                .setEmission(new Color(255, 245, 220)) // לבן בוהק מאוד עם נטייה לזהב
                .setMaterial(starMat));

        // אור מרכזי מהשמש של הגלקסיה (נשאר כמו שהיה)
        scene.lights.add(new PointLight(new Color(1000, 800, 600), centerPoint)
                .setKl(0.00005).setKq(0.000005));

        // --- הוספה קולנועית: שטיפת צבע גלובלית (זול מאוד חישובית!) ---
        // תאורת אווירה סגולה-מגנטה המגיעה מכיוון מרכז הגלקסיה (תצבע את הצד הימני של החללית)
        scene.lights.add(new DirectionalLight(new Color(150, 50, 200), new Vector(-1, -0.2, -1)));

        // תאורת אווירה כחולה עמוקה המגיעה מהאופק (תצבע את הצד השמאלי והאחורי של החללית)
        scene.lights.add(new DirectionalLight(new Color(20, 50, 180), new Vector(1, 0.5, 0.5)));

        // הילת כוכבים לבנים/תכלת קטנים סביב הליבה (באותה זווית של הגלקסיה)
        for (int i = 0; i < 400; i++) {
            double r = 40 + rnd.nextDouble() * 30; // קרוב יחסית לשמש
            double theta = rnd.nextDouble() * 2 * Math.PI;
            double phi = rnd.nextDouble() * Math.PI;

            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = (rnd.nextDouble() - 0.5) * 15; // דיסקה שטוחה להילה
            double z = r * Math.cos(phi);

            Point p = applyRotation(x, y, z, cosX, sinX, cosZ, sinZ, -900);

            scene.geometries.add(new Sphere(p, 0.8 + rnd.nextDouble() * 2)
                    .setEmission(new Color(230 + rnd.nextInt(25), 240 + rnd.nextInt(15), 255)) // לבן-תכלת חזק
                    .setMaterial(starMat));
        }

        // ענן חלקיקים חמים וסמיכים סביב הליבה
        for (int i = 0; i < 300; i++) {
            double r = 40 + rnd.nextDouble() * 60;
            double theta = rnd.nextDouble() * 2 * Math.PI;
            double phi = rnd.nextDouble() * Math.PI;

            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = (rnd.nextDouble() - 0.5) * 40;
            double z = r * Math.cos(phi);

            Point p = applyRotation(x, y, z, cosX, sinX, cosZ, sinZ, -900);
            Color coreColor = new Color(255, 60 + rnd.nextInt(60), 10); // כתום-אדום עז

            scene.geometries.add(new Sphere(p, 1.5 + rnd.nextDouble() * 3)
                    .setEmission(coreColor)
                    .setMaterial(starMat));
        }

        int numArms = 4;

        // --- 2. זרועות ספירלה ראשיות ומשניות ---
        int objectsPerMainArm = 1200;
        double maxMainRadius = 800;

        for (int arm = 0; arm < numArms; arm++) {
            double armOffset = (2 * Math.PI / numArms) * arm;
            for (int i = 0; i < objectsPerMainArm; i++) {
                double distance = 60 + (maxMainRadius - 60) * Math.pow((i / (double) objectsPerMainArm), 0.85);
                double angle = armOffset - (distance / 160.0) + (rnd.nextDouble() - 0.5) * 0.4;
                generateGalaxyObjectV6(scene, distance, angle, cosX, sinX, cosZ, sinZ, rnd, starMat, dustMat);
            }
        }

        int objectsPerMiniArm = 600;
        double maxMiniRadius = 450;

        for (int arm = 0; arm < numArms; arm++) {
            double armOffset = (2 * Math.PI / numArms) * arm + (Math.PI / numArms);
            for (int i = 0; i < objectsPerMiniArm; i++) {
                double distance = 70 + (maxMiniRadius - 70) * Math.pow((i / (double) objectsPerMiniArm), 0.9);
                double angle = armOffset - (distance / 130.0) + (rnd.nextDouble() - 0.5) * 0.6;
                generateGalaxyObjectV6(scene, distance, angle, cosX, sinX, cosZ, sinZ, rnd, starMat, dustMat);
            }
        }

        // --- 3. רקע עמוק וצבירי כוכבים ---
        // פיזור רגיל של כוכבי רקע (כחולים ולבנים)
        for (int i = 0; i < 1500; i++) {
            double x = (rnd.nextDouble() - 0.5) * 9000;
            double y = (rnd.nextDouble() - 0.5) * 9000;
            double z = -1500 - rnd.nextDouble() * 5000;

            int shade = 180 + rnd.nextInt(75);
            Color starColor = rnd.nextBoolean() ? new Color(shade, shade, 255) : new Color(255, 255, 255);

            scene.geometries.add(new Sphere(new Point(x, y, z), 2 + rnd.nextDouble() * 5)
                    .setEmission(starColor));
        }

        // יצירת צבירי כוכבים (Star Clusters) מרוחקים למראה מציאותי ועמוק
        for (int c = 0; c < 6; c++) { // 6 צבירים שונים בחלל
            double clusterX = (rnd.nextDouble() - 0.5) * 6000;
            double clusterY = (rnd.nextDouble() - 0.5) * 6000;
            double clusterZ = -2000 - rnd.nextDouble() * 3000;

            for(int i = 0; i < 70; i++) {
                double cx = clusterX + rnd.nextGaussian() * 150; // פיזור נורמלי סביב המרכז
                double cy = clusterY + rnd.nextGaussian() * 150;
                double cz = clusterZ + rnd.nextGaussian() * 150;

                scene.geometries.add(new Sphere(new Point(cx, cy, cz), 3 + rnd.nextDouble() * 6)
                        .setEmission(new Color(200 + rnd.nextInt(55), 220 + rnd.nextInt(35), 255)));
            }
        }

        scene.lights.add(new SpotLight(new Color(80, 80, 150), new Point(0, 400, 300), new Vector(0, -1, -3))
                .setKl(0.0001).setKq(0.00001));

        // === הוספת החללית לסצנת הגלקסיה ===

// וקטור האופסט:
// X=15 (ימינה), Y=-12 (למטה), Z=940 (קרוב מאוד למצלמה שנמצאת ב-1000, כדי שהיא תיראה בגודל הנכון)
        Vector spaceshipOffset = new Vector(15, -12, 940);

// טעינת המודל עם האופסט שהוספת ל-ObjParser
        ObjParser parser = new ObjParser();
        Geometries spaceship = parser.parse("models/E 45 Aircraft_obj.obj", spaceshipOffset); // או כל דרך אחרת שבה העברת את האופסט

// הוספת החללית לסצנה
        scene.geometries.add(spaceship);

        // --- רינדור ---
        Camera.getBuilder()
                .setAdaptiveGridParameters(30, 8)
                .setRayTracer(scene, RayTracerType.ADAPTIVE_GRID)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                // הגדלנו את שדה הראייה ל-350 כדי "להתרחק" ולראות שוליים שחורים
                .setVpSize(1000, 1000).setVpDistance(1000)
                .setResolution(2000, 2000) // !!! שנה כאן ל-5000 אחרי הטסט !!!
                .setAdaptiveSuperSampling(4, 5.0)
                .setDebugPrint(0.1)
                .setMultithreading(4)
                .build()
                .renderImage()
                .writeToImage("SpaceShipMilkyWayGalaxy");
    }

    /**
     * פונקציית עזר ליצירת חלקיקי אבק כוכבים בצבעים עזים במיוחד (Vibrant)
     */
    private void generateGalaxyObjectV6(Scene scene, double distance, double angle,
                                        double cosX, double sinX, double cosZ, double sinZ,
                                        java.util.Random rnd, Material starMat, Material dustMat) {

        double stretchMajor = 1.7;
        double squeezeMinor = 0.5;

        double flatX = distance * stretchMajor * Math.cos(angle);
        double flatY = (rnd.nextDouble() - 0.5) * 35;
        double flatZ = distance * squeezeMinor * Math.sin(angle);

        Color emission;
        if (distance < 250) {
            emission = new Color(255, 20 + rnd.nextInt(50), 10); // אדום ניאון וכתום עז
        } else if (distance < 550) {
            emission = new Color(200 + rnd.nextInt(55), 10 + rnd.nextInt(20), 220 + rnd.nextInt(35)); // סגול ומגנטה צועק
        } else {
            emission = new Color(10 + rnd.nextInt(30), 20 + rnd.nextInt(40), 255); // כחול עמוק ונקי
            if (rnd.nextDouble() > 0.85) {
                emission = new Color(255, 30 + rnd.nextInt(40), 20); // קצוות באדום עז
            }
        }

        Point p = applyRotation(flatX, flatY, flatZ, cosX, sinX, cosZ, sinZ, -900);

        scene.geometries.add(new Sphere(p, 0.8 + rnd.nextDouble() * 3.5D)
                .setEmission(emission)
                .setMaterial(rnd.nextBoolean() ? starMat : dustMat));

        if (rnd.nextDouble() > 0.98) {
            scene.lights.add(new PointLight(emission.scale(1.5), p)
                    .setKl(0.001).setKq(0.0001));
        }
    }

    private Point applyRotation(double x, double y, double z, double cosX, double sinX, double cosZ, double sinZ, double zOffset) {
        double y1 = y * cosX - z * sinX;
        double z1 = y * sinX + z * cosX;
        double x2 = x * cosZ - y1 * sinZ;
        double y2 = x * sinZ + y1 * cosZ;
        return new Point(x2, y2, z1 + zOffset);
    }
}
