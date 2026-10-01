package renderer;

import static primitives.Util.isZero;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import renderer.acceleration.AccelerationStructure;
import renderer.acceleration.AdaptiveGrid;
import renderer.acceleration.LinearScan;
import renderer.acceleration.RegularGrid;
import renderer.postprocessing.PostProcessFilter;
import renderer.postprocessing.PostProcessingEngine;
import renderer.sampling.AdaptiveSuperSampler;
import renderer.sampling.Point2D;
import renderer.sampling.SamplingPattern;
import renderer.sampling.TargetArea;
import scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.IntStream;

/**
 * A pinhole camera that renders a scene into an image.
 * <p>
 * The camera shoots rays from its location through a view plane divided into pixels, traces
 * them, and writes the resulting colors to an image, optionally with anti-aliasing,
 * multithreading and post-processing. Cameras are immutable once built and are created with a
 * {@link Builder}.
 *
 * @author itzik123
 */
public class Camera implements Cloneable {

    /** Threads left free for the JVM when the thread count is chosen automatically. */
    private static final int SPARE_THREADS = 2;

    /** The location of the camera. */
    private Point _p0;
    /** The viewing direction. */
    private Vector _vTo;
    /** The upward direction. */
    private Vector _vUp;
    /** The rightward direction. */
    private Vector _vRight;
    /** The width of the view plane. */
    private double _width = 0;
    /** The height of the view plane. */
    private double _height = 0;
    /** The distance from the camera to the view plane. */
    private double _distance = 0;
    /** The number of pixel columns. */
    private int _nX = 1;
    /** The number of pixel rows. */
    private int _nY = 1;
    /** The center of the view plane. */
    private Point _vpCenter;
    /** The width of a pixel on the view plane. */
    private double _pixelWidth;
    /** The height of a pixel on the view plane. */
    private double _pixelHeight;

    /** The image being rendered. */
    private ImageWriter _imageWriter;
    /** Computes the color along each ray. */
    private RayTracerBase _rayTracer;
    /** The pixel sampling used for anti-aliasing. */
    private TargetArea _pixelSampling;
    /** The filters applied to the image before it is saved. */
    private PostProcessingEngine _postProcessing;

    /** Maximum depth of adaptive super-sampling (0 disables it). */
    private int _adaptiveDepth = 0;
    /** Color tolerance of adaptive super-sampling. */
    private double _adaptiveTolerance = 0;
    /** Rendering threads: 0 - single thread, -1 - parallel streams, positive - thread count. */
    private int _threadsCount = 0;
    /** Progress printing interval in seconds (0 disables printing). */
    private double _printInterval = 0;
    /** Distributes pixels among threads and tracks progress during a render. */
    private PixelManager _pixelManager;

    /** Cameras are created with {@link #getBuilder()}. */
    private Camera() {
    }

    /**
     * Returns a new camera builder.
     *
     * @return a new camera builder
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Renders the scene into the camera image.
     *
     * @return this camera
     */
    public Camera renderImage() {
        _pixelManager = new PixelManager(_nY, _nX, _printInterval);
        switch (_threadsCount) {
            case 0 -> {
                for (int i = 0; i < _nY; ++i)
                    for (int j = 0; j < _nX; ++j) castRay(j, i);
            }
            case -1 -> IntStream.range(0, _nY).parallel()
                    .forEach(i -> IntStream.range(0, _nX).parallel().forEach(j -> castRay(j, i)));
            default -> renderWithThreads();
        }
        return this;
    }

