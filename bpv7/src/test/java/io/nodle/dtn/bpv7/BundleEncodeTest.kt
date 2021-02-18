package io.nodle.dtn.bpv7

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.nodle.dtn.bpv7.administrative.*
import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.bpv7.extensions.ageBlock
import io.nodle.dtn.crypto.Ed25519Util
import io.nodle.dtn.utils.hexToBa
import io.nodle.dtn.utils.setFlag
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI

/**
 * @author Lucien Loiseau on 12/02/21.
 */
class BundleEncodeTest {

    @Test
    fun testSimpleBundleBufferEncoding() {
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())
        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .makeBundle()
                .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))
        try {
            Assert.assertEquals(bundle, cborUnmarshalBundle(bundle.cborMarshal()))
        } catch (e: CborEncodingException) {
            Assert.fail()
        }
    }

    @Test
    fun testSimpleBundle() {
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())
        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .makeBundle()
                .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))
        testEncodeDecode(bundle)
    }

    @Test
    fun testAgeBlockBundle() {
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())
        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .creationTimestamp(0)
                .makeBundle()
                .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))
                .addBlock(ageBlock(5000))
        testEncodeDecode(bundle)
    }

    @Test
    fun testBundleSignature() {
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())
        val keyPair = Ed25519Util.generateEd25519KeyPair()
        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .makeBundle()
                .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))
        testEncodeDecode(bundle)
    }

    @Test
    fun testBundleSignature2() {
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())
        val keyPair = Ed25519Util.generateEd25519KeyPair()
        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .crcType(CRCType.NoCRC)
                .makeBundle()
                .addBlock(payloadBlock(testPayload).crcType(CRCType.CRC32))
                .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))
        testEncodeDecode(bundle)
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

    @Test
    fun testBadBundle() {
        val testPayload = "cafe00feca".hexToBa()

        val bundle = PrimaryBlock()
                .destination(URI.create("dtp://nodle/dtn-router")) // wrong dtn scheme
                .makeBundle()
                .addBlock(payloadBlock(testPayload)
                        .crcType(CRCType.CRC32))
        val buffer = ByteArrayOutputStream()

        try {
            bundle.cborMarshal(buffer)
            Assert.fail()
        } catch (e: CborEncodingException) {
        }
    }

    @Test
    fun testBadCborString() {
        val randomBuffer = "0011afdc5d26f3a6c5ffa60019".hexToBa()

        try {
            CBORFactory().createParser(ByteArrayInputStream(randomBuffer)).readBundle()
            Assert.fail()
        } catch (e: CborParsingException) {
        }
    }


    @Test
    fun testAdministrationRecord() {
        val admRecord = AdministrativeRecord(
                recordTypeCode = RecordTypeCode.StatusRecordType.code,
                data = StatusReport()
                        .assert(StatusAssertion.ReceivedBundle, true, 1613607271)
                        .assert(StatusAssertion.ForwardedBundle, true, 1613897271)
                        .reason(StatusReportReason.NoInformation)
                        .source(URI.create("dtn://test-sdk/")))

        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .procV7Flags(BundleV7Flags.AdministrativeRecordPayload)
                .makeBundle()
                .addBlock(payloadBlock(admRecord.cborMarshalData()).crcType(CRCType.CRC32))
        try {
            Assert.assertEquals(bundle, cborUnmarshalBundle(bundle.cborMarshal()))
        } catch (e: CborEncodingException) {
            Assert.fail()
        }
    }

}
