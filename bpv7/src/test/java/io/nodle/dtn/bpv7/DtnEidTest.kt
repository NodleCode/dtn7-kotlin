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
class DtnEidTest {

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test01_generateDtnEid() {
        /* Given */
        val uri = generateDtnEid()

        /* Then */
        assertNotNull(uri)
    }

    @Test
    fun test02_createNodeDtnEid() {
        /* Given */
        val uri = createDtnEid("test")

        /* Then */
        assertEquals(uri, URI.create("dtn://test/"))
    }

    @Test
    fun test03_createPathDtnEid() {
        /* Given */
        val uri = createDtnEid("test", "/test")

        /* Then */
        assertEquals(uri, URI.create("dtn://test/test"))
    }

    @Test
    fun test04_createDtnEidQuery() {
        /* Given */
        val uri = createDtnEid("test", "/test", "test")

        /* Then */
        assertEquals(uri, URI.create("dtn://test/test?test"))
    }

    @Test
    fun test04_isSingleton() {
        /* Given */
        val uri = createDtnEid("test", "/test", "test")

        /* Then */
        assertTrue(uri.isSingleton())
    }

    @Test(expected = InvalidDtnEid::class)
    fun test05_checkAuthorityNotNullFail() {
        /* Given */
        val uri = URI.create("dtn://?/")

        /* When */
        uri.checkAuthorityNotNull()
    }

    @Test(expected = InvalidDtnEid::class)
    fun test06_checkSchemeNotNullFail() {
        /* Given */
        val uri = URI.create("?://test")

        /* When */
        uri.checkSchemeNotNull()
    }

    @Test(expected = InvalidDtnEid::class)
    fun test07_checkSchemePathFail() {
        /* Given */
        val uri = URI.create("mailto:niki@nodle.co")

        /* When */
        uri.checkPathNotNull()
    }
}
