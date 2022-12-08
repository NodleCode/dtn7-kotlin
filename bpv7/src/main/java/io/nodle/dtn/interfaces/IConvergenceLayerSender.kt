package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */
typealias ClaRxHandler = suspend (Bundle) -> Boolean
typealias ClaTxHandler = suspend (Bundle) -> TransmissionStatus

interface IConvergenceLayerSender {

    val peerEndpointId: URI

    val scheduleForTransmission: ClaTxHandler

}