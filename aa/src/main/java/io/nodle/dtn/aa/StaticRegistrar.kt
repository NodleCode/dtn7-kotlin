package io.nodle.dtn.aa

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.ID
import io.nodle.dtn.bpv7.eid.swapApiMe
import io.nodle.dtn.interfaces.IActiveRegistration
import io.nodle.dtn.interfaces.IAgent
import io.nodle.dtn.interfaces.IApplicationAgent
import io.nodle.dtn.interfaces.IRegistrar
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */

class StaticRegistrar(val agent: IAgent) : IRegistrar {
    private val log = LoggerFactory.getLogger("Registrar")

    var staticRegistration: MutableMap<URI, IApplicationAgent> = mutableMapOf()

    override fun register(eid: URI, aa: IApplicationAgent): Boolean {
        if (staticRegistration.containsKey(eid)) {
            return false
        }
        staticRegistration[eid] = aa

        // set the registration handler on the AA
        aa.onRegistrationActive(object : IActiveRegistration {
            override suspend fun sendBundle(bundle: Bundle): Boolean {
                agent.transmit(bundle)
                return true
            }

            override suspend fun unregister() {
                staticRegistration.remove(eid)
            }
        })
        return true
    }

    override fun listEndpoint(): List<URI> {
        return staticRegistration.map { it.key.checkApiMe() }
    }

    override fun localDelivery(bundle: Bundle): IApplicationAgent? {
        for ((k, v) in staticRegistration) {
            if ((k == bundle.primaryBlock.destination)
                    || k.checkApiMe() == bundle.primaryBlock.destination) {
                log.debug("application agent found for bundle ${bundle.ID()}")
                return v
            }
        }
        return null
    }

    private fun URI.checkApiMe(): URI {
        try {
            if (authority == "api:me") {
                return swapApiMe(agent.nodeId())
            }
        } catch (e: Exception) {
        }
        return this
    }
}