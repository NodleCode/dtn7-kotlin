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
    val gen = CBORFactory().createGenerator(out)
    gen.writeStartArray(cborGetItemCount())
    gen.writeStartArray(securityTargets.size)
    for (target in securityTargets) {
        gen.writeNumber(target)
    }
    gen.writeEndArray()
    gen.writeNumber(securityContext)
    gen.writeNumber(securityBlockV7Flags)
    securitySource.cborMarshal(gen)

    if(hasSecurityParam()) {
        gen.writeStartArray(securityContextParameters.size)
        for(p in securityContextParameters) {
            gen.writeStartArray(2)
            gen.writeNumber(p.id)
            gen.writeBinary(p.parameter)
            gen.writeEndArray()
        }
        gen.writeEndArray()
    }

    gen.writeStartArray(securityResults.size)
    for (targetResult in securityResults) {
        gen.writeStartArray(targetResult.size)
        for (result in targetResult) {
            gen.writeStartArray(2)
            gen.writeNumber(result.id)
            gen.writeBinary(result.result)
            gen.writeEndArray()
        }
        gen.writeEndArray()
    }
    gen.writeEndArray()
    gen.writeEndArray()
    gen.flush()
}

fun AbstractSecurityBlockData.cborGetItemCount(): Int {
    if(hasSecurityParam()) {
        return 6
    }
    return 5
}


@Throws(CborParsingException::class)
fun CBORParser.readASBlockData(): AbstractSecurityBlockData {
    val ret = AbstractSecurityBlockData()
    readStartArray()
    readArray(false) {
        ret.securityTargets.add(it.intValue)
    }
    ret.securityContext = readInt()
    ret.securityBlockV7Flags = readLong()
    ret.securitySource = readEid()

    // security parameters
    if(ret.hasSecurityParam()) {
        readArray(false) {
            assertStartArray()
            ret.securityContextParameters.add(SecurityContextParameter(readInt(), readByteArray()))
            readCloseArray()
        }
    }

    // security results
    readArray(false) {
        assertStartArray()
        val results = ArrayList<SecurityResult>()
        readArray(true) {
            assertStartArray()
            results.add(SecurityResult(readInt(), readByteArray()))
            readCloseArray()
        }
        ret.securityResults.add(results)
    }
    readCloseArray()
    return ret
}

