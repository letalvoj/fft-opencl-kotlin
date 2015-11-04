package cz.letalvoj.gpgpu

import com.amd.aparapi.Kernel
import com.amd.aparapi.Range

interface I {
    fun f(i: Int): Int
}

fun main(_args: Array<String>) {

    val size = 512

    val a = FloatArray(size)
    val b = FloatArray(size)

    for (i in 0..size - 1) {
        a[i] = (Math.random() * 100).toFloat()
        b[i] = (Math.random() * 100).toFloat()
    }

    val sum = FloatArray(size)
    val kernel = object : Kernel() {
        override fun run() {
            val gid = globalId
            sum[gid] = a[gid] + b[gid]
        }
    }

    kernel.execute(Range.create(512))

    for (i in 0..size - 1) {
        System.out.printf("%6.2f + %6.2f = %8.2f\n", a[i], b[i], sum[i])
    }

    kernel.dispose()
}
