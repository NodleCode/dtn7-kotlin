package io.nodle.dtn.bpv7

import io.nodle.dtn.crypto.CRC16X25
import io.nodle.dtn.crypto.CRC32C
import io.nodle.dtn.utils.hexToBa
import org.junit.Assert
import org.junit.Test

/**
 * @author Lucien Loiseau on 13/02/21.
 */
class CRCTest {

    @Test
    fun testCRC16X25() {
        var crc = CRC16X25()
        crc.write("123456789".toByteArray())
        Assert.assertArrayEquals("906e".hexToBa(), crc.done())

        crc = CRC16X25()
        crc.write("FFFFFFFF".hexToBa())
        Assert.assertArrayEquals("0f47".hexToBa(), crc.done())

        crc = CRC16X25()
        crc.write("00112233445566778899aabbccddeeff".hexToBa())
        Assert.assertArrayEquals("8f52".hexToBa(), crc.done())

        crc = CRC16X25()
        crc.write("12345".toByteArray())
        crc.write("6789".toByteArray())
        Assert.assertArrayEquals("906e".hexToBa(), crc.done())


        crc = CRC16X25()
        crc.write("this is some random string".toByteArray())
        Assert.assertArrayEquals("7c29".hexToBa(), crc.done())

        crc = CRC16X25()
        crc.write("this is ".toByteArray())
        crc.write("some random ".toByteArray())
        crc.write("string".toByteArray())
        Assert.assertArrayEquals("7c29".hexToBa(), crc.done())
    }


    @Test
    fun testCRC32() {
        var crc = CRC32C()
        crc.write("123456789".toByteArray())
        Assert.assertArrayEquals("E3069283".hexToBa(), crc.done())

        crc = CRC32C()
        crc.write("FFFFFFFF".hexToBa())
        Assert.assertArrayEquals("FFFFFFFF".hexToBa(), crc.done())

        crc = CRC32C()
        crc.write("00112233445566778899aabbccddeeff".hexToBa())
        Assert.assertArrayEquals("48DFE982".hexToBa(), crc.done())

        crc = CRC32C()
        crc.write("12345".toByteArray())
        crc.write("6789".toByteArray())
        Assert.assertArrayEquals("E3069283".hexToBa(), crc.done())


        crc = CRC32C()
        crc.write("this is some random string".toByteArray())
        Assert.assertArrayEquals("28F4138D".hexToBa(), crc.done())

        crc = CRC32C()
        crc.write("this is ".toByteArray())
        crc.write("some random ".toByteArray())
        crc.write("string".toByteArray())
        Assert.assertArrayEquals("28F4138D".hexToBa(), crc.done())
    }

}