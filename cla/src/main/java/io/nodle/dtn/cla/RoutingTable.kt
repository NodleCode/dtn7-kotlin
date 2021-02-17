package io.nodle.dtn.cla

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.interfaces.IConvergenceLayerSender
import io.nodle.dtn.interfaces.IRouter
import org.slf4j.LoggerFactory
import java.net.URI

class RoutingTable : IRouter {
    val log = LoggerFactory.getLogger("RoutingTable")

    // main routing table
    var staticRoutes : MutableMap<URI, IConvergenceLayerSender> = HashMap()

    /**
     * findRoute returns the convergence layer to use for a given dtn endpoint id.
     * it returns the first convergence layer that matches the destination eid of the bundle
     * or null if no such endpoint were found.
     *
     * @param bundle the bundle to forwards
     * @return a convergence layer or null
     */
    override fun findRoute(bundle : Bundle) : IConvergenceLayerSender? {
        for((k, v) in staticRoutes) {
            if(k == bundle.primaryBlock.destination) {
                log.debug("route ${v.getPeerEndpointId()} found for bundle ${bundle.hashCode()}")
                return v
            }
        }
        return null
    }
}