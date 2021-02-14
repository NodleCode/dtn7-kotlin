package io.nodle.dtn.bpv7.bpsec

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.*
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
            it.writeStartArray(securityContextParameters.size)
            for (p in securityContextParameters) {
                it.writeStartArray(2)
                it.writeNumber(p.id)
                it.writeBinary(p.parameter)
                it.writeEndArray()
            }
            it.writeEndArray()
        }

        it.writeStartArray(securityResults.size)
        for (targetResult in securityResults) {
            it.writeStartArray(targetResult.size)
            for (result in targetResult) {
                it.writeStartArray(2)
                it.writeNumber(result.id)
                it.writeBinary(result.result)
                it.writeEndArray()
            }
            it.writeEndArray()
        }
        it.writeEndArray()
        it.writeEndArray()
    }
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
            securityTargets = readArray(false) { it.intValue },
            securityContext = readInt(),
            securityBlockV7Flags = readLong(),
            securitySource = readEid()
        ).also {
            // security parameters
            if (it.hasSecurityParam()) {
                it.securityContextParameters = readArray(false) {
                    readStruct(true) { SecurityContextParameter(readInt(), readByteArray()) }
                }
            }

            // security results
            it.securityResults = readArray(false) {
                readArray(true) {
                    readStruct(true) { SecurityResult(readInt(), readByteArray()) }
                }
            }
        }
    }
}

