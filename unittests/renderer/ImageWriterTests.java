package renderer;

import org.junit.jupiter.api.Test;
import primitives.Color;

public class ImageWriterTests {
    static final int HEIGHT = 501;
    static final int WIDTH = 801;
    static final int CUBE_SIZE = 50;
    static final Color GRID_COLOR =  new Color(20,225,129);
    static final Color BACKGROUND_COLOR = new Color(124,76,98);


    @Test
    void testImageWriter() {
        ImageWriter imageWriter = new ImageWriter(WIDTH,HEIGHT);
        for (int i = 0; i < HEIGHT; ++i){
            for(int j = 0; j < WIDTH; ++j){
                imageWriter.writePixel(j, i, (i % CUBE_SIZE == 0 || j % CUBE_SIZE == 0) ? GRID_COLOR : BACKGROUND_COLOR);
            }
        }
        imageWriter.writeToImage("test01");
    }






}
