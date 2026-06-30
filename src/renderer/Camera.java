package renderer;

import primitives.*;
import scene.Scene;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static primitives.Util.isZero;

import renderer.postprocessing.PostProcessingEngine;
import renderer.postprocessing.PostProcessFilter;

/**
 * Represents a camera in 3D space, utilizing the Builder design pattern.
 * @author itzik123
 */
public class Camera implements Cloneable {

    /** The location of the camera. */
    private Point _p0;

    /** The forward direction vector. */
    private Vector _vTo;

    /** The upward direction vector. */
    private Vector _vUp;

    /** The rightward direction vector. */
    private Vector _vRight;

    /** The physical width of the view plane. */
    private double _width;

    /** The physical height of the view plane. */
    private double _height;

    /** The distance from the camera to the view plane. */
    private double _distance;

    /** The number of pixels in the X axis (horizontal resolution). */
    private int _nX = 1;

    /** The number of pixels in the Y axis (vertical resolution). */
    private int _nY = 1;

    /** The center point of the view plane. */
    private Point _vpCenter;

    /** The physical width of a single pixel on the view plane. */
    private double _pixelWidth;

    /** The physical height of a single pixel on the view plane. */
    private double _pixelHeight;

    /** The image writer */
    private ImageWriter _imageWriter;

    /** The ray tracer */
    private RayTracerBase _rayTracer;

    /** The target area manager for Anti-Aliasing super-sampling */
    private TargetArea _antiAliasingTargetArea = new TargetArea();

    /** Amount of threads to use for rendering image by the camera */
    private int _threadsCount = 0;

    /** Amount of threads to spare for Java VM threads */
    private static final int SPARE_THREADS = 2;

    /** Debug print interval in seconds (for progress percentage) */
    private double _printInterval = 0;

    /** Pixel manager for supporting multi-threading and debug print */
    private PixelManager _pixelManager;

    /** The post-processing engine for 2D image filters */
    private PostProcessingEngine _postProcessingEngine = new PostProcessingEngine();


    /**
     * Private default constructor to prevent direct instantiation.
     * Use {@link #getBuilder()} to construct a Camera.
     */
    private Camera() { }

    /**
     * Gets a new Builder instance for constructing a Camera.
     * @return a new Camera Builder
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * This function renders image's pixel color map from the scene.
     * Chooses the rendering method based on threads configuration.
     * @return the camera object itself
     */
    public Camera renderImage() {
        _pixelManager = new PixelManager(_nY, _nX, _printInterval);
        return switch (_threadsCount) {
            case 0 -> renderImageNoThreads();
            case -1 -> renderImageStream();
            default -> renderImageRawThreads();
        };
    }

    /**
     * Render image without multi-threading.
     * @return the camera object itself
     */
    private Camera renderImageNoThreads() {
        for (int i = 0; i < _nY; ++i) {
            for (int j = 0; j < _nX; ++j) {
                castRay(j, i);
            }
        }
        return this;
    }

    /**
     * Render image using multi-threading by parallel streaming.
     * @return the camera object itself
     */
    private Camera renderImageStream() {
        IntStream.range(0, _nY).parallel()
                .forEach(i -> IntStream.range(0, _nX).parallel()
                        .forEach(j -> castRay(j, i)));
        return this;
    }

    /**
     * Render image using multi-threading by creating and running raw threads.
     * @return the camera object itself
     */
    private Camera renderImageRawThreads() {
        var threads = new LinkedList<Thread>();
        int count = _threadsCount;
        while (count-- > 0) {
            threads.add(new Thread(() -> {
                PixelManager.Pixel pixel;
                while ((pixel = _pixelManager.nextPixel()) != null) {
                    castRay(pixel.col(), pixel.row());
                }
            }));
        }
        for (var thread : threads) thread.start();
        try {
            for (var thread : threads) thread.join();
        } catch (InterruptedException ignored) {}
        return this;
    }

    private void castRay(int xIndex, int yIndex) {
        List<Ray> rays = constructRays(xIndex, yIndex);
        Color finalColor = Color.BLACK;

        for (Ray ray : rays) {
            finalColor = finalColor.add(_rayTracer.traceRay(ray));
        }

        finalColor = finalColor.reduce(rays.size());
        _imageWriter.writePixel(xIndex, yIndex, finalColor);

        _pixelManager.pixelDone();
    }

    public Camera printGrid(int interval, Color color) {
        for (int i = 0; i < _nY; ++i) {
            for (int j = 0; j < _nX; ++j) {
                if (j % interval == 0 || i % interval == 0) {
                    _imageWriter.writePixel(j, i, color);
                }
            }
        }
        return this;
    }

    /**
     * Delegates the image writing process to the internal ImageWriter.
     * @param imageName the name of the output image file
     */
    public void writeToImage(String imageName) {
        _imageWriter.applyPostProcessing(_postProcessingEngine);
        _imageWriter.writeToImage(imageName);
    }

    /**
     * Calculates the center point of a specific pixel on the view plane.
     */
    private Point getPixelCenter(int xIndex, int yIndex) {
        double xJ = (xIndex - (_nX - 1) / 2.0) * _pixelWidth;
        double yI = -(yIndex - (_nY - 1) / 2.0) * _pixelHeight;

        Point pIJ = _vpCenter;

        if (!isZero(xJ)) pIJ = pIJ.add(_vRight.scale(xJ));
        if (!isZero(yI)) pIJ = pIJ.add(_vUp.scale(yI));

        return pIJ;
    }

