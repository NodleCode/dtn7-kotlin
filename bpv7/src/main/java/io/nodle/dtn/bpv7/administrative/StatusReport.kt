package io.nodle.dtn.bpv7.administrative

import io.nodle.dtn.bpv7.Bundle
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */

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

data class StatusItem(
    var statusAssertion: Int,
    var asserted: Boolean = false,
    var timestamp: Long = 0,
)

fun statusRecord(
    bundle: Bundle,
    assertion: StatusAssertion,
    reason: StatusReportReason,
    time: Long
): AdministrativeRecord {
    return AdministrativeRecord(
        recordTypeCode = RecordTypeCode.StatusRecordType.code,
        data = StatusReport()
            .assert(assertion, true, time)
            .reason(reason)
            .creationTimestamp(bundle.primaryBlock.creationTimestamp)
            .source(bundle.primaryBlock.source)
    )
}

fun StatusReport.assert(status: StatusAssertion, assert: Boolean, time: Long) =
    assert(status.code, assert, time)

fun StatusReport.assert(status: Int, assert: Boolean, time: Long): StatusReport {
    when (status) {
        StatusAssertion.ReceivedBundle.code -> received = time
        StatusAssertion.ForwardedBundle.code -> forwarded = time
        StatusAssertion.DeliveredBundle.code -> delivered = time
        StatusAssertion.DeletedBundle.code -> deleted = time
        else -> otherAssertions.firstOrNull { it.statusAssertion == status }
            ?.apply {
                asserted = assert
                timestamp = time
            } ?: otherAssertions.add(StatusItem(status, assert, time))
    }
    return this
}

fun StatusReport.assertTime(status: StatusAssertion): Long = assertTime(status.code)
fun StatusReport.assertTime(status: Int): Long {
    return when (status) {
        StatusAssertion.ReceivedBundle.code -> received
        StatusAssertion.ForwardedBundle.code -> forwarded
        StatusAssertion.DeliveredBundle.code -> delivered
        StatusAssertion.DeletedBundle.code -> deleted
        else -> otherAssertions.getOrNull(status)?.timestamp ?: 0
    }
}

fun StatusReport.isAsserted(status: StatusAssertion): Boolean = assertTime(status) > 0
fun StatusReport.isAsserted(status: Int): Boolean = assertTime(status) > 0

fun StatusReport.reason(reason: StatusReportReason): StatusReport {
    bundleStatusReportReason = reason.code
    return this
}

fun StatusReport.source(src: URI): StatusReport {
    sourceNodeId = src
    return this
}

fun StatusReport.creationTimestamp(timestamp: Long): StatusReport {
    creationTimestamp = timestamp
    return this
}

fun StatusReport.offset(off: Long): StatusReport {
    fragmentOffset = off
    return this
}

fun StatusReport.appDataLength(length: Long): StatusReport {
    appDataLength = length
    return this
}

