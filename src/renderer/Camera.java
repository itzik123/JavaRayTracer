package renderer;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.isZero;

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
     * Constructs a ray passing through the center of a specific pixel on the view plane.
     * @param xIndex the column index of the pixel (j)
     * @param yIndex the row index of the pixel (i)
     * @return a new Ray originating from the camera and passing through the specified pixel
     */
    public Ray constructRay(int xIndex, int yIndex) {
        double xJ = (xIndex - (_nX - 1) / 2.0) * _pixelWidth;
        double yI = -(yIndex - (_nY - 1) / 2.0) * _pixelHeight;

        Point pIJ = _vpCenter;

        if (!isZero(xJ))
            pIJ = pIJ.add(_vRight.scale(xJ));

        if (!isZero(yI))
            pIJ = pIJ.add(_vUp.scale(yI));

        return new Ray(_p0, pIJ.subtract(_p0));
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Builder class for constructing a Camera object.
     */
    public static class Builder {

        /** The camera instance being built. */
        private final Camera _camera = new Camera();

        /** Temporary forward direction vector. */
        private Vector _vTo = null;

        /** Temporary target point the camera is looking at. */
        private Point _target = null;

        /** Temporary general upward direction vector. */
        private Vector _vUp = Vector.AXIS_Y;

        /**
         * Sets the location of the camera.
         * @param location the location point
         * @return the current Builder instance
         */
        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        /**
         * Sets the direction of the camera using a forward and upward vector.
         * @param to the forward direction vector
         * @param up the general upward direction vector
         * @return the current Builder instance
         */
        public Builder setDirection(Vector to, Vector up) {
            this._vTo = to;
            this._vUp = up;
            return this;
        }

        /**
         * Sets the direction of the camera using a target point and an upward vector.
         * @param target the target point the camera is looking at
         * @param up the general upward direction vector
         * @return the current Builder instance
         */
        public Builder setDirection(Point target, Vector up) {
            this._target = target;
            this._vUp = up;
            return this;
        }

        /**
         * Sets the direction of the camera using a target point, relying on the default upward vector.
         * @param target the target point the camera is looking at
         * @return the current Builder instance
         */
        public Builder setDirection(Point target) {
            this._target = target;
            return this;
        }

        /**
         * Sets the physical size of the view plane.
         * @param width the physical width
         * @param height the physical height
         * @return the current Builder instance
         */
        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;
            return this;
        }

        /**
         * Sets the distance from the camera to the view plane.
         * @param distance the view plane distance
         * @return the current Builder instance
         */
        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        /**
         * Sets the resolution of the view plane.
         * @param nX the number of pixels in the horizontal direction (columns)
         * @param nY the number of pixels in the vertical direction (rows)
         * @return the current Builder instance
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        /**
         * Finalizes the camera construction, performs checks, calculates derived fields,
         * and returns a cloned instance of the fully configured camera.
         * @return a new Camera instance, or null if cloning fails
         */
        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            checkViewPlane();

            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        /**
         * Validates the resolution dimensions.
         * @throws IllegalArgumentException if the resolution values are not strictly positive
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0)
                throw new IllegalArgumentException("Resolution must be positive");
        }

        /**
         * Validates the location and direction data, and calculates the final orthonormal basis vectors.
         * @throws java.util.MissingResourceException if location or direction data is missing
         * @throws IllegalArgumentException if the 'up' vector is parallel to the 'to' vector
         */
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
        }

        /**
         * Validates the view plane dimensions and distance, and calculates the view plane center and pixel dimensions.
         * @throws IllegalArgumentException if the dimensions or distance are not strictly positive
         */
        private void checkViewPlane() {
            if (_camera._width <= 0 || _camera._height <= 0)
                throw new IllegalArgumentException("Width and height must be positive");

            if (_camera._distance <= 0)
                throw new IllegalArgumentException("Distance must be positive");

            _camera._vpCenter = _camera._p0.add(_camera._vTo.scale(_camera._distance));
            _camera._pixelWidth = _camera._width / _camera._nX;
            _camera._pixelHeight = _camera._height / _camera._nY;
        }
    }
}