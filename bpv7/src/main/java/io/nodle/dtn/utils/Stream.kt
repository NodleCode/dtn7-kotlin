package io.nodle.dtn.utils

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * @author Lucien Loiseau on 24/02/21.
 */
class DualOutputStream(
        private val out1 : OutputStream,
        private val out2 : OutputStream) : OutputStream() {

    override fun write(p0: Int) {
        out1.write(p0)
        out2.write(p0)
    }

}

class CountingInputStream(private val ins : InputStream) : InputStream() {
    private var counter : Int = 0
    override fun read(): Int {
        val r = ins.read()
        counter++
        return r
    }
    fun bytesRead(): Int = counter
}

class CountingOutputStream(private val ins : OutputStream) : OutputStream() {
    private var counter : Int = 0
    override fun write(p0: Int) {
        ins.write(p0)
        counter++
    }
    fun bytesSent(): Int = counter
}

class CloseProtectOutputStream(private val out: OutputStream): OutputStream() {

    override fun write(p0: Int) {
        out.write(p0)
    }

    override fun close() {
    }

}

class LastBufferOutputStream(val size: Int) : OutputStream() {
    private val window = ByteBuffer.allocate(size)
    private var counter = 0

    override fun write(p0: Int) {
        counter++
        window.put(p0.toByte())
        if (window.remaining() == 0) {
            window.position(0)
        }
    }

    fun last(): ByteArray {
        if (counter < size) {
            return window.array().copyOfRange(0,counter)
        }

        val queue = (counter - size) % size
        return window.array().copyOfRange(queue, size) +
                window.array().copyOfRange(0, queue)
    }
}