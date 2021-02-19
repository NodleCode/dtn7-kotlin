package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.administrative.StatusAssertion
import io.nodle.dtn.bpv7.administrative.StatusReportReason
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IAgent {

    fun nodeId() : URI

    fun hasEndpoint(eid : URI) : Boolean

    fun getRegistrar() : IRegistrar

    fun getAdministrativeAgent(): IAdministrativeAgent

    fun getRouter() : IRouter

    fun getStorage() : IStorage

    suspend fun receive(bundle : Bundle)

    suspend fun transmit(bundle : Bundle)
}
