package cz.letalvoj.gpgpu.equalizer

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.LinkedBlockingDeque
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

/**
 * This class encapsulates the logic needed to read data from microphone.
 */
public class Mic(val buffSize: Int) {

    private val listeners = LinkedBlockingDeque<(DoubleArray) -> Unit>()
    private var byteBuffSize = buffSize * 2
    private var running = true

    fun addListener(listener: (DoubleArray) -> Unit) {
        listeners.offer(listener)
    }

    fun isRunning(): Boolean {
        return running
    }

    fun start() {
        Thread {
            val format = AudioFormat(44100f, 16, 1, true, true)
            val info = DataLine.Info(TargetDataLine::class.java, format)
            val line = AudioSystem.getLine(info) as TargetDataLine
            val buffer = ByteArray(byteBuffSize)

            line.open(format)
            line.start()

            while (running) {
                val numBytesRead = line.read(buffer, 0, byteBuffSize)

                if (numBytesRead < 0)
                    break

                val floats = decodePcm(buffer, byteBuffSize)

                listeners.forEach {
                    it.invoke(floats)
                }
            }

            line.close()
        }.start()

    }

    fun decodePcm(pcms: ByteArray, len: Int): DoubleArray {
        val shortBuff = ByteBuffer.wrap(pcms, 0, len).order(ByteOrder.BIG_ENDIAN).asShortBuffer()

        val shorts = ShortArray(shortBuff.capacity());
        shortBuff.get(shorts, 0, len / 2);

        return shorts.map { it.toDouble() / Short.MAX_VALUE }.toDoubleArray()
    }

    fun stop() {
        running = false
    }

}
