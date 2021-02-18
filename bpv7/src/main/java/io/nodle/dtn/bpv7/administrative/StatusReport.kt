package io.nodle.dtn.bpv7.administrative

import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */

enum class StatusAssertion(val code: Int) {
    ReceivedBundle(0),
    ForwardedBundle(1),
    DeliveredBundle(2),
    DeletedBundle(3)
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
    BlockUnsupported(11)
}

data class StatusItem(
        var statusAssertion: Int,
        var asserted: Boolean = false,
        var timestamp: Long = 0,
)

fun StatusReport.assert(status: StatusAssertion, assert: Boolean, time: Long): StatusReport {
    if (bundleStatusInformation.none { it.statusAssertion == status.code }) {
        bundleStatusInformation.add(StatusItem(
                statusAssertion = status.code,
                asserted = assert,
                timestamp = time))
    } else {
        bundleStatusInformation.first { it.statusAssertion == status.code }
                .apply {
                    asserted = assert
                    timestamp = time
                }
    }
    return this
}

fun StatusReport.reason(reason: StatusReportReason) : StatusReport {
    bundleStatusReportReason = reason.code
    return this
}

fun StatusReport.source(src:URI) : StatusReport {
    sourceNodeId = src
    return this
}

fun StatusReport.creationTimestamp(timestamp: Long) : StatusReport {
    creationTimestamp = timestamp
    return this
}

fun StatusReport.offset(off: Long) : StatusReport {
    fragmentOffset = off
    return this
}

fun StatusReport.appDataLength(length: Long) : StatusReport {
    appDataLength = length
    return this
}

