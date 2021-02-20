package io.nodle.dtn

import io.nodle.dtn.aa.StaticRegistrar
import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.ID
import io.nodle.dtn.cla.StaticRoutingTable
import io.nodle.dtn.interfaces.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory
import java.lang.Exception

/**
 * @author Lucien Loiseau on 17/02/21.
 */

abstract class BundleProtocolAgent(
        var bpAdministrativeAgent: IAdministrativeAgent = AdministrativeAgent(),
        var bpRegistrar: IRegistrar = StaticRegistrar(),
        var bpRouter: IRouter = StaticRoutingTable()
) : IAgent {

    val log = LoggerFactory.getLogger("BundleProtocolAgent")

    var agentJob: Job? = null

    override fun getRegistrar(): IRegistrar = bpRegistrar

    override fun getAdministrativeAgent(): IAdministrativeAgent = bpAdministrativeAgent

    override fun getRouter(): IRouter = bpRouter

    override suspend fun transmit(bundle: Bundle) {
        checkDuplicate(bundle) {
            val desc = processBundleTransmission(bundle)
            doneProcessing(desc)
        }
    }

    override suspend fun receive(bundle: Bundle) {
        checkDuplicate(bundle) {
            val desc = processReceivedBundle(bundle)
            doneProcessing(desc)
        }
    }

    private suspend fun checkDuplicate(bundle: Bundle, func: suspend (Bundle) -> Any) {
        if (!isDuplicate(bundle)) {
            func(bundle)
        } else {
            bpLog.debug("bundle:${bundle.ID()} - duplicate bundle, ignore")
        }
    }

    suspend fun processBundleTransmission(bundle: Bundle): BundleDescriptor =
            BundleDescriptor(bundle).apply {
                tags.add(BundleTag.OriginLocal.code)
                bundleTransmission(this)
            }

    suspend fun processReceivedBundle(bundle: Bundle): BundleDescriptor =
            BundleDescriptor(bundle).apply {
                tags.add(BundleTag.OriginCLA.code)
                bundleReceive(this)
            }

    abstract suspend fun isDuplicate(bundle: Bundle): Boolean

    abstract suspend fun doneProcessing(desc: BundleDescriptor)

}