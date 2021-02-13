package io.nodle.dtn.bpv7

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.eid.*
import io.nodle.dtn.utils.isFlagSet
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException


/**
 * @author Lucien Loiseau on 12/02/21.
 */

class CborParsingException(msg: String) : Exception(msg)
class CborBundleEnd : Exception()

private val log = LoggerFactory.getLogger("bpv7-unmarshal")

@Throws(CborParsingException::class)
fun cborUnmarshalBundle(input : InputStream) : Bundle {
    return CBORFactory()
        .createParser(input)
        .readBundle()
}

@Throws(CborParsingException::class)
fun CBORParser.readBundle(): Bundle {
    readStartArray()
    val ret = readPrimaryBlock().makeBundle()

    // parse until we either reach end of bundle or parsing exception
    try {
        while (true) {
            ret.canonicalBlocks.add(readCanonicalBlock())
        }
    } catch (bundleEnd: CborBundleEnd) {
        return ret
    } catch (e: Exception) {
        throw e
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readPrimaryBlock(): PrimaryBlock {
    val ret = newPrimaryBlock()
    readStartArray()
    ret.version = readInt()
    ret.procV7Flags = readLong()
    ret.crcType = CRCType.fromInt(readInt())
    ret.destination = readEid()
    ret.source = readEid()
    ret.reportTo = readEid()
    readStartArray()
    ret.creationTimestamp = readLong()
    ret.sequenceNumber = readLong()
    readCloseArray()
    ret.lifetime = readLong()
    if (ret.procV7Flags.isFlagSet(BundleV7Flags.FRAGMENT.offset)) {
        ret.fragmentOffset = readLong()
        ret.appDataLength = readLong()
    }
    if (ret.crcType != CRCType.NoCRC) {
        val crc = readByteArray()
    }
    readCloseArray()
    return ret
}

@Throws(CborParsingException::class, CborBundleEnd::class)
fun CBORParser.readCanonicalBlock(): CanonicalBlock {
    val header = CanonicalBlock()
    when (val next = nextToken()) {
        JsonToken.START_ARRAY -> {
        }
        JsonToken.END_ARRAY -> throw CborBundleEnd()
        else -> throw CborParsingException("unexpected token: ${next.asString()}")
    }

    header.blockType = readInt()
    header.number = readInt()
    header.procV7flags = readLong()
    header.crcType = CRCType.fromInt(readInt())
    val ret = when (header.blockType) {
        BlockType.PayloadBlock.code -> readPayloadBlock(header)
        else -> throw CborParsingException("block type unsupported")
    }
    if (ret.crcType != CRCType.NoCRC) {
        val crc = readByteArray()
    }
    readCloseArray()
    return ret
}

@Throws(CborParsingException::class)
fun CBORParser.readPayloadBlock(header: CanonicalBlock): CanonicalBlock {
    return PayloadBlock(readByteArray()).cloneHeader(header)
}

@Throws(CborParsingException::class)
fun CBORParser.readEid(): URI {
    readStartArray()
    var eid = nullDtnEid()
    when (readInt()) {
        EID_IPN_IANA_VALUE -> return createIpn(readInt(), readInt())
        EID_DTN_IANA_VALUE -> {
            val t = nextToken()
            if (t.isNumeric) {
                val zero = intValue
                if (zero != 0) {
                    throw CborParsingException("eid format exception")
                }
            } else {
                try {
                    eid = URI("dtn:$text")
                } catch (e : URISyntaxException) {
                    throw CborParsingException("invalid dtn eid: ${e.reason}");
                }
            }
        }
        else -> throw CborParsingException("eid unsupported")
    }
    readCloseArray()
    return eid
}

@Throws(CborParsingException::class)
fun CBORParser.readStartArray() {
    if (nextToken() != JsonToken.START_ARRAY) {
        throw CborParsingException("expected start array")
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readCloseArray() {
    if (nextToken() != JsonToken.END_ARRAY) {
        throw CborParsingException("expected end array")
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readInt(): Int {
    val t = nextToken()
    if (!t.isNumeric) {
        throw CborParsingException("expected number")
    }
    return intValue
}

@Throws(CborParsingException::class)
fun CBORParser.readLong(): Long {
    val t = nextToken()
    if (!t.isNumeric) {
        throw CborParsingException("expected number")
    }
    return longValue
}


@Throws(CborParsingException::class)
fun CBORParser.readString(): String {
    if (nextToken() != JsonToken.VALUE_STRING) {
        throw CborParsingException("expected string")
    }
    return text
}

@Throws(CborParsingException::class)
fun CBORParser.readByteArray(): ByteArray {
    if (nextToken() != JsonToken.VALUE_EMBEDDED_OBJECT) {
        throw CborParsingException("expected byte array")
    }
    val buffer = ByteArrayOutputStream()
    readBinaryValue(buffer)
    return buffer.toByteArray()
}



