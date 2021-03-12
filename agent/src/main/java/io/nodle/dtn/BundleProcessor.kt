package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.administrative.StatusAssertion
import io.nodle.dtn.bpv7.administrative.StatusReportReason
import io.nodle.dtn.bpv7.eid.isNullEid
import io.nodle.dtn.bpv7.extensions.*
import io.nodle.dtn.interfaces.*
import org.slf4j.LoggerFactory

/**
 * implementation of the bundle protocol agent
 * https://tools.ietf.org/html/draft-ietf-dtn-bpbis-31
 *
 * @author Lucien Loiseau on 15/02/21.
 */

val bpLog = LoggerFactory.getLogger("BundleProcessor")

suspend fun BundleProtocolAgent.bundleTransmission(desc: BundleDescriptor) {
    /* 5.2 - step 1 */
    bpLog.debug("bundle:${desc.ID()} - bundle transmission ${desc.bundle.primaryBlock.source.toASCIIString()} -> ${desc.bundle.primaryBlock.destination.toASCIIString()}")
    if (!desc.bundle.primaryBlock.source.isNullEid() && !isLocal(desc.bundle.primaryBlock.source)) {
        bpLog.debug("bundle:${desc.ID()} - bundle's source is neither dtn:none nor a node's endpoint")
        bundleDeletion(desc, StatusReportReason.NoInformation)
        return
    }

    /* 5.2 - step 2 */
    desc.constraints.add(BundleConstraint.DispatchPending.code)
    bundleDispatching(desc)
}

/* 5.6 */
suspend fun BundleProtocolAgent.bundleReceive(desc: BundleDescriptor) {
    bpLog.debug("bundle:${desc.ID()} - bundle receive ${desc.bundle.primaryBlock.source.toASCIIString()} -> ${desc.bundle.primaryBlock.destination.toASCIIString()}")

    if (desc.constraints.isNotEmpty()) {
        bpLog.debug("bundle:${desc.ID()} - received bundle is already known")
        return
    }

    /* 5.6 - step 1 */
    desc.constraints.add(BundleConstraint.DispatchPending.code)

    /* 5.6 - step 2 */
    if (desc.bundle.isFlagSet(BundleV7Flags.StatusRequestReception)) {
        bpLog.debug("bundle:${desc.ID()} - request reporting on reception")
        getAdministrativeAgent().sendStatusReport(this, desc, StatusAssertion.ReceivedBundle, StatusReportReason.NoInformation)
    }

    /* 5.6 - step 3 */
    // crc checked during deserialization

    /* 5.6 - step 4 */
    bpLog.debug("bundle:${desc.ID()} - processing bundle")
    val iterator = desc.bundle.canonicalBlocks.iterator()
    for (block in iterator) {
        if (bpv7ExtensionManager.isKnown(block.blockType)) {
            continue
        }

        bpLog.debug("bundle:${desc.ID()}, " +
                "block: number=${block.blockNumber}, " +
                "type=${block.blockType}  - block is unknown ")
        if (block.isFlagSet(BlockV7Flags.StatusReportIfNotProcessed)) {
            bpLog.debug("bundle:${desc.ID()}, " +
                    "block: number=${block.blockNumber}, " +
                    "type=${block.blockType}  - unprocessed block requested reporting ")
            getAdministrativeAgent().sendStatusReport(this, desc, StatusAssertion.ReceivedBundle, StatusReportReason.BlockUnsupported)
        }

        if (block.isFlagSet(BlockV7Flags.DiscardIfNotProcessed)) {
            bpLog.debug("bundle:${desc.ID()}, " +
                    "block: number=${block.blockNumber}, " +
                    "type=${block.blockType}  - unprocessed block requested removal from bundle")
            // TODO need to unit test this
            iterator.remove()
        }

        if (block.isFlagSet(BlockV7Flags.DeleteBundleIfNotProcessed)) {
            bpLog.debug("bundle:${desc.ID()}, " +
                    "block: number=${block.blockNumber}, " +
                    "type=${block.blockType}  - unprocessed block requested bundle deletion")
            bundleDeletion(desc, StatusReportReason.BlockUnsupported)
            return
        }
    }

    bundleDispatching(desc)
}


