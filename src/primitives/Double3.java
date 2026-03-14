/**
 * 
 */
package primitives;

import static primitives.Util.isZero;

import java.util.Objects;

/**
 * Immutable container for three double values.
 * <p>
 * The class is used as a basic numeric structure in the primitives package.
 * Typical uses include:
 * <ul>
 * <li>RGB color components</li>
 * <li>Point or vector coordinates</li>
 * <li>Material or attenuation coefficients</li>
 * </ul>
 * </p>
 * This class is intentionally minimal and optimized for performance,
 * as it is heavily used in geometric and color computations. It performs no
 * argument validation (such as {@code null} checks or division-by-zero checks).
 * It assumes correct usage by the calling code.
 * @param  _d1 first component
 * @param  _d2 second component
 * @param  _d3 third component
 * @author     Dan Zilberstein
 */
public record Double3(double _d1, double _d2, double _d3) {

   /** Constant triad (0,0,0) */
   public static final Double3 ZERO = new Double3(0, 0, 0);

   /** Constant triad (1,1,1) */
   public static final Double3 ONE = new Double3(1, 1, 1);

   /**
    * Constructor to initialize Double3 based object the same number values
    * @param value number value for all 3 numbers
    */
   public Double3(double value) { this(value, value, value); }

   @Override
   public boolean equals(Object obj) {
      return this == obj
            || (obj instanceof Double3(double od1, double od2, double od3))
                  && isZero(_d1 - od1)
                  && isZero(_d2 - od2)
                  && isZero(_d3 - od3);
   }

   @Override
   public int hashCode() { return Objects.hash(_d1, _d2, _d3); }

   @Override
   public String toString() { return "(" + _d1 + "," + _d2 + "," + _d3 + ")"; }

   /**
    * Adds this triad to another triad component-wise.
    * @param  rhs the triad to add
    * @return     a new {@code Double3} containing the component-wise sum
    */
   public Double3 add(Double3 rhs) { return new Double3(_d1 + rhs._d1, _d2 + rhs._d2, _d3 + rhs._d3); }

   /**
    * Subtracts another triad from this triad component-wise.
    * @param  rhs the triad to subtract
    * @return     a new {@code Double3} containing the component-wise difference
    */
   public Double3 subtract(Double3 rhs) { return new Double3(_d1 - rhs._d1, _d2 - rhs._d2, _d3 - rhs._d3); }

   /**
    * Multiplies all components of this triad by a scalar value.
    * @param  rhs the scaling factor
    * @return     a new {@code Double3} containing the scaled values
    */
   public Double3 scale(double rhs) { return new Double3(_d1 * rhs, _d2 * rhs, _d3 * rhs); }

   /**
    * Divides all components of this triad by a scalar value.
    * @param  rhs the divisor
    * @return     a new {@code Double3} containing the reduced values
    */
   public Double3 divide(double rhs) { return new Double3(_d1 / rhs, _d2 / rhs, _d3 / rhs); }

   /**
    * Multiplies this triad with another triad component-wise.
    * @param  rhs the triad to multiply with
    * @return     a new {@code Double3} containing the component-wise product
    */
   public Double3 product(Double3 rhs) { return new Double3(_d1 * rhs._d1, _d2 * rhs._d2, _d3 * rhs._d3); }

   /**
    * Checks whether all components are smaller than a given value.
    * @param  k the value to compare against
    * @return   {@code true} if all components are smaller than {@code k}
    */
   public boolean isLowerThan(double k) { return _d1 < k && _d2 < k && _d3 < k; }

   /**
    * Checks whether all components of this triad are smaller than the
    * corresponding components of another triad.
    * @param  other the triad to compare with
    * @return       {@code true} if each component of this triad is smaller than
    *               the corresponding component in {@code other}
    */
   public boolean isLowerThan(Double3 other) { return _d1 < other._d1 && _d2 < other._d2 && _d3 < other._d3; }
}
