package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */

sealed class TransmissionStatus {
    object ClaNotFound : TransmissionStatus()
    object TransmissionSuccessful : TransmissionStatus()
    object TransmissionTemporaryUnavailable : TransmissionStatus()
    object TransmissionFailed : TransmissionStatus()
    object TransmissionRejected : TransmissionStatus()
    object TransmissionCancelled : TransmissionStatus()
}

typealias BundleToClaMatcher = (PrimaryBlockDescriptor) -> Boolean

interface IRouter {

    /**
     * getBundleToClaMatcher is a higher-order function that given a Convergence Layer,
     * returns a predicate lambda that takes a Bundle as a parameter and return true
     * if this bundle should be forwarded to the given CLA or false otherwise. This
     * method is used when a convergence layer becomes available and we need to retrieve
     * relevant bundle from the storage to be forwarded over.
     */
    fun getBundleToClaMatcher(claPeerEid: URI) : BundleToClaMatcher

    /**
     * findRoute returns the convergence layer to use for a given dtn endpoint id.
     * it returns the first convergence layer that matches the destination eid of the bundle
     * or null if no such endpoint were found.
     *
     * @param bundle the bundle to forwards
     * @return a convergence layer or null
     */
    fun findRoute(bundle : Bundle) : IConvergenceLayerSender?

    /**
     * declareFailure is called whenever a forwarding attempt has failed for a given bundle.
     * it returns true if failure should be declared for this bundle or false otherwise.
     * A bundle marked for failure is usually deleted.
     */
    fun declareFailure(bundle: Bundle, status: TransmissionStatus) : Boolean
    
}