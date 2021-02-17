package io.nodle.dtn.aa

import io.nodle.dtn.bpv7.Bundle
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface ApplicationAgent {

    suspend fun receiveBundle(bundle : Bundle) : Boolean

    fun getApplicationAgentEndpointIds() : List<URI>

}