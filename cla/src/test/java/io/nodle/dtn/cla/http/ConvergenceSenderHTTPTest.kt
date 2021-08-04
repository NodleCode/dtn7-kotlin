package io.nodle.dtn.cla.http

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.crypto.Ed25519Util
import io.nodle.dtn.interfaces.IAgent
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
import org.mockito.kotlin.verify
import java.net.URI

@ExperimentalCoroutinesApi
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class ConvergenceSenderHTTPTest {
    // TODO CLASS
    private val uri = URI.create("https://nodle.io")
    private val agent = mock<IAgent>() {
        on { mock.nodeId() } doReturn URI.create("dtn://test/")
    }
    private val http = ConvergenceSenderHTTP(agent, uri)

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
            val uri = URI.create("https://nodle.io")
            val agent = mock<IAgent>() {
                on { mock.nodeId() } doReturn URI.create("dtn://test/")
            }

            val http = mock<ConvergenceSenderHTTP>() {
                on { mock.agent } doReturn agent
                on { mock.url } doReturn uri
            }

            val localNodeId = URI.create("dtn://test/")
            val remoteNodeId = URI.create("dtn://nodle/dtn-router")
            val keyPair = Ed25519Util.generateEd25519KeyPair()

            val outBundle1 = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .makeBundle()
                .addBlock(payloadBlock(ByteArray(10000)))
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

            http.sendBundle(outBundle1)

            verify(http).sendBundle(outBundle1)
        }
    }

    @Test
    fun test2_sendBundles() {
        runBlockingTest {
            val localNodeId = URI.create("dtn://test/")
            val remoteNodeId = URI.create("dtn://nodle/dtn-router")
            val keyPair = Ed25519Util.generateEd25519KeyPair()

            val uri = URI.create("https://nodle.io")
            val agent = mock<IAgent>() {
                on { mock.nodeId() } doReturn URI.create("dtn://test/")
            }

            val http = mock<ConvergenceSenderHTTP>() {
                on { mock.agent } doReturn agent
                on { mock.url } doReturn uri
            }

            val outBundle1 = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .makeBundle()
                .addBlock(payloadBlock(ByteArray(10000)))
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

            val bundles = listOf<Bundle>(outBundle1)

            http.sendBundles(bundles)

            verify(http).sendBundles(bundles)
        }

    }

    @Test
    fun test3_getAgent() {
        /* Check */
        assertEquals(http.agent, agent)
    }

    @Test
    fun test4_getUrl() {
        /* Check */
        assertEquals(http.url, uri)
    }
}