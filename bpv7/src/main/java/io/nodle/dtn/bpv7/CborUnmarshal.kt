package io.nodle.dtn.bpv7

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.eid.*
import io.nodle.dtn.bpv7.bpsec.*
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
class CborBundleEnd : Exception()

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
    val ret = PrimaryBlock()
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
        if (!ret.checkCRC(crc)) {
            throw CborParsingException("wrong crc")
        }
    }
    readCloseArray()
    return ret
}

@Throws(CborParsingException::class, CborBundleEnd::class)
fun CBORParser.readCanonicalBlock(): CanonicalBlock {
    val block = CanonicalBlock()
    when (val next = nextToken()) {
        JsonToken.START_ARRAY -> {
            // ok
        }
        JsonToken.END_ARRAY -> throw CborBundleEnd()
        else -> throw CborParsingException("unexpected token: ${next.asString()}")
    }

    block.blockType = readInt()
    block.blockNumber = readInt()
    block.procV7flags = readLong()
    block.crcType = CRCType.fromInt(readInt())

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

    if (block.crcType != CRCType.NoCRC) {
        val crc = readByteArray()
        if (!block.checkCRC(crc)) {
            throw CborParsingException("wrong crc")
        }
    }
    readCloseArray()
    return block
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
fun CBORParser.readArray(prefetch:Boolean, elementParser : (CBORParser) -> Any) {
    if(!prefetch) {
        readStartArray()
    }

    while(true) {
        if(nextToken() == JsonToken.END_ARRAY) {
            break
        }
        // careful! we cannot rewind the parser so first token of element is already fetched!
        elementParser(this)
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



