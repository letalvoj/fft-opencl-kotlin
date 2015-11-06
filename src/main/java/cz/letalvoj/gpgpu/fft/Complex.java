package cz.letalvoj.gpgpu.fft;

public class Complex {
    private final float re;
    private final float im;

    public Complex(float real, float imaginary) {
        this.re = real;
        this.im = imaginary;
    }

    public float magnitude() {
        return (float) Math.hypot(re, im);
    }

    public float phase() {
        return (float) Math.atan2(im, re);
    }

    public Complex plus(Complex that) {
        float real = this.re + that.re;
        float imag = this.im + that.im;

        return new Complex(real, imag);
    }

    public Complex minus(Complex that) {
        float real = this.re - that.re;
        float imag = this.im - that.im;

        return new Complex(real, imag);
    }

    public Complex times(Complex that) {
        float real = this.re * that.re - this.im * that.im;
        float imag = this.re * that.im + this.im * that.re;

        return new Complex(real, imag);
    }

    public Complex times(float alpha) {
        return new Complex(alpha * re, alpha * im);
    }

    public Complex conjugate() {
        return new Complex(re, -im);
    }

    public Complex reciprocal() {
        float scale = re * re + im * im;

        return new Complex(re / scale, -im / scale);
    }

    public float real() {
        return re;
    }

    public float imaginary() {
        return im;
    }

    public Complex divide(Complex that) {
        return this.times(that.reciprocal());
    }

    public Complex exp() {
        return new Complex((float) (Math.exp(re) * Math.cos(im)), (float) (Math.exp(re) * Math.sin(im)));
    }

    public Complex sin() {
        return new Complex((float) (Math.sin(re) * Math.cosh(im)), (float) (Math.cos(re) * Math.sinh(im)));
    }

    public Complex cos() {
        return new Complex((float) (Math.cos(re) * Math.cosh(im)), (float) (-Math.sin(re) * Math.sinh(im)));
    }

    public Complex tan() {
        return sin().divide(cos());
    }

    public static Complex plus(Complex a, Complex b) {
        float real = a.re + b.re;
        float imag = a.im + b.im;

        return new Complex(real, imag);
    }

    @Override
    public String toString() {
        return String.format("%.2f + %.2fj", re, im);
    }

}
