package renderer;

/**
 * Enumerates the available ray tracing strategies.
 * <p>
 * Used to select which ray tracer implementation the renderer should use.
 */
public enum RayTracerType {
   /**
    * A basic ray tracer.
    */
   SIMPLE,

   /**
    * A ray tracer that uses a regular grid acceleration structure.
    */
   GRID
}
