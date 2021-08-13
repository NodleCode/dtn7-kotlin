package io.nodle.dtn.bpv7

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.nodle.dtn.bpv7.administrative.*
import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.bpv7.extensions.ageBlock
import io.nodle.dtn.bpv7.extensions.hopCountBlockData
import io.nodle.dtn.bpv7.extensions.previousNodeBlock
import io.nodle.dtn.crypto.Ed25519Util
import io.nodle.dtn.utils.hexToBa
import io.nodle.dtn.utils.setFlag
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI

/**
 * @author Lucien Loiseau on 12/02/21.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class BundleEncodeTest {

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test01_SimpleBundleBufferEncoding() {
        /* Given */
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())

        /* When */
        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .makeBundle()
                .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))

        /* Then */
        try {
            Assert.assertEquals(bundle, cborUnmarshalBundle(bundle.cborMarshal()))
        } catch (e: CborEncodingException) {
            Assert.fail()
        }
    }

    @Test
    fun test02_SimpleBundle() {
        /* Given */
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())

        /* When */
        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .makeBundle()
                .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))

        /* Then */
        testEncodeDecode(bundle)
    }

    @Test
    fun test03_AgeBlockBundle() {
        /* Given */
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())

        /* When */
        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .creationTimestamp(0)
                .makeBundle()
                .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))
                .addBlock(ageBlock(5000))

        /* Then */
        testEncodeDecode(bundle)
    }

    @Test
    fun test04_HopBundle() {
        /* Given */
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())

        /* When */
        val bundle = PrimaryBlock()
            .destination(URI.create("dtn://nodle/dtn-router"))
            .source(URI.create("dtn://test-sdk/"))
            .makeBundle()
            .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))
            .addBlock(hopCountBlockData(5, 6))

        /* Then */
        testEncodeDecode(bundle)
    }

    @Test
    fun test05_PreviousBundle() {
        /* Given */
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())

        /* When */
        val bundle = PrimaryBlock()
            .destination(URI.create("dtn://nodle/dtn-router"))
            .source(URI.create("dtn://test-sdk/"))
            .makeBundle()
            .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))
            .addBlock(previousNodeBlock(URI.create("dtn://test-sdk/")))

        /* Then */
        testEncodeDecode(bundle)
    }

    @Test
    fun test06_BundleSignature() {
        /* Given */
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())
        val keyPair = Ed25519Util.generateEd25519KeyPair()

        /* When */
        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .makeBundle()
                .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

        /* Then */
        testEncodeDecode(bundle)
    }

    @Test
    fun test07_BundleSignature2() {
        /* Given */
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())
        val keyPair = Ed25519Util.generateEd25519KeyPair()

        /* When */
        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .crcType(CRCType.NoCRC)
                .makeBundle()
                .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

        /* Then */
        testEncodeDecode(bundle)
    }

    @Test
    fun test08_BadBundle() {
        /* Given */
        val testPayload = "cafe00feca".hexToBa()

        /* When */
        val bundle = PrimaryBlock()
                .destination(URI.create("dtp://nodle/dtn-router")) // wrong dtn scheme
                .makeBundle()
                .addBlock(payloadBlock(testPayload)
                .crcType(CRCType.CRC32))

        val buffer = ByteArrayOutputStream()

        /* Then */
        try {
            bundle.cborMarshal(buffer)
            Assert.fail()
        } catch (e: CborEncodingException) {
        }
    }

    @Test
    fun test09_BadCborString() {
        /* Given */
        val randomBuffer = "0011afdc5d26f3a6c5ffa60019".hexToBa()

        /* Then */
        try {
            CBORFactory().createParser(ByteArrayInputStream(randomBuffer)).readBundle()
            Assert.fail()
        } catch (e: CborParsingException) {
        }
    }

    @Throws(Exception::class)
    fun testEncodeDecode(bundle: Bundle) {
        try {
            println(bundle.toString())
            bundle.checkValid()

            // encode
            val buffer = ByteArrayOutputStream()
            bundle.cborMarshal(buffer)

            // decode
            val parsedBundle = cborUnmarshalBundle(ByteArrayInputStream(buffer.toByteArray()))

            println(parsedBundle.toString())
            Assert.assertEquals(bundle, parsedBundle)

            parsedBundle.checkValid()
        } catch (e: CborEncodingException) {
            Assert.fail()
        }
    }
}
