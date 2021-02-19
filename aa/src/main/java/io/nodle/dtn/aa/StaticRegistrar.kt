package io.nodle.dtn.aa

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.interfaces.IApplicationAgent
import io.nodle.dtn.interfaces.IRegistrar
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */

class StaticRegistrar : IRegistrar {
    val log = LoggerFactory.getLogger("Registrar")

    var staticRegistration: MutableMap<URI, IApplicationAgent> = mutableMapOf()

    override fun register(eid: URI, aa: IApplicationAgent): Boolean {
        if (staticRegistration.containsKey(eid)) {
            return false
        }
        staticRegistration[eid] = aa
        return true
    }

    override fun listEndpoint(): List<URI> {
        return staticRegistration.map { it.key }
    }

    override fun localDelivery(bundle: Bundle): IApplicationAgent? {
        for ((k, v) in staticRegistration) {
            if (k == bundle.primaryBlock.destination) {
                log.debug("application agent found for bundle ${bundle.hashCode()}")
                return v
            }
        }
        return null
    }
}