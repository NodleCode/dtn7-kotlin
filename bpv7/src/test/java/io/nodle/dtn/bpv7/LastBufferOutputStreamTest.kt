package io.nodle.dtn.bpv7

import io.nodle.dtn.utils.LastBufferOutputStream
import io.nodle.dtn.utils.hexToBa
import org.junit.Assert
import org.junit.Test

/**
 * @author Lucien Loiseau on 14/02/21.
 */
class LastBufferOutputStreamTest {
    @Test
    fun testBuffer() {
        var buf = LastBufferOutputStream(2)
        buf.write("00112233445566778899".hexToBa())
        Assert.assertArrayEquals("8899".hexToBa(), buf.last())

        buf = LastBufferOutputStream(5)
        buf.write("0011".hexToBa())
        Assert.assertArrayEquals("0011".hexToBa(), buf.last())

        buf = LastBufferOutputStream(3)
        buf.write("00112233445566".hexToBa())
        Assert.assertArrayEquals("445566".hexToBa(), buf.last())
    }
}