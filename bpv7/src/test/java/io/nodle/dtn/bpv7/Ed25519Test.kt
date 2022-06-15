package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.bpsec.AbstractSecurityBlockData
import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.bpv7.bpsec.getEd25519SignatureKey
import io.nodle.dtn.crypto.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.junit.*
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

/**
 * @author Lucien Loiseau on 14/02/21.
 */

@ExperimentalCoroutinesApi
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class Ed25519Test {
    private val localNodeId = URI.create("dtn://test/")
    private val remoteNodeId = URI.create("dtn://nodle/dtn-router")
    private val keyPair = Ed25519Util.generateEd25519KeyPair()

    private val mockedBundle = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .procV7Flags(BundleV7Flags.StatusRequestForward)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    private val unsignedBundle = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .procV7Flags(BundleV7Flags.StatusRequestForward)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test1_Ed25519Signature() {
        /* Given */
        val key = Ed25519Util.generateEd25519KeyPair()
        val plaintext = "this is a simple message to sign".toByteArray()
        val signature = key.ed25519PrivateKey().signMsg(plaintext)

        /* Then */
        Assert.assertEquals(true, key.ed25519PublicKey().checkSignature(plaintext, signature))
    }

    @Test
    fun stage0_testBundleSignature() {
        runBlockingTest {
            val data = AbstractSecurityBlockData()

            val canon = mock<CanonicalBlock>() {
                on { mock.data } doReturn data
                onGeneric { mock.blockType } doReturn 40
            }

            // add bundle
            mockedBundle.canonicalBlocks.add(canon)

            /* When */
            mockedBundle.getEd25519SignatureKey(0)

            /* Then */
            verify(canon).blockType
        }
    }

    @Test
    fun stage1_testBundleSignature() {
        runBlockingTest {
            /* When */
            unsignedBundle.addEd25519Signature(keyPair, listOf(0, 1))

            /* Then */
            Assert.assertNotNull(unsignedBundle.getEd25519SignatureKey(0))
        }
    }

    @Test
    fun stage2_testBundleSignature() {
        runBlockingTest {
            /* When */
            unsignedBundle.addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, System.currentTimeMillis(), listOf(0, 1))

            /* Then */
            Assert.assertNotNull(unsignedBundle.getEd25519SignatureKey(0))
        }
    }

    @Test
    fun stage3_testBundleSignature() {
        runBlockingTest {
            /* When */
            unsignedBundle.addEd25519Signature(keyPair, System.currentTimeMillis(), listOf(0, 1))

            /* Then */
            Assert.assertNotNull(unsignedBundle.getEd25519SignatureKey(0))
        }
    }
}