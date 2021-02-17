package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IConvergenceLayerSender {

    suspend fun sendBundle(bundle : Bundle) : Boolean

    fun getPeerEndpointId() : URI

}