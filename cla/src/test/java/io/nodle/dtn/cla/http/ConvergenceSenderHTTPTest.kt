package io.nodle.dtn.cla.http

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.cla.StaticRoutingTable
import io.nodle.dtn.crypto.Ed25519Util
import io.nodle.dtn.interfaces.IAgent
import io.nodle.dtn.interfaces.IConvergenceLayerSender
import io.nodle.dtn.interfaces.IRegistrar
import io.nodle.dtn.interfaces.IRouter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.junit.Assert.*

import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.net.URI

@ExperimentalCoroutinesApi
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class ConvergenceSenderHTTPTest {
    private val uri = URI.create("https://dtn.nodle.io/outbox")
    private val localNodeId = URI.create("dtn://test/")
    private val remoteNodeId = URI.create("dtn://nodle/dtn-router")
    private val keyPair = Ed25519Util.generateEd25519KeyPair()
    private val router = StaticRoutingTable()

    private val outBundle1 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    private val registrar = mock<IRegistrar>()

    private val cla = mock<IConvergenceLayerSender>() {
        on { mock.getPeerEndpointId() } doReturn uri
    }

    private val agent = mock<IAgent>() {
        on { mock.nodeId() } doReturn URI.create("dtn://test/")
        on { mock.getRouter() } doReturn router
        on { mock.getRegistrar() } doReturn registrar
    }

    private var http = ConvergenceSenderHTTP(agent, uri)

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test0_getPeerEndpointId() {
        /* Check */
        assertEquals(http.getPeerEndpointId(), uri)
    }

    @Test
    fun test1_sendBundle() {
        /* Check */
        runBlockingTest {
            assertTrue(http.sendBundle(outBundle1))
        }
    }

    @Test
    fun test2_sendBundles() {
        /* Given */
        val bundles = listOf(outBundle1)

        /* Check */
        runBlockingTest {
            assertTrue(http.sendBundles(bundles))
        }
    }

    @Test
    fun test3_sendBundleConnectionFailed() {
        /* Given */
        val http = ConvergenceSenderHTTP(agent, URI.create("https://nodle.io"))

        /* THen */
        runBlockingTest {
            assertFalse(http.sendBundle(outBundle1))
        }
    }

    @Test
    fun test3_sendBundleLocalFail() {
        /* Given */
        val http = ConvergenceSenderHTTP(agent, URI.create("https://127.0.0.1"))

        /* Then */
        runBlockingTest {
            assertFalse(http.sendBundle(outBundle1))
        }
    }

    @Test
    fun test4_getRouterDefault() {
        /* When */
        http.agent.getRouter().setDefaultRoute(cla)

        /* Then */
        assertEquals(http.agent.getRouter().findRoute(outBundle1)?.getPeerEndpointId(), uri)
    }

    @Test
    fun test5_getRouterTable() {
        /* When */
        router.staticRoutes[remoteNodeId] = cla

        /* Then */
        assertEquals(http.agent.getRouter().findRoute(outBundle1)?.getPeerEndpointId(), uri)
    }

    @Test
    fun test6_getRouterDefault() {
        /* When */
        router.default = cla

        /* Check */
        assertNotNull(router.default)
    }

    @Test
    fun test7_getRouterDefaultTable() {
        /* Given */
        router.staticRoutes = mutableMapOf()

        /* Then */
        assertNotNull(router.staticRoutes)
    }

    @Test
    fun test8_getAgent() {
        /* Check */
        assertEquals(http.agent, agent)
    }

    @Test
    fun test9_getUrl() {
        /* Check */
        assertEquals(http.url, uri)
    }

    @Test
    fun test10_getRouterDefaultLog() {
        /* Check */
        assertEquals(router.log.name, "NOP")
    }
}