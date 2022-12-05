package io.nodle.dtn.interfaces

import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */

class RegistrationAlreadyExists : Exception()

interface IRegistrar {

    /**
     * registers an application agent
     * @param eid to register for this aa
     * @param application agent to register
     * @return true if the registration was successful, false if URI already registered
     */
    fun register(eid: URI, aa: IApplicationAgent): Boolean

    /**
     * listEndpoints return a listing of all the currently registered appplication-agent
     * @return list of all registered endpoint id
     */
    fun listEndpoint() : List<URI>

    /**
     * localDelivery returns the matching application agent for a given destination eid, if any
     * it returns the first application agent that matches the eid
     * or null if no such endpoint were found.
     *
     * @param destination the endpoint id
     * @return a convergence layer or null
     */
    fun localDelivery(destination: URI) : IApplicationAgent?

}