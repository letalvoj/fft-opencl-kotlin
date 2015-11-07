package cz.letalvoj.gpgpu.spectrogram

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.LinkedBlockingDeque
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

/**
 * This class encapsulates the logic needed to read data from the microphone.
 *
 */
public class Mic(val buffSize: Int) {

    private val listeners = LinkedBlockingDeque<MicListener>()
    private var running = true
    private var byteBuffSize = buffSize * 2

    fun addListener(listener: MicListener) {
        listeners.add(listener)
    }

    fun addListener(listener: (DoubleArray) -> Unit) {
        listeners.add(object : MicListener {
            override fun handleData(signal: DoubleArray) {
                listener.invoke(signal)
            }
        })
    }

    fun start() {
        Thread {
            val audioFormat = AudioFormat(44100f, 16, 1, true, true)
            val targetInfo = DataLine.Info(TargetDataLine::class.java, audioFormat)

            val targetLine = AudioSystem.getLine(targetInfo) as TargetDataLine
            targetLine.open(audioFormat)
            targetLine.start()

            val targetData = ByteArray(byteBuffSize)

            while (running) {
                val numBytesRead = targetLine.read(targetData, 0, byteBuffSize)

                if (numBytesRead == -1)
                    break

                val floats = decodePcm(targetData, byteBuffSize)

                listeners.forEach {
                    it.handleData(floats)
                }
            }

            targetLine.close()
        }.start()

    }

    fun stop() {
        running = false
    }

    fun decodePcm(pcms: ByteArray, len: Int): DoubleArray {
        val shortBuff = ByteBuffer.wrap(pcms, 0, len).order(ByteOrder.BIG_ENDIAN).asShortBuffer()

        val shorts = ShortArray(shortBuff.capacity());
        shortBuff.get(shorts, 0, len / 2);

        return shorts.map { it.toDouble() / Short.MAX_VALUE }.toDoubleArray()
    }

}

interface MicListener {
    fun handleData(signal: DoubleArray)
}