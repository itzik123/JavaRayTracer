package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import scene.Scene;

class SimpleRayTracer extends RayTracerBase{

    public SimpleRayTracer(Scene scene){
        super(scene);
    }
    private Color calcColor(Point intersection){
        return _scene.ambientLight.getIntensity();
    }
    @Override
    Color traceRay(Ray ray) {
        var intersecting_point = ray.findClosestPoint(_scene.geometries.findIntersections(ray));
        return (intersecting_point != null)? calcColor(intersecting_point) : _scene.background;
    }

}
