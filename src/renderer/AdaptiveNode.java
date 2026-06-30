package renderer;

import geometries.api.Intersectable;
import primitives.AABB;
import primitives.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single node (voxel/octant) in an Adaptive Grid (Octree).
 */
public class AdaptiveNode {

    /** The spatial boundaries of this node */
    public final AABB box;

    /** List of geometries contained in this node. Null if this is an internal node. */
    public List<Intersectable> geometries;

    /** Array of 8 child nodes. Null if this is a leaf node. */
    public AdaptiveNode[] children;

    /**
     * Constructor for creating a new node with specific boundaries.
     * @param box the bounding box of the node
     */
    public AdaptiveNode(AABB box) {
        this.box = box;
        this.geometries = new ArrayList<>();
        this.children = null; // כברירת מחדל, תא נולד כ"עלה" ללא ילדים
    }

    /**
     * Adds a geometry to this node.
     * @param geo the geometry to add
     */
    public void addGeometry(Intersectable geo) {
        if (geometries != null) {
            this.geometries.add(geo);
        }
    }

    /**
     * Recursively splits this node into 8 sub-nodes if it contains too many geometries.
     * @param currentDepth the current depth of this node in the tree
     * @param maxGeometries the threshold of maximum geometries allowed in a single leaf
     * @param maxDepth the maximum allowed depth of the tree
     */
    public void split(int currentDepth, int maxGeometries, int maxDepth) {
        // תנאי עצירה: הגענו לעומק המקסימלי, או שהתא לא עמוס מספיק כדי להצדיק פיצול
        if (geometries.size() <= maxGeometries || currentDepth >= maxDepth) {
            return;
        }

        // חישוב נקודת האמצע של הקופסה (לצורך הפיצול ל-8)
        double midX = (box.min.getX() + box.max.getX()) / 2.0;
        double midY = (box.min.getY() + box.max.getY()) / 2.0;
        double midZ = (box.min.getZ() + box.max.getZ()) / 2.0;

        Point mid = new Point(midX, midY, midZ);

        children = new AdaptiveNode[8];

        // בניית 8 הקופסאות התוחמות של הילדים (מכסות את כל הקומבינציות המרחביות)
        Point[] mins = {
                box.min,
                new Point(midX, box.min.getY(), box.min.getZ()),
                new Point(box.min.getX(), midY, box.min.getZ()),
                new Point(midX, midY, box.min.getZ()),
                new Point(box.min.getX(), box.min.getY(), midZ),
                new Point(midX, box.min.getY(), midZ),
                new Point(box.min.getX(), midY, midZ),
                mid
        };

        Point[] maxs = {
                mid,
                new Point(box.max.getX(), midY, midZ),
                new Point(midX, box.max.getY(), midZ),
                new Point(box.max.getX(), box.max.getY(), midZ),
                new Point(midX, midY, box.max.getZ()),
                new Point(box.max.getX(), midY, box.max.getZ()),
                new Point(midX, box.max.getY(), box.max.getZ()),
                box.max
        };

        // אתחול 8 הילדים
        for (int i = 0; i < 8; i++) {
            children[i] = new AdaptiveNode(new AABB(mins[i], maxs[i]));
        }

        // מיון הגופים: כל גוף מהתא הנוכחי יועבר לילדים שהקופסה שלו חותכת
        for (Intersectable geo : geometries) {
            AABB geoBox = geo.getBoundingBox();
            if (geoBox != null) {
                for (int i = 0; i < 8; i++) {
                    if (children[i].box.isOverlapping(geoBox)) {
                        children[i].addGeometry(geo);
                    }
                }
            }
        }

        // חסכון קריטי בזיכרון: התא פוצל, ולכן כבר אינו "עלה". אפשר לזרוק את הרשימה שלו
        // כל תהליך הרינדור יעבוד רק עם גופים שנמצאים בעלים (קצות העץ).
        geometries = null;

        // קריאה רקורסיבית: תן לילדים לפצל את עצמם אם הם עמוסים מדי
        for (int i = 0; i < 8; i++) {
            children[i].split(currentDepth + 1, maxGeometries, maxDepth);
        }
    }
}