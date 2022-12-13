package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle

typealias ForwardingTxHandler =  (suspend (desc: BundleDescriptor, isCancelled: Boolean) -> TransmissionStatus)

interface IBundleProtocolAgent {

    /**
     * TransmitADU is called by application agent in order to schedule the transmission of a bundle
     * (flowing downward).
     */
    suspend fun transmitADU(bundle: Bundle)

    /**
     * receivePDU is called by convergence layers in order to signal the reception of a bundle
     * (flowing upward).
     */
    suspend fun receivePDU(bundle: Bundle)

    /**
     * resumeForwarding is called by the node whenever it wants to resume the forwarding of a
     * bundle previously marked for `forwarding contraindicated`. resumeForwarding will resume at
     * step 5.4 of the bundle protocol. If the bundle is schedule for transmission, and based on
     * the option next parameter, it will either:
     * - If `next` is null the bundle protocol agent will use the internal router to direct
     *   the bundle to the appropriate CLA, based on routing and channel availabilities.
     * - If `next` is not null, the bundle protocol agent will submit the bundle to `next` for
     *   further processing. This is especially useful if many bundles need to be processed and
     *   prepared right before opening a channel with some endpoint.
     */
    suspend fun resumeForwarding(desc: BundleDescriptor)
    suspend fun resumeForwarding(
        desc: BundleDescriptor,
        txHandler: ForwardingTxHandler
    )

}