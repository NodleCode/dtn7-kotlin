package io.nodle.dtn

import io.nodle.dtn.aa.StaticRegistrar
import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.cla.StaticRoutingTable
import io.nodle.dtn.interfaces.*

/**
 * @author Lucien Loiseau on 17/02/21.
 */

abstract class BundleProtocolAgent : IAgent {

    var bpAdministrativeAgent: IAdministrativeAgent = AdministrativeAgent()
    var bpRegistrar: IRegistrar = StaticRegistrar()
    var bpRouter: IRouter = StaticRoutingTable()

    override fun getRegistrar(): IRegistrar = bpRegistrar

    override fun getAdministrativeAgent(): IAdministrativeAgent = bpAdministrativeAgent

    override fun getRouter(): IRouter = bpRouter

    override suspend fun transmit(bundle: Bundle) {
        processBundleTransmission(bundle)
    }

    override suspend fun receive(bundle: Bundle) {
        processReceivedBundle(bundle)
    }

    suspend fun processReceivedBundle(bundle: Bundle) : BundleDescriptor =
            BundleDescriptor(bundle).apply {
                bundleReceive(this)
            }

    suspend fun processBundleTransmission(bundle: Bundle): BundleDescriptor =
            BundleDescriptor(bundle).apply {
                bundleTransmission(this)
            }
}