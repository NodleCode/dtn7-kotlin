package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IRegistrar {

    /**
     * registers an application agent
     * @param eid to register for this aa
     * @param application agent to register
     */
    fun register(eid: URI, aa: IApplicationAgent): Boolean

    /**
     * listEndpoints return a listing of all the currently registered appplication-agent
     * @return list of all registered endpoint id
     */
    fun listEndpoint() : List<URI>

    /**
     * localDelivery returns the matching application agent for a given bundle, if any
     * it returns the first application agent that matches the destination eid of the bundle
     * or null if no such endpoint were found.
     *
     * @param bundle the bundle to forwards
     * @return a convergence layer or null
     */
    fun localDelivery(bundle : Bundle) : IApplicationAgent?

}