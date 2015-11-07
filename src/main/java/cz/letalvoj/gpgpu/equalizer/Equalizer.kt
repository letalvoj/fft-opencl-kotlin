package cz.letalvoj.gpgpu.equalizer

import cz.letalvoj.gpgpu.fft.OpenCLFFTCalculator
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

/**
 * This class represents a simple equalizer, which shows an amplitude spectrum of signal read from microphone.
 */
class Equalizer : Application() {

    // Redraws per second = SAMPLING RATE / SAMPLES * CHUNKS = 44100 / 4096 * 2 ~= 22

    // both of them must be of power of two
    private val SAMPLES = 4096
    private val CHUNKS = 2

    private val microphone = Mic(SAMPLES / CHUNKS)
    private val fftCalculator = OpenCLFFTCalculator(SAMPLES)

    private val chunkBuffer = LinkedList<DoubleArray>(arrayListOf())
    private val sampleQueue = LinkedBlockingQueue<DoubleArray>(arrayListOf())


    override fun start(primaryStage: Stage) {
        primaryStage.title = "Equalizer"
        val root = Group()
        val canvas = Canvas(1024.0, 512.0)

        microphone.start()
        microphone.addListener { processChunkFromMic(it) }

        Thread {
            while (microphone.isRunning()) {
                val data = sampleQueue.take()
                val amplitudeSpectrum = fftCalculator.calculate(data).amplitude()

                drawFrequencies(canvas, amplitudeSpectrum)
            }
        }.start()

        root.children.add(canvas)
        primaryStage.scene = Scene(root)
        primaryStage.show()

        canvas.widthProperty().bind(primaryStage.widthProperty());
        canvas.heightProperty().bind(primaryStage.heightProperty());
    }

    private fun processChunkFromMic(data: DoubleArray) {
        chunkBuffer.offer(data)
        while (chunkBuffer.size > CHUNKS)
            chunkBuffer.poll()

        if (chunkBuffer.size == CHUNKS && sampleQueue.size < 2) {
            val packed = chunkBuffer.flatMap { it.asIterable() }.toDoubleArray()

            sampleQueue.offer(packed)
        }
    }

    private fun drawFrequencies(canvas: Canvas, spectrum: DoubleArray) {
        val gc = canvas.graphicsContext2D
        val width = canvas.width
        val height = canvas.height

        gc.fill = Color.RED
        gc.clearRect(0.0, 0.0, width, height)

        val averaged = movingAverage(spectrum, 8, spectrum.size / 2)

        for ((i, v) in averaged.withIndex()) {
            val x = width / averaged.size * i
            val y = (0.9 - v / 2) * height

            gc.fillOval(x, y, 2.0, 2.0)
        }
    }

    private fun movingAverage(spectrum: DoubleArray, windowSize: Int, length: Int): List<Double> {
        val averaged = (1..length / windowSize).map { i ->
            val v = i * windowSize
            val u = v - windowSize

            // there is a bug in the slice() method, so it can not be used...
            (u..v).map { spectrum[it] }.sum() / windowSize
        }
        return averaged
    }

    override fun stop() {
        super.stop()

        this.microphone.stop()
        this.fftCalculator.close()
    }
}
