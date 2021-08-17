package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.bpv7.eid.nullDtnEid
import io.nodle.dtn.bpv7.extensions.ageBlock
import io.nodle.dtn.crypto.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.junit.*
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import java.net.URI

/**
 * @author Niki Izvorski on 17/08/21.
 */

@ExperimentalCoroutinesApi
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class BundleValidatorTest {
    private val localNodeId = URI.create("dtn://test/")
    private val remoteNodeId = URI.create("dtn://nodle/dtn-router")
    private val keyPair = Ed25519Util.generateEd25519KeyPair()

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test(expected = ValidationException::class)
    fun stage01_testCheckValidPrimaryBlock() {
        runBlockingTest {
            /* Given */
            val mockedBundle = PrimaryBlock()
                .destination(remoteNodeId)
                .source(nullDtnEid())
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .procV7Flags(BundleV7Flags.StatusRequestForward)
                .makeBundle()
                .addBlock(payloadBlock(ByteArray(10000)))
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

            /* Then */
            mockedBundle.checkValid()
        }
    }

    @Test(expected = ValidationException::class)
    fun stage02_testCheckValidCRC() {
        runBlockingTest {
            /* Given */
            val mockedBundle = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.NoCRC)
                .procV7Flags(BundleV7Flags.StatusRequestForward)
                .makeBundle()
                .addBlock(payloadBlock(ByteArray(10000)))

            /* Then */
            mockedBundle.checkValid()
        }
    }

    @Test(expected = ValidationException::class)
    fun stage03_testCheckLifetimeExceeded() {
        runBlockingTest {
            /* Given */
            val mockedBundle = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .creationTimestamp(0L)
                .procV7Flags(BundleV7Flags.StatusRequestForward)
                .makeBundle()
                .addBlock(payloadBlock(ByteArray(10000)))
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

            /* Then */
            mockedBundle.checkValid()
        }
    }

    @Test(expected = ValidationException::class)
    fun stage04_testCheckFragmentState() {
        runBlockingTest {
            /* Given */
            val mockedBundle = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .procV7Flags(BundleV7Flags.IsFragment)
                .procV7Flags(BundleV7Flags.MustNotFragment)
                .makeBundle()
                .addBlock(payloadBlock(ByteArray(10000)))
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

            /* Then */
            mockedBundle.checkValid()
        }
    }

    @Test
    fun stage05_testCheckBundleValid() {
        runBlockingTest {
            /* Given */
            val mockedBundle = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .procV7Flags(BundleV7Flags.StatusRequestForward)
                .makeBundle()
                .addBlock(payloadBlock(ByteArray(10000)))
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

            /* Then */
            assertTrue(mockedBundle.isValid())
        }
    }

    @Test(expected = ValidationException::class)
    fun stage06_testCheckLastBlock() {
        runBlockingTest {
            /* Given */
            val mockedBundle = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .procV7Flags(BundleV7Flags.StatusRequestForward)
                .makeBundle()
                .addBlock(ageBlock(0))
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

            /* Then */
            mockedBundle.checkValid()
        }
    }

    @Test(expected = ValidationException::class)
    fun stage07_testSameBlock() {
        runBlockingTest {
            /* Given */
            val canonicalBlock = CanonicalBlock()

            val mockedBundle = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .procV7Flags(BundleV7Flags.StatusRequestForward)
                .makeBundle()
                .addBlock(payloadBlock(ByteArray(10000)))
                .addBlock(canonicalBlock)
                .addBlock(canonicalBlock)
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

            /* Then */
            mockedBundle.checkValid()
        }
    }

    @Test(expected = ValidationException::class)
    fun stage08_testWithoutPayloadBlock() {
        runBlockingTest {
            /* Given */
            val mockedBundle = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .procV7Flags(BundleV7Flags.StatusRequestForward)
                .makeBundle()

            /* Then */
            mockedBundle.checkValid()
        }
    }

    @Test(expected = ValidationException::class)
    fun stage09_testPrimaryBlockLifetimeExpired() {
        runBlockingTest {
            /* Given */
            val mockedBundle = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .lifetime(1)
                .creationTimestamp(0L)
                .procV7Flags(BundleV7Flags.StatusRequestForward)
                .makeBundle()
                .addBlock(payloadBlock(ByteArray(10000)))
                .addBlock(ageBlock(10))

            /* Then */
            mockedBundle.checkValid()
        }
    }

    @Test(expected = ValidationException::class)
    fun stage10_testPrimaryBlockLifetimeExpired() {
        runBlockingTest {
            /* Given */
            val mockedBundle = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .lifetime(1)
                .creationTimestamp(1)
                .procV7Flags(BundleV7Flags.StatusRequestForward)
                .makeBundle()
                .addBlock(payloadBlock(ByteArray(10000)))
                .addBlock(ageBlock(10))

            /* Then */
            mockedBundle.checkValid()
        }
    }

    @Test(expected = ValidationException::class)
    fun stage11_testCheckValidCanonicalBlock() {
        runBlockingTest {
            /* Given */
            val canonicalBlock = CanonicalBlock()

            val mockedBundle = PrimaryBlock()
                .destination(remoteNodeId)
                .source(localNodeId)
                .reportTo(remoteNodeId)
                .crcType(CRCType.CRC32)
                .procV7Flags(BundleV7Flags.StatusRequestForward)
                .makeBundle()
                .addBlock(payloadBlock(ByteArray(10000)))
                .addBlock(canonicalBlock, false)
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

            /* When */
            canonicalBlock.blockNumber = 0

            /* Then */
            mockedBundle.checkValid()
        }
    }
}