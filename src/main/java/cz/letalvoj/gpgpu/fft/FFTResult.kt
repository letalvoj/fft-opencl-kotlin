package cz.letalvoj.gpgpu.fft

import com.amd.aparapi.Range

data class ComplexArray(val real: Array<Float>, val imaginary: Array<Float>)


fun main(_args: Array<String>) {
    //TODO clean up the code and add JUnit test - just to be sure

    for (l in 3..28) {
        val size = Math.pow(2.0, l.toDouble()).toInt()
        val passes = Integer.numberOfTrailingZeros(size)

        val inputRe = FloatArray(size)
        val inputIm = FloatArray(size)
        val outputRe = FloatArray(size)
        val outputIm = FloatArray(size)

        for (i in 0..size - 1) {
            inputRe[i] = i.toFloat()
            inputIm[i] = 0f
        }

        var kernel: FFTKernel? = FFTKernel(inputRe, inputIm, outputRe, outputIm, size)

        //    println("inputRe: " + Arrays.toString(inputRe))
        //    println("inputIm: " + Arrays.toString(inputIm))
        //    println("outputRe: " + Arrays.toString(outputRe))
        //    println("outputIm: " + Arrays.toString(outputIm))
        //    println()

        measuteTime(false) {
            kernel?.execute(Range.create(size), passes)

            //        println("inputRe: " + Arrays.toString(inputRe))
            //        println("inputIm: " + Arrays.toString(inputIm))
            //        println("outputRe: " + Arrays.toString(outputRe))
            //        println("outputIm: " + Arrays.toString(outputIm))
            //        println()
        }

        val timeGpu = measuteTime(false) {
            for (i in 0..size - 1) {
                inputRe[i] = i.toFloat()
                inputIm[i] = 0f
            }

            kernel?.execute(Range.create(size), passes)

            //        println("inputRe: " + Arrays.toString(inputRe))
            //        println("inputIm: " + Arrays.toString(inputIm))
            //        println("outputRe: " + Arrays.toString(outputRe))
            //        println("outputIm: " + Arrays.toString(outputIm))
            //        println()

            kernel?.dispose()
        }

        measuteTime(false) {
            FFTCpu.experiment(size)
        }

        val timeCpu = measuteTime(false) {
            FFTCpu.experiment(size)
        }

        println("$l $timeCpu $timeGpu")
    }
}

fun measuteTime(verbose: Boolean, f: () -> Unit): Long {
    val start = System.currentTimeMillis()
    f()
    val end = System.currentTimeMillis()
    val duration = end - start

    //    if (verbose)
    //        println("It took: " + (end - start))

    return duration;
}