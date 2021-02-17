package io.nodle.dtn

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.interfaces.IAgent

/**
 * @author Lucien Loiseau on 17/02/21.
 */

abstract class DtnAgent : IAgent {

    override suspend fun receive(bundle: Bundle) {
        bundleReceive(BundleDescriptor( bundle))
    }

    override suspend fun transmit(bundle: Bundle) {
        bundleTransmission(BundleDescriptor(bundle))
    }

}