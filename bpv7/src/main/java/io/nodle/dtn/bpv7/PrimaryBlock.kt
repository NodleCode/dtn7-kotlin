package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.eid.nullDtnEid
import java.net.URI

/**
 * @author Lucien Loiseau on 12/02/21.
 */
data class PrimaryBlock(
        var version: Int,
        var procV7Flags: Long,
        var crcType: CRCType,
        var destination: URI,
        var source: URI,
        var reportTo: URI,
        var creationTimestamp: Long,
        var sequenceNumber: Long,
        var lifetime: Long,
        var fragmentOffset: Long = 0,
        var appDataLength: Long = 0)

var sequenceCounter: Long = 0

fun newPrimaryBlock() = PrimaryBlock(
        version = 7,
        procV7Flags = 0,
        crcType = CRCType.CRC32,
        destination = nullDtnEid(),
        source = nullDtnEid(),
        reportTo = nullDtnEid(),
        creationTimestamp = System.currentTimeMillis(),
        sequenceNumber = sequenceCounter++,
        lifetime = 10000)

fun PrimaryBlock.version(v : Int) : PrimaryBlock {
    this.version = v
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

fun PrimaryBlock.sequenceNumber(sequence : Long) : PrimaryBlock {
    this.sequenceNumber = sequence
    return this
}

fun PrimaryBlock.lifetime(lifetime : Long) : PrimaryBlock {
    this.lifetime = lifetime
    return this
}

fun PrimaryBlock.makeBundle() = Bundle(this, ArrayList())

