package cz.letalvoj.gpgpu

import com.amd.aparapi.Kernel


object KotlinCL {
    fun compileKernel(kernelFunction: (Int) -> Unit): Kernel = object : Kernel() {
        override fun run() {
            kernelFunction(globalId)
        }
    }
}