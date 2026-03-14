package test;

import static java.lang.System.out;
import static primitives.Util.isZero;

import geometries.impl.*;
import primitives.*;

/**
 * Basic sanity tests for the primitives classes.
 * The program performs a small set of runtime checks on the basic
 * geometric primitives and on constructors of several geometry objects.
 * The tests verify correctness of the main algebraic operations and
 * several edge cases such as zero vectors and orthogonality.
 * The program prints error messages only if a test fails.
 * If no output appears, all tests succeeded.
 * @author Dan Zilberstein
 */
@SuppressWarnings("java:S109")
public final class Main {
   /** Default constructor to satisfy JavaDoc generator */
   public Main() { /* to satisfy JavaDoc generator */ }

   /** A point for tests at (1,2,3) */
   private static final Point  P1          = new Point(1, 2, 3);
   /** A point for tests at (2,4,6) */
   private static final Point  P2          = new Point(2, 4, 6);
   /** A point for tests at (2,4,5) */
   private static final Point  P3          = new Point(2, 4, 5);

   /** A vector for tests to (1,2,3) */
   private static final Vector V1          = new Vector(1, 2, 3);
   /** A vector for tests to (-1,-2,-3) (opposite to V1) */
   private static final Vector V1_OPPOSITE = new Vector(-1, -2, -3);
   /** A vector for tests to (-2,-4,-6) */
   private static final Vector V2          = new Vector(-2, -4, -6);
   /** A vector for tests to (0,3,-2) */
   private static final Vector V3          = new Vector(0, 3, -2);
   /** A vector for tests to (1,2,2) */
   private static final Vector V4          = new Vector(1, 2, 2);
   /** A vector for tests to (3,6,9) */
   private static final Vector V5          = new Vector(3, 6, 9);

   /**
    * Entry point of the project's basic sanity tests.
    * The program checks the core functionality implemented in the first stage.
    * It is intended to be executed again in later stages, without modification,
    * to verify that new code does not break the existing functionality.
    */
   public static void main() {
      pointTests();
      pointDistancesTests();
      vectorDoubleOperationTests();
      vectorSingleOperationTests();
      rayTests();
      geometryConstructorTests();
      out.println("If there were no any other outputs - all tests succeeded!");
   }

   /**
    * Basic test for the Ray constructor.
    * Verifies that the direction vector is normalized by the constructor.
    */
   private static void rayTests() {
      Ray r = new Ray(P1, new Vector(2, 4, 6));
      if (!isZero(r.direction().length() - 1))
         out.println("ERROR: Ray direction is not normalized");
   }

   /**
    * Method for testing operations on points
    */
   private static void pointTests() {
      // Subtract points
      if (!P2.subtract(P1).equals(V1))
         out.println("ERROR: (point2 - point1) does not work correctly");
      try {
         P1.subtract(P1);
         out.println("ERROR: (point - itself) does not throw an exception");
      } catch (IllegalArgumentException _) { /* ignored */ } catch (Exception _) {
         out.println("ERROR: (point - itself) throws wrong exception");
      }

      // Add vector to point
      if (!(P1.add(V1).equals(P2)))
         out.println("ERROR: (point + vector) = other point does not work correctly");
      if (!(P1.add(V1_OPPOSITE).equals(Point.ZERO)))
         out.println("ERROR: (point + vector) = center of coordinates does not work correctly");

      // Test consistency: P1 + (P2 - P1) = P2
      if (!P1.add(P2.subtract(P1)).equals(P2))
         out.println("ERROR: point + (point - point) incorrect");
   }

   /**
    * Tests distance and distanceSquared operations of Point.
    * Includes symmetry checks and consistency between the two methods.
    */
   private static void pointDistancesTests() { // distances
      if (!isZero(P1.distanceSquared(P1)))
         out.println("ERROR: point squared distance to itself is not zero");
      if (!isZero(P1.distance(P1)))
         out.println("ERROR: point distance to itself is not zero");
      if (!isZero(P1.distanceSquared(P3) - 9))
         out.println("ERROR: squared distance between points is wrong");
      if (!isZero(P3.distanceSquared(P1) - 9))
         out.println("ERROR: squared distance between points is wrong");
      if (!isZero(P1.distance(P3) - 3))
         out.println("ERROR: distance between points to itself is wrong");
      if (!isZero(P3.distance(P1) - 3))
         out.println("ERROR: distance between points to itself is wrong");
      if (P1.distance(P2) + P2.distance(P3) < P1.distance(P3))
         out.println("ERROR: triangle inequality violated");
      double d = P1.distance(P3);
      if (!isZero(d * d - P1.distanceSquared(P3)))
         out.println("ERROR: distance and distanceSquared inconsistent");
   }

