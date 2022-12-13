package io.nodle.dtn.bpv7.administrative

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.DtnTime
import io.nodle.dtn.bpv7.dtnTimeNow
import io.nodle.dtn.bpv7.eid.nullDtnEid
import io.nodle.dtn.bpv7.isFragment
import java.net.URI
import java.util.*

/**
 * @author Lucien Loiseau on 17/02/21.
 */

data class StatusReport(
    var received: DtnTime = 0,
    var forwarded: DtnTime = 0,
    var delivered: DtnTime = 0,
    var deleted: DtnTime = 0,
    var otherAssertions: MutableList<StatusItem> = mutableListOf(),
    var bundleStatusReportReason: Int = StatusReportReason.NoInformation.code,
    var sourceNodeId: URI = nullDtnEid(),
    var creationTimestamp: DtnTime = 0,
    var sequenceNumber: Long = 0,
    var isFragment: Boolean = false,
    var fragmentOffset: Long = 0,
    var appDataLength: Long = 0
) : AdministrativeData()

fun statusReport(bundle: Bundle): StatusReport =
    StatusReport(
        sourceNodeId = bundle.primaryBlock.source,
        creationTimestamp = bundle.primaryBlock.creationTimestamp,
        sequenceNumber = bundle.primaryBlock.sequenceNumber,
        isFragment = bundle.primaryBlock.isFragment(),
        fragmentOffset = bundle.primaryBlock.fragmentOffset,
        appDataLength = bundle.primaryBlock.appDataLength
    )

data class StatusItem(
    var statusAssertion: Int,
    var asserted: Boolean = false,
    var timestamp: DtnTime = 0,
)

enum class StatusAssertion(val code: Int) {
    ReceivedBundle(0),
    ForwardedBundle(1),
    DeliveredBundle(2),
    DeletedBundle(3);

    override fun toString(): String {
        return this.name + "($code)"
    }
}

enum class StatusReportReason(val code: Int) {
    // NoInformation is the "No additional information" bundle status report
    // reason code.
    NoInformation(0),

    // LifetimeExpired is the "Lifetime expired" bundle status report reason code.
    LifetimeExpired(1),

    // ForwardUnidirectionalLink is the "Forwarded over unidirectional link"
    // bundle status report reason code.
    ForwardUnidirectionalLink(2),

    // TransmissionCanceled is the "Transmission canceled" bundle status report
    // reason code.
    TransmissionCanceled(3),

    // DepletedStorage is the "Depleted storage" bundle status report reason code.
    DepletedStorage(4),

    // DestEndpointUnintelligible is the "Destination endpoint ID unintelligible"
    // bundle status report reason code.
    DestEndpointUnintelligible(5),

    // NoRouteToDestination is the "No known route to destination from here"
    // bundle status report reason code.
    NoRouteToDestination(6),

    // NoNextNodeContact is the "No timely contact with next node on route" bundle
    // status report reason code.
    NoNextNodeContact(7),

    // BlockUnintelligible is the "Block unintelligible" bundle status report
    // reason code.
    BlockUnintelligible(8),

    // HopLimitExceeded is the "Hop limit exceeded" bundle status report reason
    // code.
    HopLimitExceeded(9),

    // TrafficPared is the "Traffic pared (e.g., status reports)" bundle status
    // report reason code.
    TrafficPared(10),

    // BlockUnsupported is the "Block unsupported" bundle status report reason
    // code.
    BlockUnsupported(11);

    override fun toString(): String {
        return this.name + "($code)"
    }
}

fun statusRecord(
    bundle: Bundle,
    assertion: StatusAssertion,
    reason: StatusReportReason,
    time: DtnTime = dtnTimeNow()
): AdministrativeRecord {
    return AdministrativeRecord(
        recordTypeCode = RecordTypeCode.StatusRecordType.code,
        data = statusReport(bundle)
            .assert(assertion, true, time)
            .reason(reason)
    )
}

fun StatusReport.assert(status: StatusAssertion, assert: Boolean, time: DtnTime): StatusReport {
    when (status) {
        StatusAssertion.ReceivedBundle -> received = time
        StatusAssertion.ForwardedBundle -> forwarded = time
        StatusAssertion.DeliveredBundle -> delivered = time
        StatusAssertion.DeletedBundle -> deleted = time
        else -> otherAssertions.firstOrNull { it.statusAssertion == status.code }
            ?.apply {
                asserted = assert
                timestamp = time
            } ?: otherAssertions.add(StatusItem(status.code, assert, time))
    }
    return this
}

fun StatusReport.reason(reason: StatusReportReason): StatusReport {
    bundleStatusReportReason = reason.code
    return this
}

fun StatusReport.reportedId() : String {
    return UUID.nameUUIDFromBytes(
        (sourceNodeId.toASCIIString()
                + creationTimestamp
                + sequenceNumber
                + isFragment
                + fragmentOffset
                + appDataLength)
            .toByteArray()).toString()
}

fun StatusReport.assertion() : String {
    val sb = StringBuilder("");
    if(received > 0) {
        sb.append("${if (sb.isNotEmpty()) "| " else ""}RECEIVED ")
    }
    if(forwarded > 0) {
        sb.append("${if (sb.isNotEmpty()) "| " else ""}FORWARDED ")
    }
    if(delivered > 0) {
        sb.append("${if (sb.isNotEmpty()) "| " else ""}DELIVERED ")
    }
    if(deleted > 0) {
        sb.append("${if (sb.isNotEmpty()) "| " else ""}DELETED ")
    }
    return sb.toString()
}
