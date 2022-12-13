package io.nodle.dtn.cla

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.interfaces.BundleToClaMatcher
import io.nodle.dtn.interfaces.IConvergenceLayerSender
import io.nodle.dtn.interfaces.IRouter
import io.nodle.dtn.interfaces.TransmissionStatus
import org.slf4j.LoggerFactory
import java.net.URI

class StaticRoutingTable : IRouter {
    private val log = LoggerFactory.getLogger("RoutingTable")

    // main routing table
    private var staticRoutes: MutableMap<URI, IConvergenceLayerSender> = HashMap()
    private var default: IConvergenceLayerSender? = null

    fun setDefaultRoute(cla: IConvergenceLayerSender?) {
        default = cla
    }

    override fun getBundleToClaMatcher(claPeerEid: URI): BundleToClaMatcher {
        val allDestinationEidRoutingToClaPeerEid =
            staticRoutes
                .filter { it.value.peerEndpointId == claPeerEid }
                .map { it.key }

        return { primaryDesc ->
            val bundleDest = primaryDesc.primaryBlock.destination.toASCIIString()
            bundleDest.startsWith(claPeerEid.toASCIIString())
                    || (default?.peerEndpointId == claPeerEid)
                    || allDestinationEidRoutingToClaPeerEid.any {
                bundleDest.startsWith(it.toASCIIString())
            }
        }
    }

    override fun findRoute(bundle: Bundle): IConvergenceLayerSender? {
        for ((k, v) in staticRoutes) {
            if (k == bundle.primaryBlock.destination) {
                log.debug("route ${v.peerEndpointId} found for bundle ${bundle.hashCode()}")
                return v
            }
        }
        return default
    }

    override fun declareFailure(bundle: Bundle, status: TransmissionStatus) =
        when (status) {
            TransmissionStatus.ClaNotFound,
            TransmissionStatus.TransmissionSuccessful,
            TransmissionStatus.TransmissionFailed,
            TransmissionStatus.TransmissionTemporaryUnavailable -> false
            else -> true
        }
}