package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.administrative.StatusAssertion
import io.nodle.dtn.bpv7.administrative.StatusReportReason
import kotlinx.coroutines.CoroutineScope
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IAgent {

    /**
     * @return the DTN Endpoint Id of the current bundle protocol agent.
     */
    fun nodeId() : URI

    /**
     * checks if a given Endpoint Id is local or not.
     * @param eid: an endpoint Id
     * @return true if the eid matches one of the application agent registered eid, false otherwise.
     */
    fun hasEndpoint(eid : URI) : Boolean

    /**
     * get the registrar instance associated with this bundle protocol agent.
     */
    fun getRegistrar() : IRegistrar

    /**
     * get the administrative agent instance associated with this bundle protocol agent.
     */
    fun getAdministrativeAgent(): IAdministrativeAgent

    /**
     * get the routing agent associated with this bundle protocol agent.
     */
    fun getRouter() : IRouter

    /**
     * this is the main entry point for incoming bundle.
     * @param bundle: bundle to process for reception
     */
    suspend fun receive(bundle : Bundle)

    /**
     * this is the main entry point for outgoing bundle.
     * @param bundle: bundle to process for transmission
     */
    suspend fun transmit(bundle : Bundle)

    /**
     * this is to tell the bundle protocol agent that there might be an opportunity for
     * communication and it needs to check the network interface and storage.
     */
    suspend fun checkForwardOpportunity()
}
