package cz.letalvoj.gpgpu.fft

import com.amd.aparapi.Range

/**
 * Unified interface for both CPU and GPU implementation of the FFT algorithm.
 */
interface FFTCalculator {
    fun calculate(signal: DoubleArray): FrequencySpectrum
    fun close()
}

class OpenCLFFTCalculator(val size: Int) : FFTCalculator {

    private val kernel = FFTKernel(size)

    override fun calculate(signal: DoubleArray): FrequencySpectrum {
        for (i in 0..kernel.N - 1)
            kernel.setInputValue(i, signal[i].toFloat(), 0.0f)

        kernel.execute(Range.create(size), kernel.logN)

        val real = toDoubleArray(kernel.resultRe)
        val imag = toDoubleArray(kernel.resultIm)

        return FrequencySpectrum(real, imag)
    }

    override fun close() = kernel.dispose()

    private fun toDoubleArray(outputRe: FloatArray): DoubleArray = outputRe.map { it.toDouble() }.toDoubleArray()

}

data class FrequencySpectrum(val real: DoubleArray, val imag: DoubleArray) {

    fun amplitude(): DoubleArray = mapResult { re, im -> Math.hypot(im, re) }

    fun phase(): DoubleArray = mapResult { re, im -> Math.atan2(im, re) }

    private fun mapResult(f: (Double, Double) -> Double): DoubleArray {
        val result = DoubleArray(real.size)

        for (i in 1..real.size - 1)
            result[i] = f(real[i], imag[i])

        return result
    }

}