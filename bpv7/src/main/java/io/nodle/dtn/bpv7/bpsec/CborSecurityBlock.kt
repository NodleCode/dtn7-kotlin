package io.nodle.dtn.bpv7.bpsec

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.CborEncodingException
import io.nodle.dtn.bpv7.CborParsingException
import io.nodle.dtn.bpv7.cborMarshal
import io.nodle.dtn.bpv7.readEid
import io.nodle.dtn.crypto.toEd25519PublicKey
import io.nodle.dtn.utils.*
import java.io.OutputStream

/**
 * @author Lucien Loiseau on 14/02/21.
 */
@Throws(CborEncodingException::class)
fun AbstractSecurityBlockData.cborMarshalData(out: OutputStream) {
    CBORFactory().createGenerator(out).use {
        it.writeStartArray(cborGetItemCount())
        it.writeStartArray(securityTargets.size)
        for (target in securityTargets) {
            it.writeNumber(target)
        }
        it.writeEndArray()
        it.writeNumber(securityContext)
        it.writeNumber(securityBlockV7Flags)
        it.cborMarshal(securitySource)

        if (hasSecurityParam()) {
            when (securityContext) {
                SecurityContext.Ed25519BlockSignature.id ->
                    it.cborMarshal(securityContextParameters as Ed25519SecurityParameter)
                else -> throw CborEncodingException("security block is unknown")
            }
        }

        it.writeStartArray(securityResults.size)
        for (targetResult in securityResults) {
            when (securityContext) {
                SecurityContext.Ed25519BlockSignature.id ->
                    it.cborMarshal(targetResult as Ed25519SecurityResult)
                else -> throw CborEncodingException("security block is unknown")
            }
        }
        it.writeEndArray()
        it.writeEndArray()
    }
}

fun CBORGenerator.cborMarshal(params: Ed25519SecurityParameter) {
    writeStartArray(2)

    // pubkey
    writeStartArray(2)
    writeNumber(0)
    writeBinary(params.ed25519PublicKey.hexToBa())
    writeEndArray()
    // timestamp
    writeStartArray(2)
    writeNumber(1)
    writeNumber(params.timestamp)
    writeEndArray()

    writeEndArray()
}

fun CBORGenerator.cborMarshal(result: Ed25519SecurityResult) {
    writeStartArray(1)

    // the actual signature
    writeStartArray(2)
    writeNumber(0)
    writeBinary(result.signature.hexToBa())
    writeEndArray()

    writeEndArray()
}

fun AbstractSecurityBlockData.cborGetItemCount(): Int {
    if (hasSecurityParam()) {
        return 6
    }
    return 5
}


@Throws(CborParsingException::class)
fun CBORParser.readASBlockData(): AbstractSecurityBlockData {
    return readStruct(false) {
        AbstractSecurityBlockData(
                securityTargets = readArray() { it.intValue },
                securityContext = readInt(),
                securityBlockV7Flags = readLong(),
                securitySource = readEid()
        ).also { asb ->
            if (asb.securityContext != SecurityContext.Ed25519BlockSignature.id) {
                throw CborParsingException("security context unknown")
            }

            // security parameters
            if (asb.hasSecurityParam()) {
                asb.securityContextParameters = readEd25519SecurityParameters()
            }

            // security results
            asb.securityResults = readArray {
                readEd25519SecurityResult(true)
            }
        }
    }
}

fun CBORParser.readEd25519SecurityParameters(): Ed25519SecurityParameter {
    readStartArray()

    readStartArray()
    readInt()
    val pubKey = readByteArray()
    readCloseArray()

    readStartArray()
    readInt()
    val time = readLong()
    readCloseArray()

    readCloseArray()
    return Ed25519SecurityParameter(pubKey.toHex(), time)
}


fun CBORParser.readEd25519SecurityResult(prefetch: Boolean): Ed25519SecurityResult {
    if (prefetch) {
        assertStartArray()
    } else {
        readStartArray()
    }
    readStartArray()
    readInt()
    val signature = readByteArray()
    readCloseArray()

    readCloseArray()
    return Ed25519SecurityResult(signature.toHex())
}



