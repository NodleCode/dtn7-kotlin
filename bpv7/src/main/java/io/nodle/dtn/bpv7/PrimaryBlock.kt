package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.eid.nullDtnEid
import io.nodle.dtn.utils.LastBufferOutputStream
import io.nodle.dtn.utils.isFlagSet
import io.nodle.dtn.utils.setFlag
import java.net.URI
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Lucien Loiseau on 12/02/21.
 */
var sequenceCounter: Long = 0

data class PrimaryBlock(
    var version: Int = 7,
    var procV7Flags: Long = 0,
    var crcType: CRCType = CRCType.CRC32,
    var destination: URI = nullDtnEid(),
    var source: URI = nullDtnEid(),
    var reportTo: URI = nullDtnEid(),
    var creationTimestamp: Long = System.currentTimeMillis(),
    var sequenceNumber: Long = sequenceCounter++,
    var lifetime: Long = 3600000,
    var fragmentOffset: Long = 0,
    var appDataLength: Long = 0)

fun PrimaryBlock.version(v : Int) : PrimaryBlock {
    this.version = v
    return this
}

fun PrimaryBlock.procV7Flags(flags : BundleV7Flags) : PrimaryBlock {
    this.procV7Flags = this.procV7Flags.setFlag(flags.offset)
    return this
}

fun PrimaryBlock.procV7Flags(flags : Long) : PrimaryBlock {
    this.procV7Flags = flags
    return this
}

fun PrimaryBlock.crcType(type : CRCType) : PrimaryBlock {
    this.crcType = type
    return this
}

fun PrimaryBlock.destination(uri : URI) : PrimaryBlock {
    this.destination = uri
    return this
}

fun PrimaryBlock.source(uri : URI) : PrimaryBlock {
    this.source = uri
    return this
}

fun PrimaryBlock.reportTo(uri : URI) : PrimaryBlock {
    this.reportTo = uri
    return this
}

fun PrimaryBlock.creationTimestamp(timestamp : Long) : PrimaryBlock {
    this.creationTimestamp = timestamp
    return this
}

fun PrimaryBlock.sequenceNumber(sequence : Long) : PrimaryBlock {
    this.sequenceNumber = sequence
    return this
}

fun PrimaryBlock.lifetime(lifetime : Long) : PrimaryBlock {
    this.lifetime = lifetime
    return this
}

fun PrimaryBlock.isFragment() : Boolean = procV7Flags.isFlagSet(BundleV7Flags.IsFragment.offset)

fun PrimaryBlock.isAdministiveRecord() : Boolean = procV7Flags.isFlagSet(BundleV7Flags.AdministrativeRecordPayload.offset)

fun PrimaryBlock.makeBundle() = Bundle(this, ArrayList())

fun PrimaryBlock.hasCRC() : Boolean = (crcType != CRCType.NoCRC)

fun PrimaryBlock.checkCRC(crc : ByteArray) : Boolean {
    val buf = when(crcType) {
        CRCType.CRC16 -> LastBufferOutputStream(2)
        CRCType.CRC32 -> LastBufferOutputStream(4)
        else -> return true
    }
    cborMarshal(buf)
    return buf.last().contentEquals(crc)
}

// ID is unique accross all bundle
fun PrimaryBlock.ID(): String {
    return UUID.nameUUIDFromBytes((source.toASCIIString() +
            creationTimestamp +
            sequenceNumber +
            isFragment() +
            fragmentOffset +
            appDataLength)
            .toByteArray()).toString()
}

// fragmentedID is shared by all fragment of the same bundle
fun PrimaryBlock.fragmentedID(): String {
    return UUID.nameUUIDFromBytes((source.toASCIIString() +
            creationTimestamp +
            sequenceNumber).toByteArray()).toString()
}