suspend fun BundleProtocolAgent.bundleDispatching(desc: BundleDescriptor) {
    bpLog.debug("bundle:${desc.ID()} - dispatching bundle")

    if(!desc.bundle.isValid()) {
        bpLog.debug("bundle:${desc.ID()} - is invalid!")
        desc.constraints.clear()
        return
    }

    if (isLocal(desc.bundle.primaryBlock.destination)) {
        /* 5.3 - step 1 */
        localDelivery(desc)
    } else {
        /* 5.3 - step 2 */
        bundleForwarding(desc)
    }
}

/* 5.7 */
suspend fun BundleProtocolAgent.localDelivery(desc: BundleDescriptor) {
    bpLog.debug("bundle:${desc.ID()} - local delivery")
    desc.constraints.add(BundleConstraint.LocalEndpoint.code)

    if (getRegistrar().localDelivery(desc.bundle)?.deliver(desc.bundle) == true) {
        bpLog.debug("bundle:${desc.ID()} - bundle is delivered")
        desc.tags.add(BundleTag.Delivered.code)
        if (desc.bundle.isFlagSet(BundleV7Flags.StatusRequestDelivery)) {
            getAdministrativeAgent().sendStatusReport(this, desc, StatusAssertion.DeliveredBundle, StatusReportReason.NoInformation)
        }
    } else {
        bpLog.debug("bundle:${desc.ID()} - delivery unsuccessful")
    }

    // delete the bundle whether it was delivered or not
    desc.constraints.clear()
}

/* 5.4 */
suspend fun BundleProtocolAgent.bundleForwarding(desc: BundleDescriptor) {
    bpLog.debug("bundle:${desc.ID()} - bundle forwarding")
    desc.constraints.add(BundleConstraint.ForwardPending.code)
    desc.constraints.remove(BundleConstraint.DispatchPending.code)

    // check and increase hop count
    if (desc.bundle.hasBlockType(BlockType.HopCountBlock)) {
        val hc = desc.bundle.getHopCountBlockData()
        hc.inc()
        bpLog.debug("bundle:${desc.ID()} - contain hop count block")

        if (hc.isExceeded()) {
            bpLog.debug("bundle:${desc.ID()} - hop count exceeded")
            return
        }
    }

    // update age block, if any and check lifetime
    desc.updateAgeBlock()
    if (System.currentTimeMillis() > desc.expireAt()) {
        bpLog.debug("bundle:${desc.ID()} - is expired")
        bundleDeletion(desc, StatusReportReason.LifetimeExpired)
        return
    }

    if (desc.bundle.hasBlockType(BlockType.PreviousNodeBlock)) {
        desc.bundle.getPreviousNodeBlockData().replaceWith(nodeId())
        bpLog.debug("bundle:${desc.ID()} - previous node block updated")
    } else {
        desc.bundle.addBlock(previousNodeBlock(nodeId()))
    }

    if (getRouter().findRoute(desc.bundle)?.sendBundle(desc.bundle) == true) {
        bpLog.debug("bundle:${desc.ID()} - forwarding succeeded")
        if (desc.bundle.isFlagSet(BundleV7Flags.StatusRequestForward)) {
            getAdministrativeAgent().sendStatusReport(this, desc, StatusAssertion.ForwardedBundle, StatusReportReason.NoInformation)
        }

        // deleter afterward
        desc.tags.add(BundleTag.Forwarded.code)
        desc.constraints.clear()
    } else {
        bpLog.debug("bundle:${desc.ID()} - forwarding failed")
        bundleContraindicated(desc)
    }
}

suspend fun BundleProtocolAgent.bundleContraindicated(desc: BundleDescriptor) {
    bpLog.debug("bundle:${desc.ID()} - bundle marked for contraindication")
    desc.constraints.add(BundleConstraint.Contraindicated.code)
}

suspend fun BundleProtocolAgent.bundleDeletion(desc: BundleDescriptor, reason: StatusReportReason) {
    bpLog.debug("bundle:${desc.ID()} - bundle deletion")

    if (desc.bundle.isFlagSet(BundleV7Flags.StatusRequestDeletion)) {
        getAdministrativeAgent().sendStatusReport(this, desc, StatusAssertion.DeletedBundle, reason)
    }

    desc.constraints.clear()
}