package geometries.api;

import primitives.Point;
import primitives.Ray;

import java.util.List;

public abstract class Intersectable {
    public abstract List<Point> findIntersections(Ray ray);
}
