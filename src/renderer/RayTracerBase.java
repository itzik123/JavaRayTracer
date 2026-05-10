package renderer;

import primitives.Color;
import primitives.Ray;
import scene.Scene;

abstract class RayTracerBase {
    protected final Scene _scene;

    abstract Color traceTay(Ray ray);

    RayTracerBase(Scene scene){
        _scene = scene;
    }
}
