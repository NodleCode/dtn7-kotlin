package io.nodle.dtn

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.ID
import io.nodle.dtn.interfaces.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BundleProtocolAgent(private val core: IBundleNode) : IBundleProtocolAgent {

    private val bpaLog: Logger = LoggerFactory.getLogger("BundleProtocolAgent")

    override suspend fun transmitADU(bundle: Bundle) {
        checkDuplicate(bundle) {
            BundleDescriptor(bundle).apply {
                tags.add(BundleTag.OriginLocal.code)
                core.bundleTransmission(this)
                doneProcessing(this)
            }
        }
    }

    override suspend fun receivePDU(bundle: Bundle) {
        checkDuplicate(bundle) {
            BundleDescriptor(bundle).apply {
                tags.add(BundleTag.OriginCLA.code)
                core.bundleReceive(this)
                doneProcessing(this)
            }
        }
    }

    override suspend fun resumeForwarding(desc: BundleDescriptor) =
        resumeForwarding(desc) { desc, isCancelled ->
            core.findClaForBundle(desc, isCancelled)
        }

    override suspend fun resumeForwarding(
        desc: BundleDescriptor,
        txHandler: ForwardingTxHandler
    ) {
        core.bundleForwarding(desc, txHandler)
        doneProcessing(desc)
    }

    private suspend fun doneProcessing(desc: BundleDescriptor) {
        if (desc.constraints.contains(BundleConstraint.ForwardPending.code) &&
            desc.constraints.contains(BundleConstraint.Contraindicated.code)
        ) {
            // store
            bpaLog.debug("bundle:${desc.ID()} - forward later, put in storage")
            core.store.bundleStore.insert(desc)
        } else {
            // delete
            bpaLog.debug("bundle:${desc.ID()} - forget this bundle")
            core.store.bundleStore.delete(desc.ID())
            desc.tags.add(BundleTag.Deleted.code)
        }
    }

    private suspend fun checkDuplicate(bundle: Bundle, func: suspend (Bundle) -> Unit) {
        if (!core.store.bundleStore.exists(bundle.ID())) {
            func(bundle)
        } else {
            bpLog.debug("bundle:${bundle.ID()} - duplicate bundle, ignore")
        }
    }
}