    /**
     * Constructs a beam of rays passing through a specific pixel for super-sampling.
     */
    public List<Ray> constructRays(int xIndex, int yIndex) {
        List<Ray> rays = new LinkedList<>();
        Point pIJ = getPixelCenter(xIndex, yIndex);

        List<Point2D> samplePoints = _antiAliasingTargetArea.getSamplePoints();

        for (Point2D offset : samplePoints) {
            Point pSample = pIJ;

            if (!isZero(offset.x())) {
                pSample = pSample.add(_vRight.scale(offset.x()));
            }
            if (!isZero(offset.y())) {
                pSample = pSample.add(_vUp.scale(offset.y()));
            }

            rays.add(new Ray(_p0, pSample.subtract(_p0)));
        }
        return rays;
    }

    public Ray constructRay(int xIndex, int yIndex) {
        return constructRays(xIndex, yIndex).getFirst();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Builder class for constructing a Camera object.
     */
    public static class Builder {

        private final Camera _camera = new Camera();
        private Vector _vTo = null;
        private Point _target = null;
        private Vector _vUp = Vector.AXIS_Y;
        private double _rotationAngle = 0;

        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        public Builder setDirection(Vector to, Vector up) {
            this._vTo = to;
            this._vUp = up;
            return this;
        }

        public Builder setDirection(Point target, Vector up) {
            this._target = target;
            this._vUp = up;
            return this;
        }

        public Builder setDirection(Point target) {
            this._target = target;
            return this;
        }

        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;
            return this;
        }

        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        public Builder setRayTracer(Scene scene, RayTracerType type) {
            if (type == RayTracerType.SIMPLE) {
                _camera._rayTracer = new SimpleRayTracer(scene);
            } else {
                throw new IllegalArgumentException("Unsupported ray tracer type: " + type);
            }
            return this;
        }

        public Builder setAntiAliasing(int resolution, SamplingPattern pattern) {
            _camera._antiAliasingTargetArea.setResolution(resolution).setSamplingPattern(pattern);
            return this;
        }

        /**
         * Adds a 2D post-processing filter to the camera's pipeline.
         */
        public Builder addPostProcessFilter(PostProcessFilter filter) {
            _camera._postProcessingEngine.addFilter(filter);
            return this;
        }

        /**
         * Rotates the camera around its viewing direction (vTo axis).
         * @param angle Rotation angle in degrees (clockwise)
         * @return The Builder itself
         */
        public Builder rotate(double angle) {
            this._rotationAngle = angle;
            return this;
        }

        /**
         * Set multi-threading.
         * Parameter value meaning:
         * -2 : number of threads is number of logical processors less SPARE_THREADS
         * -1 : stream processing parallelization (implicit multi-threading) is used
         * 0 : multi-threading is not activated
         * 1+ : literally number of threads
         */
        public Builder setMultithreading(int threads) {
            if (threads < -2)
                throw new IllegalArgumentException("Multithreading parameter must be -2 or higher");
            if (threads == -2) {
                int cores = Runtime.getRuntime().availableProcessors() - Camera.SPARE_THREADS;
                _camera._threadsCount = cores <= 2 ? 1 : cores;
            } else {
                _camera._threadsCount = threads;
            }
            return this;
        }

        /**
         * Set debug printing interval. If it's zero there won't be printing at all.
         * @param interval printing interval in seconds
         */
        public Builder setDebugPrint(double interval) {
            if (interval < 0) throw new IllegalArgumentException("interval parameter must be non-negative");
            _camera._printInterval = interval;
            return this;
        }

        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            checkViewPlane();

            if (_camera._rayTracer == null) {
                this.setRayTracer(new Scene("test"), RayTracerType.SIMPLE);
            }

            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0)
                throw new IllegalArgumentException("Resolution must be positive");
            _camera._imageWriter = new ImageWriter(_camera._nX, _camera._nY);
        }

        private void checkLocationAndDirection() {
            if (_camera._p0 == null || _vUp == null || (_vTo == null && _target == null)) {
                throw new java.util.MissingResourceException("Missing camera location or direction data", "Camera.Builder", "");
            }

            if (_vTo == null) {
                _vTo = _target.subtract(_camera._p0);
            }

            _camera._vTo = _vTo.normalize();

            try {
                _camera._vRight = _camera._vTo.crossProduct(_vUp).normalize();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("The 'up' vector cannot be parallel to the 'to' vector");
            }

            _camera._vUp = _camera._vRight.crossProduct(_camera._vTo);

            if (!isZero(_rotationAngle)) {
                double radians = Math.toRadians(_rotationAngle);
                double cos = Math.cos(radians);
                double sin = Math.sin(radians);

                Vector oldUp = _camera._vUp;
                Vector oldRight = _camera._vRight;

                Vector newUp = null;
                if (isZero(cos)) {
                    newUp = oldRight.scale(sin);
                } else if (isZero(sin)) {
                    newUp = oldUp.scale(cos);
                } else {
                    newUp = oldUp.scale(cos).add(oldRight.scale(sin));
                }

                _camera._vUp = newUp.normalize();

                // חישוב וקטור ימינה מחדש כדי לשמור על מערכת צירים אורתוגונלית
                _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();
            }
        }

        private void checkViewPlane() {
            if (_camera._width <= 0 || _camera._height <= 0)
                throw new IllegalArgumentException("Width and height must be positive");

            if (_camera._distance <= 0)
                throw new IllegalArgumentException("Distance must be positive");

            _camera._vpCenter = _camera._p0.add(_camera._vTo.scale(_camera._distance));
            _camera._pixelWidth = _camera._width / _camera._nX;
            _camera._pixelHeight = _camera._height / _camera._nY;

            _camera._antiAliasingTargetArea.setSize(_camera._pixelWidth, _camera._pixelHeight);
        }
    }
}