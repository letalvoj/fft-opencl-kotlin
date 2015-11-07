package cz.letalvoj.gpgpu.spectrogram

import cz.letalvoj.gpgpu.fft.OpenCLFFTCalculator
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.util.concurrent.LinkedBlockingDeque


/**
 * This is just an example class. The 1.5s time window is too long to visualize sudden changes in the signal, especially
 * in high frequencies.
 *
 * Without the GPU acceleration the CPU is too slow to keep up the 32 FPS.
 *
 * !!! -> Run with:  -Djava.library.path=./native  -Dcom.amd.aparapi.executionMode=%1
 */
class Spectrogram : Application() {

    val FPS = 16
    val SAMPLES = 65536

    val mic = Mic(SAMPLES / FPS)
    val fft = OpenCLFFTCalculator(SAMPLES)
    val buffer = LinkedBlockingDeque<DoubleArray>(arrayListOf())

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Drawing Operations Test"
        val root = Group()
        val canvas = Canvas(640.0, 240.0)

        mic.addListener({ data: DoubleArray ->
            buffer.offer(data)
            while (buffer.size > FPS)
                buffer.poll()

            if (buffer.size == FPS) {
                val packed = buffer.flatMap { it.asIterable() }.toDoubleArray()
                val amplitudeSpectrum = fft.calculate(packed).amplitude()

                drawSpectrogram(canvas, amplitudeSpectrum)
            }

        })
        mic.start()

        root.children.add(canvas)
        primaryStage.scene = Scene(root)
        primaryStage.show()

    }

    private fun drawSpectrogram(canvas: Canvas, data: DoubleArray) {
        val gc = canvas.graphicsContext2D
        //TODO this should be plotted in a log scale
        gc.fill = Color.GREEN
        gc.stroke = Color.BLUE
        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)

        val logMax = Math.log(1280.0)
        for (i in 1..640) {
            val j = i * 50

            val x = i.toDouble()
            val y = (1 - data[j]) * 220

            gc.fillOval(x, y, 1.0, 1.0)
        }
    }

    override fun stop() {
        super.stop()
        this.mic.stop()
        this.fft.close()
    }
}

fun main(args: Array<String>) {
    Application.launch(Spectrogram::class.java, *args)
}
