package io.nodle.dtn.bpv7.administrative

/**
 * @author Lucien Loiseau on 17/02/21.
 */
enum class StatusReportReason(val code : Int) {
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