    /**
     * Renders with a pool of worker threads that take pixels from the pixel manager.
     */
    private void renderWithThreads() {
        List<Thread> threads = new ArrayList<>(_threadsCount);
        for (int i = 0; i < _threadsCount; ++i)
            threads.add(Thread.ofPlatform().start(() -> {
                PixelManager.Pixel pixel;
                while ((pixel = _pixelManager.nextPixel()) != null) castRay(pixel.col(), pixel.row());
            }));
        try {
            for (Thread thread : threads) thread.join();
        } catch (InterruptedException e) {
            threads.forEach(Thread::interrupt);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Computes and writes the color of one pixel.
     *
     * @param xIndex the pixel column
     * @param yIndex the pixel row
     */
    private void castRay(int xIndex, int yIndex) {
        Color color;
        if (_adaptiveDepth > 0) {
            color = AdaptiveSuperSampler.sample(getPixelCenter(xIndex, yIndex), _pixelWidth, _pixelHeight,
                    _vRight, _vUp, _adaptiveDepth, _adaptiveTolerance,
                    point -> _rayTracer.traceRay(new Ray(_p0, point.subtract(_p0))));
        } else {
            List<Ray> rays = constructRays(xIndex, yIndex);
            color = Color.BLACK;
            for (Ray ray : rays) color = color.add(_rayTracer.traceRay(ray));
            color = color.reduce(rays.size());
        }
        _imageWriter.writePixel(xIndex, yIndex, color);
        _pixelManager.pixelDone();
    }

    /**
     * Draws a grid over the image, for debugging.
     *
     * @param interval the grid spacing, in pixels
     * @param color    the grid color
     * @return this camera
     */
    public Camera printGrid(int interval, Color color) {
        for (int i = 0; i < _nY; ++i)
            for (int j = 0; j < _nX; ++j)
                if (j % interval == 0 || i % interval == 0) _imageWriter.writePixel(j, i, color);
        return this;
    }

    /**
     * Applies the post-processing filters and saves the image as a PNG file.
     *
     * @param imageName the file name, without extension
     */
    public void writeToImage(String imageName) {
        _imageWriter.writeToImage(imageName, _postProcessing);
    }

    /**
     * Calculates the center of a pixel on the view plane.
     *
     * @param xIndex the pixel column
     * @param yIndex the pixel row
     * @return the center of the pixel
     */
    private Point getPixelCenter(int xIndex, int yIndex) {
        double xJ = (xIndex - (_nX - 1) / 2.0) * _pixelWidth;
        double yI = -(yIndex - (_nY - 1) / 2.0) * _pixelHeight;
        return moveOnViewPlane(_vpCenter, xJ, yI);
    }

    /**
     * Moves a point along the view plane axes, skipping zero offsets (which cannot build a vector).
     *
     * @param point the point
     * @param right the offset to the right
     * @param up    the upward offset
     * @return the moved point
     */
    private Point moveOnViewPlane(Point point, double right, double up) {
        Point result = point;
        if (!isZero(right)) result = result.add(_vRight.scale(right));
        if (!isZero(up)) result = result.add(_vUp.scale(up));
        return result;
    }

    /**
     * Constructs the ray through the center of a pixel.
     *
     * @param xIndex the pixel column
     * @param yIndex the pixel row
     * @return the ray through the pixel center
     */
    public Ray constructRay(int xIndex, int yIndex) {
        return new Ray(_p0, getPixelCenter(xIndex, yIndex).subtract(_p0));
    }

    /**
     * Constructs the rays through the sample points of a pixel - a single central ray when
     * anti-aliasing is off.
     *
     * @param xIndex the pixel column
     * @param yIndex the pixel row
     * @return the rays through the pixel
     */
    public List<Ray> constructRays(int xIndex, int yIndex) {
        Point center = getPixelCenter(xIndex, yIndex);
        List<Point2D> samples = _pixelSampling.getSamplePoints();
        List<Ray> rays = new ArrayList<>(samples.size());
        for (Point2D offset : samples)
            rays.add(new Ray(_p0, moveOnViewPlane(center, offset.x(), offset.y()).subtract(_p0)));
        return rays;
    }

    @Override
    protected Camera clone() {
        try {
            return (Camera) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Camera is Cloneable", e);
        }
    }

    /**
     * Builds {@link Camera} instances.
     * <p>
     * Location, direction, view plane size and distance are mandatory. The builder can be reused:
     * every {@link #build()} produces an independent camera with its own image and ray tracer.
     */
    public static class Builder {

        /** Holds the plain camera settings; cloned by every build. */
        private final Camera _camera = new Camera();

        /** The viewing direction, if given explicitly. */
        private Vector _vTo;
        /** The point to look at, if given instead of a direction. */
        private Point _target;
        /** The approximate upward direction. */
        private Vector _vUp = Vector.AXIS_Y;
        /** Rotation of the camera around its viewing direction, in degrees. */
        private double _rotationAngle = 0;

        /** The scene to render; an empty scene is used if none is set. */
        private Scene _scene;
        /** How the ray tracer finds the geometry a ray hits. */
        private RayTracerType _rayTracerType = RayTracerType.SIMPLE;
        /** Voxels per axis of the regular grid. */
        private int _gridResolution = 50;
        /** Geometries per octree cell before it is split. */
        private int _maxGeometriesPerNode = 10;
        /** Maximum depth of the octree. */
        private int _maxOctreeDepth = 5;

        /** Samples per axis of each pixel for anti-aliasing. */
        private int _antiAliasingResolution = 1;
        /** The distribution of the pixel samples. */
        private SamplingPattern _samplingPattern;
        /** The post-processing filters, in application order. */
        private final List<PostProcessFilter> _filters = new ArrayList<>();

        /** Use {@link Camera#getBuilder()}. */
        private Builder() {
        }

        /**
         * Sets the location of the camera.
         *
         * @param location the location of the camera
         * @return this builder
         */
        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        /**
         * Sets the viewing direction with an explicit up vector.
         *
         * @param to the viewing direction
         * @param up the upward direction; must not be parallel to {@code to}
         * @return this builder
         */
        public Builder setDirection(Vector to, Vector up) {
            _vTo = to;
            _target = null;
            _vUp = up;
            return this;
        }

        /**
         * Aims the camera at a point with an explicit up vector.
         *
         * @param target the point to look at
         * @param up     the upward direction
         * @return this builder
         */
        public Builder setDirection(Point target, Vector up) {
            _target = target;
            _vTo = null;
            _vUp = up;
            return this;
        }

        /**
         * Aims the camera at a point, keeping the Y axis as the upward direction.
         *
         * @param target the point to look at
         * @return this builder
         */
        public Builder setDirection(Point target) {
            return setDirection(target, Vector.AXIS_Y);
        }

        /**
         * Rotates the camera around its viewing direction.
         *
         * @param angle the rotation angle, in degrees
         * @return this builder
         */
        public Builder rotate(double angle) {
            _rotationAngle = angle;
            return this;
        }

        /**
         * Sets the size of the view plane.
         *
         * @param width  the width of the view plane
         * @param height the height of the view plane
         * @return this builder
         */
        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;
            return this;
        }

        /**
         * Sets the distance from the camera to the view plane.
         *
         * @param distance the distance from the camera to the view plane
         * @return this builder
         */
        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        /**
         * Sets the image resolution.
         *
         * @param nX the number of pixel columns
         * @param nY the number of pixel rows
         * @return this builder
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        /**
         * Sets the scene and the geometry search strategy. The ray tracer is created by
         * {@link #build()}, so the scene must be complete by then.
         *
         * @param scene the scene to render
         * @param type  how the ray tracer finds the geometry a ray hits
         * @return this builder
         */
        public Builder setRayTracer(Scene scene, RayTracerType type) {
            _scene = scene;
            _rayTracerType = type;
            return this;
        }

        /**
         * Sets the number of voxels along each axis of the regular grid.
         *
         * @param resolution the number of voxels along each axis of the regular grid
         * @return this builder
         * @throws IllegalArgumentException if the resolution is not positive
         */
        public Builder setGridResolution(int resolution) {
            if (resolution < 1) throw new IllegalArgumentException("Grid resolution must be positive");
            _gridResolution = resolution;
            return this;
        }

        /**
         * Sets the octree parameters, used by {@link RayTracerType#ADAPTIVE_GRID}.
         *
         * @param maxGeometries the number of geometries an octree cell may hold before it is split
         * @param maxDepth      the maximum depth of the octree
         * @return this builder
         * @throws IllegalArgumentException if a parameter is not positive
         */
        public Builder setAdaptiveGridParameters(int maxGeometries, int maxDepth) {
            if (maxGeometries < 1 || maxDepth < 1)
                throw new IllegalArgumentException("Octree capacity and depth must be positive");
            _maxGeometriesPerNode = maxGeometries;
            _maxOctreeDepth = maxDepth;
            return this;
        }

        /**
         * Enables anti-aliasing by super-sampling every pixel.
         *
         * @param resolution the number of samples along each axis of a pixel (1 disables it)
         * @param pattern    the distribution of the samples, or {@code null} to disable it
         * @return this builder
         * @throws IllegalArgumentException if the resolution is not positive
         */
        public Builder setAntiAliasing(int resolution, SamplingPattern pattern) {
            if (resolution < 1) throw new IllegalArgumentException("Anti-aliasing resolution must be positive");
            _antiAliasingResolution = resolution;
            _samplingPattern = pattern;
            return this;
        }

        /**
         * Enables adaptive super-sampling, which subdivides a pixel only where its colors differ.
         * Overrides fixed anti-aliasing, and also applies to glossy and frosted materials.
         *
         * @param depth     the maximum subdivision depth (0 disables it)
         * @param tolerance the per-channel color difference under which samples are considered
         *                  equal
         * @return this builder
         * @throws IllegalArgumentException if a parameter is negative
         */
        public Builder setAdaptiveSuperSampling(int depth, double tolerance) {
            if (depth < 0 || tolerance < 0) throw new IllegalArgumentException("Parameters must be non-negative");
            _camera._adaptiveDepth = depth;
            _camera._adaptiveTolerance = tolerance;
            return this;
        }

        /**
         * Appends a filter to the post-processing pipeline.
         *
         * @param filter the filter
         * @return this builder
         */
        public Builder addPostProcessFilter(PostProcessFilter filter) {
            _filters.add(filter);
            return this;
        }

        /**
         * Sets multithreading:
         * <ul>
         * <li>-2: one thread per logical processor, minus {@value Camera#SPARE_THREADS}</li>
         * <li>-1: parallel streams</li>
         * <li>0: single-threaded</li>
         * <li>positive: that number of threads</li>
         * </ul>
         *
         * @param threads the multithreading mode
         * @return this builder
         * @throws IllegalArgumentException if the value is less than -2
         */
        public Builder setMultithreading(int threads) {
            if (threads < -2) throw new IllegalArgumentException("Multithreading parameter must be -2 or higher");
            if (threads == -2) {
                int cores = Runtime.getRuntime().availableProcessors() - SPARE_THREADS;
                _camera._threadsCount = cores <= 2 ? 1 : cores;
            } else {
                _camera._threadsCount = threads;
            }
            return this;
        }

        /**
         * Sets the progress printing interval in seconds (0 disables printing).
         *
         * @param interval the progress printing interval in seconds (0 disables printing)
         * @return this builder
         * @throws IllegalArgumentException if the interval is negative
         */
        public Builder setDebugPrint(double interval) {
            if (interval < 0) throw new IllegalArgumentException("Interval must be non-negative");
            _camera._printInterval = interval;
            return this;
        }

        /**
         * Validates the settings and creates a camera.
         *
         * @return the new camera
         * @throws MissingResourceException if the location or the direction is missing
         * @throws IllegalArgumentException if a setting is invalid
         */
        public Camera build() {
            Camera camera = _camera.clone();
            if (camera._nX <= 0 || camera._nY <= 0) throw new IllegalArgumentException("Resolution must be positive");
            if (camera._width <= 0 || camera._height <= 0)
                throw new IllegalArgumentException("View plane width and height must be positive");
            if (camera._distance <= 0) throw new IllegalArgumentException("View plane distance must be positive");

            setOrientation(camera);
            camera._vpCenter = camera._p0.add(camera._vTo.scale(camera._distance));
            camera._pixelWidth = camera._width / camera._nX;
            camera._pixelHeight = camera._height / camera._nY;

            // Mutable parts are created per camera, so cameras built by one builder never share state
            camera._imageWriter = new ImageWriter(camera._nX, camera._nY);
            camera._pixelSampling = new TargetArea()
                    .setSize(camera._pixelWidth, camera._pixelHeight)
                    .setResolution(_antiAliasingResolution)
                    .setSamplingPattern(_samplingPattern);
            camera._postProcessing = new PostProcessingEngine(_filters);
            camera._rayTracer = createRayTracer(camera);
            return camera;
        }

        /**
         * Computes the orthonormal camera axes, applying the rotation around the viewing direction.
         *
         * @param camera the camera to orient
         * @throws MissingResourceException if the location or the direction is missing
         * @throws IllegalArgumentException if the up vector is parallel to the viewing direction
         */
        private void setOrientation(Camera camera) {
            if (camera._p0 == null || _vUp == null || (_vTo == null && _target == null))
                throw new MissingResourceException("Missing camera location or direction", "Camera.Builder", "");

            camera._vTo = (_vTo != null ? _vTo : _target.subtract(camera._p0)).normalize();
            try {
                camera._vRight = camera._vTo.crossProduct(_vUp).normalize();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("The up vector cannot be parallel to the viewing direction", e);
            }
            camera._vUp = camera._vRight.crossProduct(camera._vTo);

            if (isZero(_rotationAngle)) return;
            double radians = Math.toRadians(_rotationAngle);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            // Rotate up towards right; a zero component cannot be scaled into a vector
            Vector up;
            if (isZero(cos)) up = camera._vRight.scale(sin);
            else if (isZero(sin)) up = camera._vUp.scale(cos);
            else up = camera._vUp.scale(cos).add(camera._vRight.scale(sin));
            camera._vUp = up.normalize();
            camera._vRight = camera._vTo.crossProduct(camera._vUp).normalize();
        }

        /**
         * Creates the ray tracer with the configured geometry search strategy.
         *
         * @param camera the camera being built
         * @return the ray tracer
         */
        private RayTracerBase createRayTracer(Camera camera) {
            Scene scene = _scene != null ? _scene : new Scene("Empty scene");
            AccelerationStructure accelerator = switch (_rayTracerType) {
                case SIMPLE -> new LinearScan(scene.geometries);
                case REGULAR_GRID -> new RegularGrid(scene.geometries.flatten(), _gridResolution);
                case ADAPTIVE_GRID -> new AdaptiveGrid(scene.geometries.flatten(), _maxGeometriesPerNode, _maxOctreeDepth);
            };
            return new SimpleRayTracer(scene, accelerator)
                    .setAdaptiveGlossySampling(camera._adaptiveDepth, camera._adaptiveTolerance);
        }
    }
}
