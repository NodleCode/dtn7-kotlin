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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
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
    CBORFactory().createGenerator(CloseProtectOutputStream(out)).use {
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

    // prepare assertions
    val assertions = mutableListOf<StatusItem>()
    assertions.add(0, StatusItem(0, sr.received > 0, sr.received))
    assertions.add(1, StatusItem(1, sr.forwarded > 0, sr.forwarded))
    assertions.add(2, StatusItem(2, sr.delivered > 0, sr.delivered))
    assertions.add(3, StatusItem(3, sr.deleted > 0, sr.deleted))
    assertions.addAll(sr.otherAssertions)

    writeStartArray(assertions.size)
    assertions.forEach {
        cborMarshal(it)
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
fun cborUnmarshalAdmnistrativeRecord(buffer: ByteArray) =
        cborUnmarshalAdmnistrativeRecord(ByteArrayInputStream(buffer))

@Throws(CborParsingException::class)
fun cborUnmarshalAdmnistrativeRecord(input: InputStream): AdministrativeRecord {
    return CBORFactory()
            .createParser(input)
            .readAdministrativeRecord()
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
            otherAssertions = readArray { readStatusItem(assertCounter++, true) },
            bundleStatusReportReason = readInt(),
            sourceNodeId = readEid(),
            creationTimestamp = readLong()
    ).also {
        if (it.otherAssertions.size < 4) {
            throw CborParsingException("missing some of status assertion")
        }
        it.received = it.otherAssertions[0].timestamp
        it.forwarded = it.otherAssertions[1].timestamp
        it.delivered = it.otherAssertions[2].timestamp
        it.deleted = it.otherAssertions[3].timestamp
        if (it.otherAssertions.size == 4) {
            it.otherAssertions = mutableListOf()
        } else {
            it.otherAssertions = it.otherAssertions.subList(4, it.otherAssertions.size)
        }

        // unfortunately with jackson we cannot read the cbor array length
        // so we cannot know if there are the fragment offset part or not
        if (nextToken() != JsonToken.END_ARRAY) {
            it.fragmentOffset = longValue
            it.appDataLength = readLong()
            readCloseArray()
        }
    }
}

fun CBORParser.readStatusItem(code: Int, prefetch: Boolean): StatusItem {
    return readStruct(prefetch = prefetch) {
        val assert = readBoolean()
        val time = if (assert) {
            readLong()
        } else {
            0
        }
        StatusItem(code, assert, time)
    }
}