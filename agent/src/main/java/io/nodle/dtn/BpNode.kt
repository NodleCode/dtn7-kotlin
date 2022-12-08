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
class BpNode(
    nodeId: URI,
    override val store : Bpv7Storage = NoBpv7Storage,
) : IBundleNode {

    private val scope = CoroutineScope(Dispatchers.IO)

    override val applicationAgent = MuxAgent(nodeId) {
        scope.launch {
            bpa.transmitADU(it)
        }
        true
    }

    override val bpa = BundleProtocolAgent(this)

    override val router = StaticRoutingTable()

}