package geometries.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Unit tests for class {@link Polygon}.
 * The tests verify:
 * <ul>
 * <li>Polygon constructor validity</li>
 * <li>{@link Polygon#getNormal(Point)}</li>
 * <li>{@link Polygon#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
class PolygonTests {
   /** Default constructor to satisfy JavaDoc generator */
   PolygonTests() { /* to satisfy JavaDoc generator */ }

   /** Vertex (1,0,0) used in polygon tests */
   private static final Point  POINT_X       = new Point(1, 0, 0);
   /** Vertex (0,1,0) used in polygon tests */
   private static final Point  POINT_Y       = new Point(0, 1, 0);
   /** Vertex (0,0,1) used in polygon tests */
   private static final Point  POINT_Z       = new Point(0, 0, 1);

   /** Additional vertex used for valid polygon construction */
   private static final Point  POINT1        = new Point(-1, 1, 1);
   /** Point not in the polygon plane */
   private static final Point  POINT2        = new Point(0, 2, 2);
   /** Point that creates a concave polygon */
   private static final Point  POINT3        = new Point(0.5, 0.25, 0.5);
   /** Point located on one of the polygon edges */
   private static final Point  POINT4        = new Point(0, 0.5, 0.5);

   /**
    * Delta value for accuracy when comparing double values.
    */
   private static final double DELTA         = 1e-6;

   /** Error message for wrong plane intersection */
   private static final String ERROR_PLANE   = "ERROR: wrong intersection with plane";
   /** Error message for wrong polygon intersection */
   private static final String ERROR_POLYGON = "ERROR: wrong polygon intersection";

   /**
    * Test method for {@link Polygon#Polygon(Point...)}.
    * Verifies correct and incorrect polygon constructions.
    */
   @Test
   void testConstructor() {

      // ============ Equivalence Partitions Tests ==============

      // TC01: Correct convex quadrilateral with vertices in correct order
      assertDoesNotThrow(() -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT1),
                         "Failed constructing a correct polygon");

      // TC02: Wrong vertices order
      assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_Y, POINT_X, POINT1),
                   "Constructed a polygon with wrong order of vertices");

      // TC03: Vertices not in the same plane
      assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT2),
                   "Constructed a polygon with vertices that are not in the same plane");

      // TC04: Concave quadrilateral
      assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT3),
                   "Constructed a concave polygon");

      // =============== Boundary Values Tests ==================

      // TC11: Vertex on a side
      assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT4),
                   "Constructed a polygon with a vertex on a side");

      // TC12: Last point equals first point
      assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_Z),
                   "Constructed a polygon with duplicate first/last vertex");

      // TC13: Co-located points
      assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_Y),
                   "Constructed a polygon with co-located vertices");
   }

   /**
    * Test method for {@link Polygon#getNormal(Point)}.
    * Verifies that the returned normal vector is unit length and orthogonal
    * to all polygon edges.
    */
   @Test
   void testGetNormal() {
      // ============ Equivalence Partitions Tests ==============
      Point[] pts     =
         { POINT_Z, POINT_X, POINT_Y, POINT1 };
      Polygon polygon = new Polygon(pts);
      // Ensure method does not throw exception
      assertDoesNotThrow(() -> polygon.getNormal(POINT_Z), "getNormal() threw unexpected exception");
      Vector result = polygon.getNormal(POINT_Z);
      // Ensure |n| = 1
      assertEquals(1, result.length(), DELTA, "Polygon normal is not a unit vector");
      // Ensure normal is orthogonal to all edges
      for (int i = 0; i < pts.length; ++i) {
         Vector edge = pts[i].subtract(pts[i == 0 ? pts.length - 1 : i - 1]);
         assertEquals(0d, result.dotProduct(edge), DELTA, "Polygon normal is not orthogonal to an edge");
      }
   }

    /** Error message for polygon intersection failures */
    private static final String ERROR_POLYGON_INTERSECTION = "Wrong polygon intersection result";

    private static final Point V1 = new Point(1, 1, 0);
    private static final Point V2 = new Point(1, -1, 0);
    private static final Point V3 = new Point(-1, -1, 0);
    private static final Point V4 = new Point(-1, 1, 0);

    /** The polygon (square) used for tests */
    private static final Polygon POLYGON = new Polygon(V1, V2, V3, V4);

    /** EP01: Ray intersecting strictly inside the polygon (at 0,0,0) */
    private static final Ray EP01 = new Ray(new Point(0, 0, 1), new Vector(0, 0, -1));

    /** EP02: Ray intersecting outside against an edge (against V1-V2) */
    private static final Ray EP02 = new Ray(new Point(2, 0, 1), new Vector(0, 0, -1));

    /** EP03: Ray intersecting outside against a vertex (against V1) */
    private static final Ray EP03 = new Ray(new Point(2, 2, 1), new Vector(0, 0, -1));

    /** Ray whose line does not intersect the plane of the polygon at all */
    private static final Ray EP04 = new Ray(new Point(0, 0, 1), new Vector(1, 1, 1));

    /** BV11: Ray intersecting strictly on an edge (on V1-V2 at 1,0,0) */
    private static final Ray BV11 = new Ray(new Point(1, 0, 1), new Vector(0, 0, -1));

    /** BV12: Ray intersecting strictly on a vertex (on V1 at 1,1,0) */
    private static final Ray BV12 = new Ray(new Point(1, 1, 1), new Vector(0, 0, -1));

    /** BV13: Ray intersecting on an edge's continuation (continuation of V2-V1) */
    private static final Ray BV13 = new Ray(new Point(1, 2, 1), new Vector(0, 0, -1));


    /** Ray parallel to the plane and included in it */
    private static final Ray BV21 = new Ray(new Point(0, 0, 0), new Vector(1, 0, 0));

    /** Ray parallel to the plane but not included in it */
    private static final Ray BV22 = new Ray(new Point(0, 0, 1), new Vector(1, 0, 0));

    /** Ray orthogonal to the plane, starting IN the plane */
    private static final Ray BV31 = new Ray(new Point(0, 0, 0), new Vector(0, 0, -1));

    /** Ray orthogonal to the plane, starting AFTER the plane */
    private static final Ray BV32 = new Ray(new Point(0, 0, -1), new Vector(0, 0, -1));

    /** Ray orthogonal to the plane, starting BEFORE the plane */
    private static final Ray BV33 = new Ray(new Point(0, 0, 1), new Vector(0, 0, -1));

    /** Ray beginning on the plane and pointing away from it */
    private static final Ray BV41 = new Ray(new Point(0, 0, 0), new Vector(1, 1, 1));

    // ================= Expected Intersections Lists =================

    /** Expected intersection point for EP01 test */
    private static final List<Point> INTERSECTION_EP01 = List.of(new Point(0, 0, 0));

    /** Expected intersection point for BV33 test */
    private static final List<Point> INTERSECTION_BV33 = List.of(new Point(0, 0, 0));

    /**
     * Test method for {@link Polygon#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {

        // =========================================================================
        //                 PART 1: SPECIFIC POLYGON TESTS
        // All rays here go straight down (0,0,-1) from z=1 to intersect the plane z=0
        // =========================================================================

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray intersects strictly inside the polygon
        assertEquals(INTERSECTION_EP01, POLYGON.findIntersections(EP01), ERROR_POLYGON_INTERSECTION);

        // TC02: Ray intersects outside against an edge (against V2-V3)
        assertNull(POLYGON.findIntersections(EP02), ERROR_POLYGON_INTERSECTION);

        // TC03: Ray intersects outside against a vertex (against V1)
        assertNull(POLYGON.findIntersections(EP03), ERROR_POLYGON_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Ray intersects strictly on an edge (on V2-V3)
        assertNull(POLYGON.findIntersections(BV11), ERROR_POLYGON_INTERSECTION);

        // TC12: Ray intersects strictly on a vertex (on V1)
        assertNull(POLYGON.findIntersections(BV12), ERROR_POLYGON_INTERSECTION);

        // TC13: Ray intersects on an edge's continuation (continuation of V2-V3)
        assertNull(POLYGON.findIntersections(BV13), ERROR_POLYGON_INTERSECTION);


        // =========================================================================
        //                 PART 2: PLANE BASED TESTS (No Intersections)
        // Cases where the ray doesn't even intersect the plane the polygon is on
        // =========================================================================

        // ============ Equivalence Partitions Tests ==============

        // TC04: Ray's line does not intersect the plane at all
        assertNull(POLYGON.findIntersections(EP04), ERROR_POLYGON_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray is parallel to the plane
        // TC21: The ray is included in the plane
        assertNull(POLYGON.findIntersections(BV21), ERROR_POLYGON_INTERSECTION);

        // TC22: The ray is parallel but not included in the plane
        assertNull(POLYGON.findIntersections(BV22), ERROR_POLYGON_INTERSECTION);

        // **** Group 2: Ray is orthogonal to the plane
        // TC31: Ray starts IN the plane (orthogonal)
        assertNull(POLYGON.findIntersections(BV31), ERROR_POLYGON_INTERSECTION);

        // TC32: Ray starts AFTER the plane (orthogonal)
        assertNull(POLYGON.findIntersections(BV32), ERROR_POLYGON_INTERSECTION);

        // TC33: Ray starts BEFORE the plane (orthogonal)
        assertEquals(INTERSECTION_BV33, POLYGON.findIntersections(BV33), ERROR_POLYGON_INTERSECTION);

        // **** Group 3: Ray begins on the plane (neither orthogonal nor parallel)
        // TC41: Ray begins on the plane and points away
        assertNull(POLYGON.findIntersections(BV41), ERROR_POLYGON_INTERSECTION);
    }
}
