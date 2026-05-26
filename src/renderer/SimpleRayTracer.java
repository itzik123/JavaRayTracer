package renderer;

import geometries.api.Intersectable;
import primitives.Color;
import primitives.Point;
import primitives.Ray;
import scene.Scene;

class SimpleRayTracer extends RayTracerBase{

    public SimpleRayTracer(Scene scene){
        super(scene);
    }
    private Color calcColor(Intersectable.Intersection intersection){
        return (_scene.ambientLight.getIntensity().scale(intersection.material.kA)).add(intersection.geometry.getEmission());
    }
    @Override
    Color traceRay(Ray ray) {
        var intersecting = ray.findClosestIntersection(_scene.geometries.calcIntersections(ray));
        return (intersecting != null)? calcColor(intersecting) : _scene.background;
    }

}
