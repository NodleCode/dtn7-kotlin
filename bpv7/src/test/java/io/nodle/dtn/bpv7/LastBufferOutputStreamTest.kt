package io.nodle.dtn.bpv7

import io.nodle.dtn.utils.LastBufferOutputStream
import io.nodle.dtn.utils.hexToBa
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule

/**
 * @author Lucien Loiseau on 14/02/21.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class LastBufferOutputStreamTest {

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test1_Buffer() {
        /* Given */
        val buf = LastBufferOutputStream(2)

        /* When */
        buf.write("00112233445566778899".hexToBa())

        /* Then */
        Assert.assertArrayEquals("8899".hexToBa(), buf.last())
    }

    @Test
    fun test2_Buffer() {
        /* Given */
        val buf = LastBufferOutputStream(5)

        /* When */
        buf.write("0011".hexToBa())

        /* Then */
        Assert.assertArrayEquals("0011".hexToBa(), buf.last())
    }

    @Test
    fun test3_Buffer() {
        /* Given */
        val buf = LastBufferOutputStream(3)

        /* When */
        buf.write("00112233445566".hexToBa())

        /* Then */
        Assert.assertArrayEquals("445566".hexToBa(), buf.last())
    }
}