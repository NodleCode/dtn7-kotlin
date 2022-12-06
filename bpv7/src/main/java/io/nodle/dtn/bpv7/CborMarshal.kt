package io.nodle.dtn.bpv7

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import io.nodle.dtn.bpv7.eid.*
import io.nodle.dtn.crypto.CRC
import io.nodle.dtn.crypto.CRC16X25
import io.nodle.dtn.crypto.CRC32C
import io.nodle.dtn.crypto.NullCRC
import io.nodle.dtn.utils.CloseProtectOutputStream
import io.nodle.dtn.utils.DualOutputStream
import io.nodle.dtn.utils.isFlagSet
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.URI

/**
 * @author Lucien Loiseau on 12/02/21.
 */

class CborEncodingException(msg: String) : Exception(msg)

@Throws(CborEncodingException::class)
fun Bundle.cborMarshal() : ByteArray {
    return ByteArrayOutputStream().use {
        this.cborMarshal(it)
        it.toByteArray()
    }
}

@Throws(CborEncodingException::class)
fun Bundle.cborMarshal(out: OutputStream) {
    CBORFactory().createGenerator(CloseProtectOutputStream(out)).use {
        it.writeStartArray()
        it.flush()
        primaryBlock.cborMarshal(out)
        for (block in canonicalBlocks) {
            block.cborMarshal(out)
        }
        it.writeEndArray()
    }
}

@Throws(CborEncodingException::class)
fun PrimaryBlock.cborMarshal(): ByteArray {
    return ByteArrayOutputStream().use {
        this.cborMarshal(it)
        it.toByteArray()
    }
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
    CBORFactory().createGenerator(dualOut).use {
        it.writeStartArray(cborGetItemCount())
        it.writeNumber(version)
        it.writeNumber(procV7Flags)
        it.writeNumber(crcType.ordinal)
        it.cborMarshal(destination)
        it.cborMarshal(source)
        it.cborMarshal(reportTo)
        it.writeStartArray(2)
        it.writeNumber(creationTimestamp)
        it.writeNumber(sequenceNumber)
        it.writeEndArray()
        it.writeNumber(lifetime)
        if (procV7Flags.isFlagSet(BundleV7Flags.IsFragment.offset)) {
            it.writeNumber(fragmentOffset)
            it.writeNumber(appDataLength)
        }
        if (crcType != CRCType.NoCRC) {
            it.flush() // force into the crc
            it.writeBinary(endCRC(crc).done())
        }
        it.writeEndArray()
    }
}

fun PrimaryBlock.endCRC(crc: CRC): CRC {
    // The CRC SHALL be computed over the concatenation of
    // all bytes (including CBOR "break" characters) of the primary block
    // including the CRC field itself, which for this purpose SHALL be
    // temporarily populated with all bytes set to zero.
    CBORFactory().createGenerator(crc).use {
        when (crcType) {
            CRCType.CRC16 -> it.writeBinary(byteArrayOf(0, 0))
            CRCType.CRC32 -> it.writeBinary(byteArrayOf(0, 0, 0, 0))
            CRCType.NoCRC -> {
            }
        }
    }
    return crc
}

fun PrimaryBlock.cborGetItemCount(): Int {
    var length = 8
    if (this.crcType != CRCType.NoCRC) {
        length++
    }
    if (this.procV7Flags.isFlagSet(BundleV7Flags.IsFragment.offset)) {
        length += 2
    }
    return length
}

@Throws(CborEncodingException::class)
fun CanonicalBlock.cborMarshal(): ByteArray {
    return ByteArrayOutputStream().use {
        this.cborMarshal(it)
        it.toByteArray()
    }
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
    CBORFactory().createGenerator(dualOut).use {
        it.writeStartArray(cborGetItemCount())
        it.writeNumber(blockType)
        it.writeNumber(blockNumber)
        it.writeNumber(procV7flags)
        it.writeNumber(crcType.ordinal)
        it.writeBlockData(blockType, data)

        if (crcType != CRCType.NoCRC) {
            it.flush() // force into the crc
            it.writeBinary(crc.endCRC(crcType).done())
        }
        it.writeEndArray()
    }
}


@Throws(CborEncodingException::class)
fun CBORGenerator.writeBlockData(blockType: Int, data: ExtensionBlockData) {
    getBpv7BlockExtensionEncoder(blockType)?.let { marshal ->
        val buf = ByteArrayOutputStream()
        marshal(data, buf)
        writeBinary(buf.toByteArray())
    } ?: run {
        if(blockType != BlockType.PayloadBlock.code) {
            throw CborEncodingException("block type is unknown")
        }
        writeBinary((data as PayloadBlockData).buffer)
    }
}


fun CRC.endCRC(crcType: CRCType): CRC {
    // The CRC SHALL be computed over the concatenation of
    // all bytes (including CBOR "break" characters) of the primary block
    // including the CRC field itself, which for this purpose SHALL be
    // temporarily populated with all bytes set to zero.
    CBORFactory().createGenerator(this).use {
        when (crcType) {
            CRCType.CRC16 -> it.writeBinary(byteArrayOf(0, 0))
            CRCType.CRC32 -> it.writeBinary(byteArrayOf(0, 0, 0, 0))
            CRCType.NoCRC -> {
            }
        }
    }
    return this
}

fun CanonicalBlock.cborGetItemCount(): Int {
    if (crcType == CRCType.NoCRC) {
        return 5
    }
    return 6
}

@Throws(CborEncodingException::class)
fun CBORGenerator.cborMarshal(uri: URI) {
    if (uri.isIpnEid()) {
        writeStartArray(2)
        writeNumber(EID_IPN_IANA_VALUE)
        writeArray(intArrayOf(uri.getNodeNumberUnsafe(), uri.getServiceNumberUnsafe()), 0, 2)
        writeEndArray()
        return
    }
    if (uri.isNullEid()) {
        writeStartArray(2)
        writeNumber(EID_DTN_IANA_VALUE)
        writeNumber(0)
        writeEndArray()
        return
    }
    if (uri.isDtnEid()) {
        writeStartArray(2)
        writeNumber(EID_DTN_IANA_VALUE)
        writeString(uri.schemeSpecificPart)
        writeEndArray()
        return
    }
    throw CborEncodingException("eid not supported: ${uri.toASCIIString()}")
}