package geometries.impl;

import geometries.impl.Triangle;
import primitives.Color;
import primitives.Point;
import primitives.Texture;
import primitives.UV;
import primitives.Vector;

/**
 * A textured extension of a mathematical Triangle.
 * Uses Barycentric coordinates to map a 3D intersection point to a 2D texture pixel.
 */
public class TexturedTriangle extends Triangle {

    private final Point _p1, _p2, _p3;
    private final UV _uv1, _uv2, _uv3;
    private final Texture _texture;
    private final double _totalArea; // חישוב מראש של שטח המשולש לטובת יעילות

    public TexturedTriangle(Point p1, Point p2, Point p3,
                            UV uv1, UV uv2, UV uv3,
                            Texture texture) {
        super(p1, p2, p3); // הגיאומטריה מנוהלת על ידי מחלקת האם!
        this._p1 = p1;
        this._p2 = p2;
        this._p3 = p3;
        this._uv1 = uv1;
        this._uv2 = uv2;
        this._uv3 = uv3;
        this._texture = texture;

        // חישוב שטח המשולש הגדול (אורך מכפלה וקטורית חלקי 2)
        Vector v1 = _p2.subtract(_p1);
        Vector v2 = _p3.subtract(_p1);
        this._totalArea = v1.crossProduct(v2).length() / 2.0;
    }

    /**
     * This is where the magic happens!
     * We override the getEmission method to return the specific pixel color
     * based on the exact intersection point.
     */
    @Override
    public Color getEmission(Point intersectionPoint) {
        // מציאת השטחים הפנימיים (המשקלים הבריצנטריים)
        double w1 = calculateSubArea(intersectionPoint, _p2, _p3) / _totalArea;
        double w2 = calculateSubArea(intersectionPoint, _p1, _p3) / _totalArea;
        double w3 = calculateSubArea(intersectionPoint, _p1, _p2) / _totalArea;

        // אינטרפולציה של ה-UV בעזרת המשקלים
        double interpolatedU = w1 * _uv1.u + w2 * _uv2.u + w3 * _uv3.u;
        double interpolatedV = w1 * _uv1.v + w2 * _uv2.v + w3 * _uv3.v;

        // שאיבת הצבע מהתמונה!
        return _texture.getColor(interpolatedU, interpolatedV);
    }

    /**
     * פונקציית עזר לחישוב שטח של משולש פנימי בין נקודת החיתוך לשני קודקודים
     */
    private double calculateSubArea(Point p, Point vertexA, Point vertexB) {
        try {
            Vector vA = vertexA.subtract(p);
            Vector vB = vertexB.subtract(p);
            return vA.crossProduct(vB).length() / 2.0;
        } catch (IllegalArgumentException e) {
            // במקרה שנקודת החיתוך נופלת בדיוק על אחד הקצוות (וקטור אפס), השטח הוא 0
            return 0.0;
        }
    }
}