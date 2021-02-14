package io.nodle.dtn.utils

import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * @author Lucien Loiseau on 14/02/21.
 */
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
