package renderer;

import org.junit.jupiter.api.Test;
import renderer.postprocessing.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Utility test to apply Post-Processing filters to an already rendered external image.
 */
public class ExternalImageProcessingTest {

    @Test
    public void processGalaxyImage() {
        try {
            // 1. טעינת התמונה המקורית מהדיסק
            // ודא שהנתיב מדויק ושהתמונה נמצאת בתיקייה הנכונה
            File inputFile = new File("images/milkyWayEpicGalaxyV6.png");
            BufferedImage originalImage = ImageIO.read(inputFile);

            if (originalImage == null) {
                System.out.println("Could not load image. Check the file path.");
                return;
            }

            System.out.println("Loaded image: " + originalImage.getWidth() + "x" + originalImage.getHeight());

            // 2. בדיקת Bloom בנפרד
            System.out.println("Applying Bloom Only...");
            BloomFilter bloom = new BloomFilter()
                    .setThreshold(0.4f)   // סף נמוך בכוונה, כדי לתפוס גם את הכוכבים הפחות בהירים בזרועות
                    .setBlurRadius(40)    // רדיוס ענק! ייצור הילה ענקית סביב ליבת הגלקסיה
                    .setIntensity(0.9f);
            BufferedImage bloomImage = bloom.apply(originalImage);
            ImageIO.write(bloomImage, "png", new File("images/Galaxy_01_BloomOnly.png"));

            // 3. בדיקת Color Grading בנפרד
            System.out.println("Applying Color Grading Only...");
            ColorGradingFilter grading = new ColorGradingFilter()
                    .setExposure(1.1f)    // חשיפה קלה להארת הכוכבים החשוכים
                    .setContrast(1.15f)   // מעמיק את השחור של החלל
                    .setSaturation(1.6f)  // הקפצה מסיבית של הרוויה כדי להפוך את הוורוד והכחול לעשירים
                    .setGamma(1.2f);      // מבהיר את גווני הביניים
            BufferedImage gradedImage = grading.apply(originalImage);
            ImageIO.write(gradedImage, "png", new File("images/Galaxy_02_GradedOnly.png"));

            // 4. בדיקת FXAA בנפרד
            System.out.println("Applying FXAA Only...");
            FXAAFilter fxaa = new FXAAFilter()
                    .setJitterStrength(0.12f)
                    .setMaxSearchSteps(20);
            BufferedImage fxaaImage = fxaa.apply(originalImage);
            ImageIO.write(fxaaImage, "png", new File("images/Galaxy_03_FXAAOnly.png"));

            // 5. אינטגרציה מלאה: כל הצינור יחד!
            System.out.println("Applying Full Post-Processing Pipeline...");
            PostProcessingEngine engine = new PostProcessingEngine()
                    .addFilter(bloom)
                    .addFilter(grading)
                    .addFilter(fxaa);

            BufferedImage finalImage = engine.process(originalImage);
            ImageIO.write(finalImage, "png", new File("images/Galaxy_04_Final_Masterpiece.png"));

            System.out.println("Processing complete! Check your images folder.");

        } catch (IOException e) {
            System.out.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}