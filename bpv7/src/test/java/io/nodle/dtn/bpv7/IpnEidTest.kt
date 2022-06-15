package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.eid.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import java.lang.AssertionError
import java.net.URI

/**
 * @author Niki Izvorski on 12/08/21.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class IpnEidTest {

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test01_createIpnEid() {
        /* Given */
        val uri = createIpn(0, 0)

        /* Then */
        assertEquals(uri.getServiceNumber(), 0)
    }

    @Test
    fun test02_checkValidIpnEid() {
        /* Given */
        val uri = URI.create("ipn:0.0")

        /* Then */
        assertTrue(uri.isIpnEid())
    }

    @Test
    fun test03_getNodeNumber() {
        /* Given */
        val uri = URI.create("ipn:0.0")

        /* Then */
        assertEquals(uri.getNodeNumber(), 0)
    }

    @Test
    fun test04_getNodeNumberUnsafe() {
        /* Given */
        val uri = URI.create("ipn:0.0")

        /* Then */
        assertEquals(uri.getNodeNumberUnsafe(), 0)
    }

    @Test
    fun test05_getServiceNumber() {
        /* Given */
        val uri = URI.create("ipn:0.0")

        /* Then */
        assertEquals(uri.getServiceNumber(), 0)
    }

    @Test
    fun test06_getServiceNumberUnsafe() {
        /* Given */
        val uri = URI.create("ipn:0.0")

        /* Then */
        assertEquals(uri.getServiceNumberUnsafe(), 0)
    }

    @Test(expected = InvalidIpnEid::class)
    fun test07_checkIpnSchemeAndPathFail() {
        /* Given */
        val uri = URI.create("ipn://test")

        /* Then */
        uri.checkIpnSchemeAndPath()
    }

    @Test(expected = InvalidIpnEid::class)
    fun test08_checkValidIpnEidFail() {
        /* Given */
        val uri = URI.create("ipn://test")

        /* Then */
        uri.checkValidIpnEid()
    }

    @Test(expected = IllegalArgumentException::class)
    fun test09_getNodeNumberUnsafeFail() {
        /* Given */
        val uri = URI.create("ipn://test")

        /* Then */
        uri.getNodeNumberUnsafe()
    }

    @Test(expected = IllegalArgumentException::class)
    fun test10_getServiceNumberUnsafeFail() {
        /* Given */
        val uri = URI.create("ipn://test")

        /* Then */
        uri.getServiceNumberUnsafe()
    }

    @Test(expected = InvalidIpnEid::class)
    fun test11_checkIpnAuthoryFail() {
        /* Given */
        val uri = URI.create("ipn:0.0/test")

        /* Then */
        uri.checkValidIpnEid()
    }
}
