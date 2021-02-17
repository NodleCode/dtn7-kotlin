package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IRouter {
    fun findRoute(bundle : Bundle) : IConvergenceLayerSender?
}