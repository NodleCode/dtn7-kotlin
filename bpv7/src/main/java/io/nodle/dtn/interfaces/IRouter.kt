package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle

/**
 * @author Lucien Loiseau on 17/02/21.
 */

sealed class TransmissionStatus {
    object ClaNotFound : TransmissionStatus()
    object TransmissionSuccessful : TransmissionStatus()
    object TransmissionTemporaryUnavailable : TransmissionStatus()
    object TransmissionFailed : TransmissionStatus()
    object TransmissionRejected : TransmissionStatus()
}

interface IRouter {

    fun findRoute(bundle : Bundle) : IConvergenceLayerSender?

    fun declareFailure(bundle: Bundle, status: TransmissionStatus) : Boolean
    
}