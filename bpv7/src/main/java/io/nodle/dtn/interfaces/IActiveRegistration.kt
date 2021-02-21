package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle

/**
 * An active registration allows the holder of such instance, the application agent,
 * to send bundle down the stack to the bundle protocol agent.
 *
 * @author Lucien Loiseau on 21/02/21.
 */
interface IActiveRegistration {

    suspend fun sendBundle(bundle : Bundle) : Boolean

}