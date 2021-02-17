package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IAgent {
    fun isEndpoint(eid : URI) : Boolean

    suspend fun receive(bundle : Bundle)

    suspend fun transmit(bundle : Bundle)

    fun getRegistrar() : IRegistrar

    fun getRouter() : IRouter
}
