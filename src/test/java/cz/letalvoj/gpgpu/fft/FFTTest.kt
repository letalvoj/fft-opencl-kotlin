package cz.letalvoj.gpgpu.fft

import org.junit.Assert
import org.junit.Test

/**
 * Run it with: -Djava.library.path=./native  -Dcom.amd.aparapi.executionMode=%1
 */
class FFTTest {

    @Test
    fun testCalculateOddPower() {
        val len = 16
        val expected = FrequencySpectrum(
                doubleArrayOf(120.0, -8.0, -8.0, -8.0, -8.0, -8.0, -8.0, -8.0,
                        -8.0, -8.0, -8.0, -8.0, -8.0, -8.0, -8.0, -8.0
                ),
                doubleArrayOf(0.0, 40.2187, 19.3137, 11.9728, 8.0, 5.3454, 3.3137,
                        1.5913, 0.0, -1.5913, -3.3137, -5.3454, -8.0, -11.9728, -19.3137, -40.2187
                )
        );

        testCalculateTwice(len, expected)
    }


    @Test
    fun testCalculateEvenPower() {
        val len = 32
        val expected = FrequencySpectrum(
                doubleArrayOf(
                        496.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0,
                        -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0,
                        -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0, -16.0
                ),
                doubleArrayOf(
                        0.0, 162.4507, 80.4374, 52.7449, 38.6274, 29.9339, 23.9457, 19.4961, 16.0,
                        13.1309, 10.6909, 8.5522, 6.6274, 4.8535, 3.1826, 1.5759, 0.0, -1.5759,
                        -3.1826, -4.8535, -6.6274, -8.5522, -10.6909, -13.1309, -16.0, -19.4961,
                        -23.9457, -29.9339, -38.6274, -52.7449, -80.4374, -162.4507
                )
        );

        testCalculateTwice(len, expected)
    }

    private fun testCalculateTwice(len: Int, expected: FrequencySpectrum) {
        val fft = OpenCLFFTCalculator(len)

        testCalculate(fft, len, expected)

        fft.close()
    }

    fun testCalculate(fft: FFTCalculator, len: Int, expected: FrequencySpectrum) {
        val signal = DoubleArray(len)
        for (i in 0..len - 1)
            signal[i] = i.toDouble()

        val actual = fft.calculate(signal)

        Assert.assertArrayEquals("Real parts equal", expected.real, actual.real, 10E-5)
        Assert.assertArrayEquals("Imaginary parts equal", expected.imag, actual.imag, 10E-5)
    }

}