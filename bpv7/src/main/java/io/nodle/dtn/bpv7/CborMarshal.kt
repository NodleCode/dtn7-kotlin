package io.nodle.dtn.bpv7

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import io.nodle.dtn.bpv7.bpsec.AbstractSecurityBlockData
import io.nodle.dtn.bpv7.bpsec.cborMarshalData
import io.nodle.dtn.bpv7.eid.*
import io.nodle.dtn.bpv7.extensions.BundleAgeBlockData
import io.nodle.dtn.bpv7.extensions.cborMarshalData
import io.nodle.dtn.crypto.CRC
import io.nodle.dtn.crypto.CRC16X25
import io.nodle.dtn.crypto.CRC32C
import io.nodle.dtn.crypto.NullCRC
import io.nodle.dtn.utils.DualOutputStream
import io.nodle.dtn.utils.isFlagSet
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.URI

/**
 * @author Lucien Loiseau on 12/02/21.
 */

class CborEncodingException(msg: String) : Exception(msg)

private val log = LoggerFactory.getLogger("bpv7-marshal")

@Throws(CborEncodingException::class)
fun Bundle.cborMarshal(out: OutputStream) {
    val gen = CBORFactory().createGenerator(out)
    gen.writeStartArray()
    gen.flush()
    primaryBlock.cborMarshal(out)
    for (block in canonicalBlocks) {
        block.cborMarshal(out)
    }
    gen.writeEndArray()
    gen.flush()
}

@Throws(CborEncodingException::class)
fun PrimaryBlock.cborMarshal(out: OutputStream) {
    val crc = when (crcType) {
        CRCType.NoCRC -> NullCRC()
        CRCType.CRC16 -> CRC16X25()
        CRCType.CRC32 -> CRC32C()
    }

    // multiplexing the output stream to compute crc at same time
    val dualOut = DualOutputStream(crc, out)
    val gen = CBORFactory().createGenerator(dualOut)

    gen.writeStartArray(cborGetItemCount())
    gen.writeNumber(version)
    gen.writeNumber(procV7Flags)
    gen.writeNumber(crcType.ordinal)
    destination.cborMarshal(gen)
    source.cborMarshal(gen)
    reportTo.cborMarshal(gen)
    gen.writeStartArray(2)
    gen.writeNumber(creationTimestamp)
    gen.writeNumber(sequenceNumber)
    gen.writeEndArray()
    gen.writeNumber(lifetime)
    if (procV7Flags.isFlagSet(BundleV7Flags.FRAGMENT.offset)) {
        gen.writeNumber(fragmentOffset)
        gen.writeNumber(appDataLength)
    }
    gen.flush() // force into the crc
    if (crcType != CRCType.NoCRC) {
        gen.writeBinary(endCRC(crc).done())
    }
    gen.writeEndArray()
    gen.flush() // end cbor stream
}

fun PrimaryBlock.endCRC(crc: CRC): CRC {
    // The CRC SHALL be computed over the concatenation of
    // all bytes (including CBOR "break" characters) of the primary block
    // including the CRC field itself, which for this purpose SHALL be
    // temporarily populated with all bytes set to zero.
    val crcLast = CBORFactory().createGenerator(crc)
    when (crcType) {
        CRCType.CRC16 -> crcLast.writeBinary(byteArrayOf(0, 0))
        CRCType.CRC32 -> crcLast.writeBinary(byteArrayOf(0, 0, 0, 0))
        CRCType.NoCRC -> {
        }
    }
    crcLast.flush()
    return crc
}

fun PrimaryBlock.cborGetItemCount(): Int {
    var length = 8
    if (this.crcType != CRCType.NoCRC) {
        length++
    }
    if (this.procV7Flags.isFlagSet(BundleV7Flags.FRAGMENT.offset)) {
        length += 2
    }
    return length
}

@Throws(CborEncodingException::class)
fun CanonicalBlock.cborMarshal(out: OutputStream) {
    val crc = when (crcType) {
        CRCType.NoCRC -> NullCRC()
        CRCType.CRC16 -> CRC16X25()
        CRCType.CRC32 -> CRC32C()
    }

    // multiplexing the output stream to compute crc at same time
    val dualOut = DualOutputStream(crc, out)
    val gen = CBORFactory().createGenerator(dualOut)

    gen.writeStartArray(cborGetItemCount())
    gen.writeNumber(blockType)
    gen.writeNumber(blockNumber)
    gen.writeNumber(procV7flags)
    gen.writeNumber(crcType.ordinal)
    writeBlockData(gen)

    if (crcType != CRCType.NoCRC) {
        gen.flush() // force into the crc
        gen.writeBinary(endCRC(crc).done())
    }
    gen.writeEndArray()
    gen.flush()
}

fun CanonicalBlock.writeBlockData(gen: CBORGenerator) {
    val buf = ByteArrayOutputStream()
    when (blockType) {
        BlockType.BlockIntegrityBlock.code -> {
            (data as AbstractSecurityBlockData).cborMarshalData(buf)
            gen.writeBinary(buf.toByteArray())
        }
        BlockType.BlockConfidentialityBlock.code -> {
            (data as AbstractSecurityBlockData).cborMarshalData(buf)
            gen.writeBinary(buf.toByteArray())
        }
        BlockType.BundleAgeBlock.code -> {
            (data as BundleAgeBlockData).cborMarshalData(buf)
            gen.writeBinary(buf.toByteArray())
        }
        else -> gen.writeBinary((data as BlobBlockData).buffer)
    }
}


fun CanonicalBlock.endCRC(crc: CRC): CRC {
    // The CRC SHALL be computed over the concatenation of
    // all bytes (including CBOR "break" characters) of the primary block
    // including the CRC field itself, which for this purpose SHALL be
    // temporarily populated with all bytes set to zero.
    val crcLast = CBORFactory().createGenerator(crc)
    when (crcType) {
        CRCType.CRC16 -> crcLast.writeBinary(byteArrayOf(0, 0))
        CRCType.CRC32 -> crcLast.writeBinary(byteArrayOf(0, 0, 0, 0))
        CRCType.NoCRC -> {
        }
    }
    crcLast.flush()
    return crc
}

fun CanonicalBlock.cborGetItemCount(): Int {
    if (crcType == CRCType.NoCRC) {
        return 5
    }
    return 6
}

@Throws(CborEncodingException::class)
fun URI.cborMarshal(gen: CBORGenerator) {
    if (isIpnEid()) {
        gen.writeStartArray(2)
        gen.writeNumber(EID_IPN_IANA_VALUE)
        gen.writeArray(intArrayOf(getNodeNumberUnsafe(), getServiceNumberUnsafe()), 0, 2)
        gen.writeEndArray()
        return
    }
    if (isNullEid()) {
        gen.writeStartArray(2)
        gen.writeNumber(EID_DTN_IANA_VALUE)
        gen.writeNumber(0)
        gen.writeEndArray()
        return
    }
    if (isDtnEid()) {
        gen.writeStartArray(2)
        gen.writeNumber(EID_DTN_IANA_VALUE)
        gen.writeString(schemeSpecificPart)
        gen.writeEndArray()
        return
    }
    throw CborEncodingException("eid not supported: ${this.toASCIIString()}")
}