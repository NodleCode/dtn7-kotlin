package io.nodle.dtn.bpv7.administrative

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.CborEncodingException
import io.nodle.dtn.bpv7.CborParsingException
import io.nodle.dtn.bpv7.cborMarshal
import io.nodle.dtn.bpv7.readEid
import io.nodle.dtn.utils.*
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * @author Lucien Loiseau on 18/02/21.
 */
@Throws(CborEncodingException::class)
fun AdministrativeRecord.cborMarshalData(): ByteArray =
        ByteArrayOutputStream().use {
            cborMarshalData(it)
            it
        }.toByteArray()

@Throws(CborEncodingException::class)
fun AdministrativeRecord.cborMarshalData(out: OutputStream) {
    CBORFactory().createGenerator(out).use {
        it.writeStartArray(2)
        it.writeNumber(recordTypeCode)
        when (data) {
            is StatusReport -> it.cborMarshal(data as StatusReport)
        }
        it.writeEndArray()
    }
}

fun CBORGenerator.cborMarshal(sr: StatusReport) {
    writeStartArray(sr.cborGetItemCount())
    writeStartArray(4)
    for (i in 0..3) {
        if (sr.bundleStatusInformation.none { it.statusAssertion == i }) {
            cborMarshal(StatusItem(i, false))
        } else {
            cborMarshal(sr.bundleStatusInformation.first { it.statusAssertion == i })
        }
    }
    writeEndArray()
    writeNumber(sr.bundleStatusReportReason)
    cborMarshal(sr.sourceNodeId)
    writeNumber(sr.creationTimestamp)
    if (sr.fragmentOffset.toInt() != -1) {
        writeNumber(sr.fragmentOffset)
        writeNumber(sr.appDataLength)
    }
    writeEndArray()
}

fun CBORGenerator.cborMarshal(item: StatusItem) {
    writeStartArray(item.cborGetItemCount())
    writeBoolean(item.asserted)
    if (item.asserted) {
        writeNumber(item.timestamp)
    }
    writeEndArray()
}

fun StatusReport.cborGetItemCount(): Int {
    if (fragmentOffset.toInt() == -1) {
        return 4
    }
    return 6
}


fun StatusItem.cborGetItemCount(): Int {
    return if (asserted) {
        2
    } else {
        1
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readAdministrativeRecord(): AdministrativeRecord {
    readStartArray()
    return AdministrativeRecord(
            recordTypeCode = readInt()
    ).also { adm ->
        adm.data = when (adm.recordTypeCode) {
            RecordTypeCode.StatusRecordType.code -> readStatusReport()
            else -> throw CborParsingException("unsupported administrative record")
        }
    }
    // we don't close the array as it will be closed by status report
    // see below for explanation
}


@Throws(CborParsingException::class)
fun CBORParser.readStatusReport(): StatusReport {
    readStartArray()
    var assertCounter = 0
    return StatusReport(
            bundleStatusInformation = readArray {
                readStruct(true) {
                    val assertion = readBoolean()
                    StatusItem(
                            assertCounter++,
                            assertion,
                            if (assertion) {
                                readLong()
                            } else {
                                0
                            })
                }
            },
            bundleStatusReportReason = readInt(),
            sourceNodeId = readEid(),
            creationTimestamp = readLong()
    ).also {
        // unfortunately with jackson we cannot read the cbor array length
        // so we have to pre-fetch the token and check if it is end of array
        // or next element
        if (nextToken() != JsonToken.END_ARRAY) {
            it.fragmentOffset = longValue
            it.appDataLength = readLong()
            readCloseArray()
        }
    }
}