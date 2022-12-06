package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle
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
    fun isLocal(eid : URI) : Boolean

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
     * get the storage layer associated with this bundle protocol agent
     */
    fun getBundleStorage() : IBundleStorage

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
}
