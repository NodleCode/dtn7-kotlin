package io.nodle.dtn.aa

import MockApplicationAgent
import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.bpv7.eid.apiMe
import io.nodle.dtn.crypto.Ed25519Util
import io.nodle.dtn.interfaces.IAgent
import io.nodle.dtn.interfaces.IApplicationAgent
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
class StaticRegistrarTest {
    private val uri = URI.create("https://dtn.nodle.io/outbox")
    private val localNodeId = URI.create("dtn://test/")
    private val remoteNodeId = URI.create("dtn://nodle/dtn-router")
    private val check = apiMe()
    private val keyPair = Ed25519Util.generateEd25519KeyPair()
    private val mockAppAgent = MockApplicationAgent()

    private val outBundle1 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    private val outBundle2 = PrimaryBlock()
        .destination(localNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    private val appAgent = mock<IApplicationAgent>()

    private val agent = mock<IAgent>() {
        on { mock.nodeId() } doReturn URI.create("dtn://test/")
    }

    private val registrar = StaticRegistrar(agent)

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test0_register() {
        /* Check */
        assertTrue(registrar.register(uri, appAgent))
    }

    @Test
    fun test1_registerFail() {
        /* When */
        registrar.staticRegistration.put(uri, appAgent)

        /* Then */
        assertFalse(registrar.register(uri, appAgent))
    }

    @Test
    fun test1_listEndpoint() {
        /* When */
        registrar.staticRegistration.put(uri, appAgent)

        /* Then */
        assertEquals(registrar.listEndpoint().size, 1)
    }

    @Test
    fun test2_localDelivery() {
        /* When */
        registrar.staticRegistration[remoteNodeId] = appAgent

        /* Then */
        assertEquals(registrar.localDelivery(outBundle1), appAgent)
    }

    @Test
    fun test3_localDeliveryWithCheck() {
        /* When */
        registrar.staticRegistration[check] = appAgent

        /* Then */
        assertEquals(registrar.localDelivery(outBundle2), appAgent)
    }

    @Test
    fun test4_getStaticRegistration() {
        /* Given */
        registrar.staticRegistration = mutableMapOf()

        /* Then */
        assertNotNull(registrar.staticRegistration)
    }


    @Test
    fun test5_appBundleDelivery() {
        runBlockingTest {
            /* Given */
            registrar.staticRegistration[remoteNodeId] = mockAppAgent

            /* When */
            registrar.register(uri, mockAppAgent)

            /* Then */
            assertTrue(mockAppAgent.deliver(outBundle1))
        }
    }

    @Test
    fun test6_failLocalDelivery() {
        runBlockingTest {
            /* Check */
            assertNull(registrar.localDelivery(outBundle2))
        }
    }
}