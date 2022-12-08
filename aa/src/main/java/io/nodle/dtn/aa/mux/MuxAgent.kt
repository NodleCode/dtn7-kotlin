package io.nodle.dtn.aa.mux

import io.nodle.dtn.aa.AdministrativeAgent
import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.eid.swapApiMe
import io.nodle.dtn.interfaces.*
import io.nodle.dtn.utils.addPath
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */
class MuxAgent(
    private val nodeId: URI,
    override val scheduleForTransmission: AATxHandler,
) : IApplicationAgent {

    private val log = LoggerFactory.getLogger("MuxAgent")
    private var muxTable: MutableMap<URI, AARxHandler> = mutableMapOf()

    override val administrativeAgent: IAdministrativeAgent = AdministrativeAgent(nodeId)
    override fun endpoints() = muxTable.map{it.key}.toMutableList().apply { add(nodeId) }

    override val receiveForLocalDelivery: AARxHandler = { bundle -> localDelivery(bundle) }

    private suspend fun localDelivery(bundle: Bundle): DeliveryStatus {
        for ((k, v) in muxTable) {
            if ((k == bundle.primaryBlock.destination) || k.checkApiMe() == bundle.primaryBlock.destination) {
                log.debug("application agent found for eid ${bundle.primaryBlock.destination}")
                return v(bundle)
            }
        }
        return DeliveryStatus.EndpointNotFound
    }

    private fun URI.checkApiMe(): URI {
        try {
            if (authority == "api:me") {
                return swapApiMe(nodeId)
            }
        } catch (_: Exception) {
        }
        return this
    }

    fun handleEid(eid: URI, handler: AARxHandler): MuxAgent {
        if (!muxTable.containsKey(eid)) {
            muxTable[eid] = handler
        }
        return this
    }

    fun handlePath(path: String, handler: AARxHandler): MuxAgent {
        val eid = nodeId.addPath(path)
        return handleEid(eid, handler)
    }
}