package io.nodle.dtn.bpv7

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.nodle.dtn.utils.hexToBa
import io.nodle.dtn.utils.toHex
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
    fun testBundleEncoding() {
        val testPayload = byteArrayOf(0xca.toByte(), 0xfe.toByte(), 0, 0xfe.toByte(), 0xca.toByte())

        val bundle = newPrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .makeBundle()
                .addBlock(PayloadBlock(testPayload)
                        .crcType(CRCType.CRC32))

        try {
            // encode
            val buffer = ByteArrayOutputStream()
            bundle.cborMarshal(buffer)

            // decode
            val parsedBundle = cborUnmarshalBundle(ByteArrayInputStream(buffer.toByteArray()))

            println(parsedBundle.toString())
            Assert.assertEquals(bundle, parsedBundle)
        } catch (e : CborEncodingException) {
            Assert.fail()
        }
    }

    @Test
    fun testBadBundleEncoding() {
        val testPayload = "cafe00feca".hexToBa()

        val bundle = newPrimaryBlock()
                .destination(URI.create("dtp://nodle/dtn-router")) // wrong dtn scheme
                .makeBundle()
                .addBlock(PayloadBlock(testPayload)
                        .crcType(CRCType.CRC32))
        val buffer = ByteArrayOutputStream()

        try {
            bundle.cborMarshal(buffer)
            Assert.fail()
        } catch (e : CborEncodingException) {
        }
    }

    @Test
    fun testBadCborString() {
        val randomBuffer = "0011afdc5d26f3a6c5ffa60019".hexToBa()

        try {
            CBORFactory().createParser(ByteArrayInputStream(randomBuffer)).readBundle()
            Assert.fail()
        } catch (e : CborParsingException) {
        }
    }
}
