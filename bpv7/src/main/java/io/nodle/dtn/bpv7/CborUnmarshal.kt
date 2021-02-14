package io.nodle.dtn.bpv7

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.bpsec.readASBlockData
import io.nodle.dtn.bpv7.eid.EID_DTN_IANA_VALUE
import io.nodle.dtn.bpv7.eid.EID_IPN_IANA_VALUE
import io.nodle.dtn.bpv7.eid.createIpn
import io.nodle.dtn.bpv7.eid.nullDtnEid
import io.nodle.dtn.bpv7.extensions.readBundleAgeBlockData
import io.nodle.dtn.utils.isFlagSet
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException


/**
 * @author Lucien Loiseau on 12/02/21.
 */
class CborParsingException(msg: String) : Exception(msg)

@Throws(CborParsingException::class)
fun cborUnmarshalBundle(input: InputStream): Bundle {
    return CBORFactory()
        .createParser(input)
        .readBundle()
}

@Throws(CborParsingException::class)
fun CBORParser.readBundle(): Bundle {
    readStartArray()
    return readPrimaryBlock()
        .makeBundle()
        .also {
            while (true) {
                if (nextToken() == JsonToken.END_ARRAY) {
                    break
                }
                it.addBlock(readCanonicalBlock(true), false)
            }
        }
}

@Throws(CborParsingException::class)
fun CBORParser.readPrimaryBlock(): PrimaryBlock {
    return readStruct(false) {
        PrimaryBlock(
            version = readInt(),
            procV7Flags = readLong(),
            crcType = CRCType.fromInt(readInt()),
            destination = readEid(),
            source = readEid(),
            reportTo = readEid(),
            creationTimestamp = let { readStartArray(); readLong() },
            sequenceNumber = readLong(),
            lifetime = let { readCloseArray(); readLong() },
        ).also { primary ->
            // fragment specific information
            if (primary.procV7Flags.isFlagSet(BundleV7Flags.FRAGMENT.offset)) {
                primary.fragmentOffset = readLong()
                primary.appDataLength = readLong()
            }

            // check CRC
            if (primary.crcType != CRCType.NoCRC) {
                val crc = readByteArray()
                if (!primary.checkCRC(crc)) {
                    throw CborParsingException("wrong crc")
                }
            }
        }
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readCanonicalBlock(prefetch: Boolean): CanonicalBlock {
    return readStruct(prefetch) {
        CanonicalBlock(
            blockType = readInt(),
            blockNumber = readInt(),
            procV7flags = readLong(),
            crcType = CRCType.fromInt(readInt())
        ).also { block ->
            // parse block specific data
            val blockDataBuf = readByteArray()
            when (block.blockType) {
                BlockType.BlockIntegrityBlock.code -> {
                    val asbParser = CBORFactory().createParser(blockDataBuf)
                    block.data = asbParser.readASBlockData()
                }
                BlockType.BlockConfidentialityBlock.code -> {
                    val asbParser = CBORFactory().createParser(blockDataBuf)
                    block.data = asbParser.readASBlockData()
                }
                BlockType.BundleAgeBlock.code -> {
                    val ageParser = CBORFactory().createParser(blockDataBuf)
                    block.data = ageParser.readBundleAgeBlockData()
                }
                else -> block.data = BlobBlockData(blockDataBuf)
            }

            // check CRC
            if (block.crcType != CRCType.NoCRC) {
                val crc = readByteArray()
                if (!block.checkCRC(crc)) {
                    throw CborParsingException("wrong crc")
                }
            }
        }
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readCanonicalBlock() = readCanonicalBlock(false)

@Throws(CborParsingException::class)
fun CBORParser.readEid(): URI {
    return readStruct(false) {
        try {
            when (readInt()) {
                EID_IPN_IANA_VALUE -> createIpn(readInt(), readInt())
                EID_DTN_IANA_VALUE -> {
                    val t = nextToken()
                    if (t.isNumeric) {
                        check(0 == intValue)
                        nullDtnEid()
                    } else {
                        URI("dtn:$text")
                    }
                }
                else -> throw CborParsingException("eid unsupported")
            }
        } catch (e: URISyntaxException) {
            throw CborParsingException("invalid dtn eid: ${e.message}");
        } catch (e: IllegalStateException) {
            throw CborParsingException("invalid eid: ${e.message}");
        }
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readStartArray() {
    if (nextToken() != JsonToken.START_ARRAY) {
        throw CborParsingException("expected start array but got $currentName")
    }
}

@Throws(CborParsingException::class)
fun CBORParser.assertStartArray() {
    if (!isExpectedStartArrayToken) {
        throw CborParsingException("expected start array but got $currentName")
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readCloseArray() {
    if (nextToken() != JsonToken.END_ARRAY) {
        throw CborParsingException("expected end array but got $currentName")
    }
}

@Throws(CborParsingException::class)
fun <T : Any> CBORParser.readArray(
    prefetch: Boolean,
    elementParser: (CBORParser) -> T
): MutableList<T> {
    if (!prefetch) {
        readStartArray()
    } else {
        assertStartArray()
    }

    val ret = ArrayList<T>()
    while (true) {
        if (nextToken() == JsonToken.END_ARRAY) {
            break
        }
        ret.add(elementParser(this))
    }
    return ret
}

@Throws(CborParsingException::class)
fun <T : Any> CBORParser.readStruct(prefetch: Boolean, elementParser: (CBORParser) -> T): T {
    if (!prefetch) {
        readStartArray()
    } else {
        assertStartArray()
    }

    try {
        return elementParser(this)
    } finally {
        readCloseArray()
    }
}


@Throws(CborParsingException::class)
fun CBORParser.readInt(): Int {
    val t = nextToken()
    if (!t.isNumeric) {
        throw CborParsingException("expected number but got $currentName")
    }
    return intValue
}

@Throws(CborParsingException::class)
fun CBORParser.readLong(): Long {
    val t = nextToken()
    if (!t.isNumeric) {
        throw CborParsingException("expected number but got $currentName")
    }
    return longValue
}


@Throws(CborParsingException::class)
fun CBORParser.readString(): String {
    if (nextToken() != JsonToken.VALUE_STRING) {
        throw CborParsingException("expected string but got $currentName")
    }
    return text
}

@Throws(CborParsingException::class)
fun CBORParser.readByteArray(): ByteArray {
    if (nextToken() != JsonToken.VALUE_EMBEDDED_OBJECT) {
        throw CborParsingException("expected byte array but got $currentName")
    }
    val buffer = ByteArrayOutputStream()
    readBinaryValue(buffer)
    return buffer.toByteArray()
}



