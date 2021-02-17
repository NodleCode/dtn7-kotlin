package io.nodle.dtn.aa

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.interfaces.IApplicationAgent
import io.nodle.dtn.interfaces.IRegistrar
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */

class Registrar : IRegistrar {
    val log = LoggerFactory.getLogger("Registrar")

    // main routing table
    var staticRoutes : MutableMap<URI, IApplicationAgent> = HashMap()

    /**
     * localDelivery returns the matching application agent for a given bundle, if any
     * it returns the first application agent that matches the destination eid of the bundle
     * or null if no such endpoint were found.
     *
     * @param bundle the bundle to forwards
     * @return a convergence layer or null
     */
    override fun localDelivery(bundle : Bundle) : IApplicationAgent? {
        for((k, v) in staticRoutes) {
            if(k == bundle.primaryBlock.destination) {
                log.debug("application agent found for bundle ${bundle.hashCode()}")
                return v
            }
        }
        return null
    }
}