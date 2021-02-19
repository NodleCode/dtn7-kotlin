package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IApplicationAgent {

    suspend fun deliver(bundle : Bundle) : Boolean

}