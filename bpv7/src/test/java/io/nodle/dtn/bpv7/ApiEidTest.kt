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
import java.net.URI
import java.net.URISyntaxException

/**
 * @author Niki Izvorski on 12/08/21.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class ApiEidTest {

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test01_ApiMePath() {
        /* Given */
        val test = "/test"

        /* Then */
        assertEquals(apiMe(test), URI.create("dtn://api:me/test"))
    }

    @Test
    fun test02_ApiMePathWithQ() {
        /* Given */
        val test = "/test"
        val query = "test"

        /* Then */
        assertEquals(apiMe(test, query), URI.create("dtn://api:me/test?test"))
    }

    @Test
    fun test03_ApiMePathWithQFragment() {
        /* Given */
        val test = "/test"
        val query = "test"
        val fragment = "test"

        /* Then */
        assertEquals(apiMe(test, query, fragment),URI.create("dtn://api:me/test?test#test"))
    }

    @Test
    fun test04_swapApiMeUnsafe() {
        /* Given */
        val test = "/test"
        val query = "test"
        val fragment = "test"
        val swap = apiMe(test, fragment, query)
        val uri = apiMe(test)

        /* Then */
        assertEquals(uri?.swapApiMeUnsafe(swap!!), uri)
    }

    @Test
    fun test05_isApiEid() {
        /* Given */
        val uri = apiMe()

        /* Check */
        assertTrue(uri.isApiEid())
    }

    @Test
    fun test06_checkValidDtnEid() {
        /* Given */
        val uri = apiMe()

        /* Check */
        assertNotNull(uri.checkValidApiEid())
    }

    @Test(expected = InvalidDtnEid::class)
    fun test06_checkValidDtnEidFail() {
        /* Given */
        val uri = URI.create("dtn://test")

        /* When */
        uri.checkValidApiEid()
    }

    @Test(expected = InvalidApiEid::class)
    fun test07_checkValidDtnEidFail() {
        /* Given */
        val uri = URI.create("dtn://api-test/")

        /* When */
        uri.checkValidApiEid()
    }

    @Test(expected = IllegalArgumentException::class)
    fun test08_swapApiMeUnsafeFail() {
        /* Given */
        val test = URI.create("test")
        val uri = URI.create("dtn://api-test/")

        /* When */
        uri.swapApiMeUnsafe(test)
    }


    @Test(expected = IllegalArgumentException::class)
    fun test09_swapApiMeUnsafeFail() {
        /* Given */
        val test = URI.create("test://api:me/^NIKI")
        val uri = URI.create("dtn://api:me/NIKI")

        /* When */
        uri.swapApiMeUnsafe(test)
    }
}