   /**
    * Method for testing operations on single vector
    */
   private static void vectorSingleOperationTests() {
      // test zero vector =====================================================
      try {
         new Vector(0, 0, 0);
         new Vector(Double3.ZERO);
         out.println("ERROR: zero vector does not throw an exception");
      } catch (IllegalArgumentException _) {
         /* ignored */
      } catch (Exception _) {
         out.println("ERROR: zero vector throws wrong exception");
      }

      // test length
      if (!isZero(V4.lengthSquared() - 9))
         out.println("ERROR: lengthSquared() wrong value");
      if (!isZero(V4.length() - 3))
         out.println("ERROR: length() wrong value");

      // test vector normalization vs vector length and cross-product
      Vector u = V1.normalize();
      if (!isZero(u.length() - 1))
         out.println("ERROR: the normalized vector is not a unit vector");
      try { // test that the vectors are co-lined
         V1.crossProduct(u);
         out.println("ERROR: the normalized vector is not parallel to the original one");
      } catch (Exception _) { /* ignored */ }
      if (V1.dotProduct(u) < 0)
         out.println("ERROR: the normalized vector is opposite to the original one");

      // Test scale
      if (!V1.scale(2).equals(new Vector(2, 4, 6)))
         out.println("ERROR: scale() wrong result");
      try {
         V1.scale(0);
         out.println("ERROR: scale(0) should throw exception");
      } catch (IllegalArgumentException _) { /* ignored */ }
   }

   /**
    * Method for testing operations on two vectors
    */
   private static void vectorDoubleOperationTests() {
      // Test add & subtract
      try {
         V1.add(V1_OPPOSITE);
         out.println("ERROR: Vector + -itself does not throw an exception");
      } catch (IllegalArgumentException _) {
         /* ignored */
      } catch (Exception _) {
         out.println("ERROR: Vector + itself throws wrong exception");
      }
      try {
         V1.subtract(V1);
         out.println("ERROR: Vector - itself does not throw an exception");
      } catch (IllegalArgumentException _) {
         /* ignored */
      } catch (Exception _) {
         out.println("ERROR: Vector + itself throws wrong exception");
      }
      if (!V1.add(V2).equals(V1_OPPOSITE))
         out.println("ERROR: Vector + Vector does not work correctly");
      if (!V1.subtract(V2).equals(V5))
         out.println("ERROR: Vector - Vector does not work correctly");

      // test Dot-Product
      if (!isZero(V1.dotProduct(V3)))
         out.println("ERROR: dotProduct() for orthogonal vectors is not zero");
      if (!isZero(V1.dotProduct(V2) + 28))
         out.println("ERROR: dotProduct() wrong value");
      if (!isZero(V1.dotProduct(V1) - V1.lengthSquared()))
         out.println("ERROR: dotProduct(v,v) != lengthSquared()");

      // test Cross-Product properties: orthogonality and magnitude relation
      try { // test zero vector
         V1.crossProduct(V2);
         out.println("ERROR: crossProduct() for parallel vectors does not throw an exception");
      } catch (Exception _) { /* ignored */ }
      Vector vr = V1.crossProduct(V3);
      if (!isZero(vr.length() - V1.length() * V3.length()))
         out.println("ERROR: crossProduct() wrong result length");
      if (!isZero(vr.dotProduct(V1)) || !isZero(vr.dotProduct(V3)))
         out.println("ERROR: crossProduct() result is not orthogonal to its operands");

      // Test a × b = -(b × a)
      Vector a = V1.crossProduct(V3);
      Vector b = V3.crossProduct(V1);
      if (!a.equals(b.scale(-1)))
         out.println("ERROR: cross product anti-commutativity wrong");
   }

   /**
    * Basic constructor checks for geometry classes.
    * At this stage most geometry constructors only store the given parameters.
    * Therefore the tests verify mainly that objects can be created successfully.
    * The only geometry that performs structural validation in its constructor
    * is {@link Polygon}, which checks coplanarity, ordering and convexity.
    * However, Polygon is supplied to the students already tested, therefore
    * it is not tested here.
    */
   private static void geometryConstructorTests() {
      // At this stage geometries mainly store constructor parameters.

      // ---- Plane ----------------------------------------------------------
      // constructor by three points
      new Plane(P1, P2, P3);
      // constructor by point and normal vector
      Plane plane = new Plane(P1, V1);
      if (!plane.getNormal(P1).equals(V1.normalize()))
         out.println("ERROR: Plane(point, normal) constructor - wrong normal");

      // ---- Polygon based geometries ---------------------------------------
      new Triangle(P1, P2, P3);

      // ---- Radial geometries ----------------------------------------------
      new Sphere(new Point(0, 0, 0), 1);
      // ---- Tube-based geometries ------------------------------------------
      Ray axis = new Ray(Point.ZERO, Vector.AXIS_Z);
      new Tube(1, axis);
      new Cylinder(1, axis, 2);
   }

}
