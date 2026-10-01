package geometries.impl;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

import geometries.api.Geometry;
import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * A convex, planar polygon.
 * <p>
 * The vertices must be ordered along the edge path, lie in one plane and form a convex shape;
 * all of this is validated by the constructor.
 *
 * @author Dan Zilberstein
 */
public class Polygon extends Geometry {

    /** The vertices, in edge order. */
    protected final List<Point> _vertices;
    /** The plane containing the polygon. */
    protected final Plane _plane;
    /** The number of vertices. */
    private final int _size;

    /**
     * Creates a convex polygon from its vertices.
     *
     * @param vertices at least three coplanar vertices, ordered along the edge path of a convex
     *                 polygon
     * @throws IllegalArgumentException if the vertices do not form a valid convex polygon
     */
    public Polygon(Point... vertices) {
        if (vertices.length < 3) throw new IllegalArgumentException("A polygon can't have less than 3 vertices");
        _vertices = List.of(vertices);
        _size = vertices.length;

        // The plane through the first three vertices defines the normal of the polygon
        _plane = new Plane(vertices[0], vertices[1], vertices[2]);
        if (_size == 3) return; // any three distinct, non-collinear points form a valid triangle

        Vector n = _plane.getNormal(vertices[0]);
        // Subtracting identical vertices would create a zero vector (illegal)
        Vector edge1 = vertices[_size - 1].subtract(vertices[_size - 2]);
        Vector edge2 = vertices[0].subtract(vertices[_size - 1]);

        // In a convex polygon, the cross products of consecutive edges all point to the same side
        // of the plane; a sign change means the polygon is concave or its vertices are unordered
        boolean positive = edge1.crossProduct(edge2).dotProduct(n) > 0;
        for (int i = 1; i < _size; ++i) {
            if (!isZero(vertices[i].subtract(vertices[0]).dotProduct(n)))
                throw new IllegalArgumentException("All vertices of a polygon must lay in the same plane");
            edge1 = edge2;
            edge2 = vertices[i].subtract(vertices[i - 1]);
            if (positive != (edge1.crossProduct(edge2).dotProduct(n) > 0))
                throw new IllegalArgumentException("All vertices must be ordered and the polygon must be convex");
        }
    }

    @Override
    public Vector getNormal(Point point) {
        return _plane.getNormal(point);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The supporting plane is intersected first - a cheap test that rejects most rays and
     * enforces {@code maxDistance}. The hit is then kept only if it is strictly inside the
     * polygon: for every edge, the ray must pass on the same side of the triangle formed by the
     * ray origin and that edge. Hits on an edge or a vertex are not intersections.
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        var planeIntersections = _plane.calcIntersections(ray, maxDistance);
        if (planeIntersections == null) return null;

        // The origin is off the plane here, so the vectors to the vertices are never zero
        // and consecutive ones are never parallel
        Point origin = ray.origin();
        Vector direction = ray.direction();
        Vector v1 = _vertices.get(_size - 1).subtract(origin);
        boolean positive = false;
        for (int i = 0; i < _size; ++i) {
            Vector v2 = _vertices.get(i).subtract(origin);
            // Normalizing keeps the zero test independent of the scene scale
            double sign = alignZero(direction.dotProduct(v1.crossProduct(v2).normalize()));
            if (sign == 0) return null; // on an edge or its continuation
            if (i == 0) positive = sign > 0;
            else if (positive != sign > 0) return null;
            v1 = v2;
        }
        return List.of(new Intersection(this, planeIntersections.getFirst().point));
    }

    @Override
    protected AABB createBoundingBox() {
        return AABB.enclosing(_vertices);
    }
}
