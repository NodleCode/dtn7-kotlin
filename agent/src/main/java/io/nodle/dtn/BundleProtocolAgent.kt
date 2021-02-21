package io.nodle.dtn

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.ID
import io.nodle.dtn.interfaces.*
import org.slf4j.LoggerFactory

/**
 * @author Lucien Loiseau on 17/02/21.
 */

abstract class BundleProtocolAgent : IAgent {

    val bpaLog = LoggerFactory.getLogger("BundleProtocolAgent")

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

    private suspend fun processBundleTransmission(bundle: Bundle): BundleDescriptor =
            BundleDescriptor(bundle).apply {
                tags.add(BundleTag.OriginLocal.code)
                bundleTransmission(this)
            }

    private suspend fun processReceivedBundle(bundle: Bundle): BundleDescriptor =
            BundleDescriptor(bundle).apply {
                tags.add(BundleTag.OriginCLA.code)
                bundleReceive(this)
            }

    abstract suspend fun isDuplicate(bundle: Bundle): Boolean

    abstract suspend fun doneProcessing(desc: BundleDescriptor)

}