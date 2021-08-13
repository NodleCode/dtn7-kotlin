package io.nodle.dtn.bpv7

import io.nodle.dtn.crypto.CRC16X25
import io.nodle.dtn.crypto.CRC32C
import io.nodle.dtn.utils.hexToBa
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule

/**
 * @author Lucien Loiseau on 13/02/21.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class CRCTest {
    private val crc = CRC16X25()
    private val crc32 = CRC32C()

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test01_CRC16X25() {
        /* When */
        crc.write("123456789".toByteArray())

        /* Then */
        Assert.assertArrayEquals("906e".hexToBa(), crc.done())
    }

    @Test
    fun test02_CRC16X25() {
        /* When */
        crc.write("FFFFFFFF".hexToBa())

        /* Then */
        Assert.assertArrayEquals("0f47".hexToBa(), crc.done())
    }

    @Test
    fun test03_CRC16X25() {
        /* When */
        crc.write("00112233445566778899aabbccddeeff".hexToBa())

        /* Then */
        Assert.assertArrayEquals("8f52".hexToBa(), crc.done())
    }

    @Test
    fun test04_CRC16X25() {
        /* When */
        crc.write("12345".toByteArray())
        crc.write("6789".toByteArray())

        /* Then */
        Assert.assertArrayEquals("906e".hexToBa(), crc.done())
    }

    @Test
    fun test05_CRC16X25() {
        /* When */
        crc.write("this is some random string".toByteArray())

        /* Then */
        Assert.assertArrayEquals("7c29".hexToBa(), crc.done())
    }

    @Test
    fun test06_CRC16X25() {
        /* When */
        crc.write("this is ".toByteArray())
        crc.write("some random ".toByteArray())
        crc.write("string".toByteArray())

        /* Then */
        Assert.assertArrayEquals("7c29".hexToBa(), crc.done())
    }


    @Test
    fun test07_CRC32() {
        /* When */
        crc32.write("123456789".toByteArray())

        /* Then */
        Assert.assertArrayEquals("E3069283".hexToBa(), crc32.done())
    }

    @Test
    fun test08_CRC32() {
        /* When */
        crc32.write("FFFFFFFF".hexToBa())

        /* Then */
        Assert.assertArrayEquals("FFFFFFFF".hexToBa(), crc32.done())
    }

    @Test
    fun test09_CRC32() {
        /* When */
        crc32.write("00112233445566778899aabbccddeeff".hexToBa())

        /* Then */
        Assert.assertArrayEquals("48DFE982".hexToBa(), crc32.done())
    }

    @Test
    fun test10_CRC32() {
        /* When */
        crc32.write("12345".toByteArray())
        crc32.write("6789".toByteArray())

        /* Then */
        Assert.assertArrayEquals("E3069283".hexToBa(), crc32.done())
    }

    @Test
    fun test11_CRC32() {
        /* When */
        crc32.write("this is some random string".toByteArray())

        /* Then */
        Assert.assertArrayEquals("28F4138D".hexToBa(), crc32.done())
    }

    @Test
    fun test12_CRC32() {
        /* When */
        crc32.write("this is ".toByteArray())
        crc32.write("some random ".toByteArray())
        crc32.write("string".toByteArray())

        /* Then */
        Assert.assertArrayEquals("28F4138D".hexToBa(), crc32.done())
    }

}