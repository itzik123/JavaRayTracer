package primitives;

public class Vector extends Point {

    public static final Vector AXIS_X = new Vector(1,0,0);
    public static final Vector AXIS_Y = new Vector(0,1,0);
    public static final Vector AXIS_Z = new Vector(0,0,1);

    public Vector(double x, double y, double z) {
        super(x, y, z);
        if (_xyz.equals(Double3.ZERO)) {
            throw new IllegalArgumentException("The vector cannot be (0,0,0)");
        }

    }

    public Vector(Double3 xyz) {
        super(xyz);
        if (xyz.equals(Double3.ZERO)) {
            throw new IllegalArgumentException("The vector cannot be (0,0,0)");
        }

    }

    public Vector add(Vector other){
        return new Vector(_xyz.add(other._xyz));
    }

    public Vector scale(double rhs){
        return new Vector(_xyz.scale(rhs));
    }

    public double dotProduct(Vector rhs){
        Double3 product = _xyz.product(rhs._xyz);
        return product._d1() + product._d2() + product._d3();
    }


    public Vector crossProduct(Vector rhs) {
        return new Vector(
                _xyz._d2() * rhs._xyz._d3() - _xyz._d3() * rhs._xyz._d2(),
                _xyz._d3() * rhs._xyz._d1() - _xyz._d1() * rhs._xyz._d3(),
                _xyz._d1() * rhs._xyz._d2() - _xyz._d2() * rhs._xyz._d1()
        );
    }


    public double lengthSquared() {
        return super.distanceSquared(ZERO);
    }

    public double length() {
        return Math.sqrt(lengthSquared()); //SoC
    }

    public Vector normalize(){
        return new Vector(_xyz.divide(length()));
    }


    @Override
    public String toString() {
        return "->" + super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return super.equals(obj);
    }
}