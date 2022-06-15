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
import java.lang.IllegalArgumentException
import java.net.URI
import java.net.URISyntaxException

/**
 * @author Niki Izvorski on 12/08/21.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class EidTest {

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test01_getEndpointId() {
        /* Given */
        val uri = URI.create("dtn://api:me/")

        /* Then */
        assertEquals(uri.getEndpoint(), URI.create("dtn://api:me/"))
    }

    @Test
    fun test03_getDemux() {
        /* Given */
        val uri = URI.create("dtn://api:me/test?test#test")

        /* Then */
        assertEquals(uri.getDemux(), "/test?test#test")
    }

    @Test
    fun test04_hasSameScheme() {
        /* Given */
        val uri = URI.create("dtn://api:me/")
        val test = URI.create("dtn://api:me/")

        /* Then */
        assertTrue(uri.hasSameScheme(test))
    }

    @Test
    fun test05_hasSameAuthority() {
        /* Given */
        val uri = URI.create("dtn://api:me/")
        val test = URI.create("dtn://api:me/")

        /* Then */
        assertTrue(uri.hasSameAuthority(test))
    }

    @Test
    fun test06_hasSamePath() {
        /* Given */
        val uri = URI.create("dtn://api:me/")
        val test = URI.create("dtn://api:me/")

        /* Then */
        assertTrue(uri.hasSamePath(test))
    }

    @Test
    fun test07_matchAuthority() {
        /* Given */
        val uri = URI.create("dtn://api:me/")
        val test = URI.create("dtn://api:me/")

        /* Then */
        assertTrue(uri.matchAuthority(test))
    }

    @Test
    fun test08_matchEndpoint() {
        /* Given */
        val uri = URI.create("dtn://api:me/")
        val test = URI.create("dtn://api:me/")

        /* Then */
        assertTrue(uri.matchEndpoint(test))
    }

    @Test
    fun test09_getEndpointSelf(){
        /* Given */
        val uri = URI.create("dtn:none")

        /* Then */
        assertEquals(uri.getEndpoint(), uri)
    }

    @Test
    fun test10_getEndpointSelfNotDtn(){
        /* Given */
        val uri = URI.create("/test")

        /* Then */
        assertEquals(uri.getEndpoint(), uri)
    }

    @Test(expected = IllegalArgumentException::class)
    fun test11_getEndpointException(){
        /* Given */
        val uri = URI.create("dtn:\\api:me\test")

        /* Then */
        assertEquals(uri.getEndpoint(), uri)
    }
}
