package io.nodle.dtn

import io.nodle.dtn.aa.mux.MuxAgent
import io.nodle.dtn.cla.StaticRoutingTable
import io.nodle.dtn.interfaces.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */
open class BpNode(
    nodeId: URI,
    override val bundleStorage : IBundleStorage,
) : IBundleNode {

    private val scope = CoroutineScope(Dispatchers.IO)

    override val router = StaticRoutingTable()

    override val bundleProtocolAgent = BundleProtocolAgent(this)

    override val applicationAgent = MuxAgent(nodeId) {
        scope.launch {
            bundleProtocolAgent.transmitADU(it)
        }
        true
    }
}