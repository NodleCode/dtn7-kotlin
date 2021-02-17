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
import io.nodle.dtn.bpv7.extensions.readHopCountBlockData
import io.nodle.dtn.utils.*
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException

/**
 * @author Lucien Loiseau on 12/02/21.
 */
class CborParsingException(msg: String) : Exception(msg)

val extensionBlockParserRegister = HashMap<Int, (CBORParser) -> ExtensionBlockData>()
    .putElement(BlockType.BlockIntegrityBlock.code, { it.readASBlockData() })
    .putElement(BlockType.BlockConfidentialityBlock.code, { it.readASBlockData() })
    .putElement(BlockType.BundleAgeBlock.code, { it.readBundleAgeBlockData() })
    .putElement(BlockType.HopCountBlock.code, { it.readHopCountBlockData() })

@Throws(CborParsingException::class)
fun cborUnmarshalBundle(buffer: ByteArray) =
    cborUnmarshalBundle(ByteArrayInputStream(buffer))

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

            // parse block-specific data
            block.data = extensionBlockParserRegister[block.blockType]?.let {
                val cbor = CBORFactory().createParser(readByteArray())
                it(cbor)
            } ?: run {
                BlobBlockData(readByteArray())
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



