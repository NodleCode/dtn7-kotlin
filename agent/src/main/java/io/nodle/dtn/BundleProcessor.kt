package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.administrative.StatusAssertion
import io.nodle.dtn.bpv7.administrative.StatusReportReason
import io.nodle.dtn.bpv7.eid.isNullEid
import io.nodle.dtn.bpv7.extensions.*
import io.nodle.dtn.interfaces.BundleConstraint
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.interfaces.ID
import org.slf4j.LoggerFactory

/**
 * implementation of the bundle protocol agent
 * https://tools.ietf.org/html/draft-ietf-dtn-bpbis-31
 *
 * @author Lucien Loiseau on 15/02/21.
 */

val log = LoggerFactory.getLogger("BundleProcessor")

suspend fun BundleProtocolAgent.bundleTransmission(desc: BundleDescriptor) {
    /* 5.2 - step 1 */
    log.debug("bundle: ${desc.ID()} - bundle transmission")
    if (!desc.bundle.primaryBlock.source.isNullEid() && !isEndpoint(desc.bundle.primaryBlock.source)) {
        log.debug("bundle:${desc.ID()} - bundle's source is neither dtn:none nor a node's endpoint")
        bundleDeletion(desc, StatusReportReason.NoInformation)
        return
    }

    /* 5.2 - step 2 */
    desc.addConstraint(BundleConstraint.DispatchPending)
    bundleDispatching(desc)
}

/* 5.6 */
suspend fun BundleProtocolAgent.bundleReceive(desc: BundleDescriptor) {
    log.debug("bundle:${desc.ID()} - bundle receive")

    if (desc.constraints.isNotEmpty()) {
        log.debug("bundle:${desc.ID()} - received bundle is already known")
        return
    }

    /* 5.6 - step 1 */
    desc.addConstraint(BundleConstraint.DispatchPending.name)

    /* 5.6 - step 2 */
    if (desc.bundle.isFlagSet(BundleV7Flags.StatusRequestReception)) {
        log.debug("bundle:${desc.ID()} - request reporting on reception")
        getAdministrativeAgent().sendStatusReport(this, desc, StatusAssertion.ReceivedBundle, StatusReportReason.NoInformation)
    }

    /* 5.6 - step 3 */
    // crc checked during deserialization

    /* 5.6 - step 4 */
    log.debug("bundle:${desc.ID()} - processing bundle")
    val iterator = desc.bundle.canonicalBlocks.iterator()
    for (block in iterator) {
        if (bpv7ExtensionManager.isKnown(block.blockType)) {
            continue
        }

        log.debug("bundle:${desc.ID()}, " +
                "block: number=${block.blockNumber}, " +
                "type=${block.blockType}  - block is unknown ")
        if (block.isFlagSet(BlockV7Flags.StatusReportIfNotProcessed)) {
            log.debug("bundle:${desc.ID()}, " +
                    "block: number=${block.blockNumber}, " +
                    "type=${block.blockType}  - unprocessed block requested reporting ")
            getAdministrativeAgent().sendStatusReport(this, desc, StatusAssertion.ReceivedBundle, StatusReportReason.BlockUnsupported)
        }

        if (block.isFlagSet(BlockV7Flags.DeleteBundleIfNotProcessed)) {
            log.debug("bundle:${desc.ID()}, " +
                    "block: number=${block.blockNumber}, " +
                    "type=${block.blockType}  - unprocessed block requested bundle deletion")
            bundleDeletion(desc, StatusReportReason.BlockUnsupported)
        }

        if (block.isFlagSet(BlockV7Flags.DiscardIfNotProcessed)) {
            log.debug("bundle:${desc.ID()}, " +
                    "block: number=${block.blockNumber}, " +
                    "type=${block.blockType}  - unprocessed block requested removal from bundle")
            // TODO need to unit test this
            iterator.remove()
        }
    }

    bundleDispatching(desc)
}


suspend fun BundleProtocolAgent.bundleDispatching(desc: BundleDescriptor) {
    log.debug("bundle:${desc.ID()} - dispatching bundle")

    val e = catchException { desc.bundle.checkValid() }
    if (e != null) {
        log.debug("${e.message}")
        return
    }

    if (isEndpoint(desc.bundle.primaryBlock.destination)) {
        /* 5.3 - step 1 */
        localDelivery(desc)
    } else {
        /* 5.3 - step 2 */
        bundleForwarding(desc)
    }
}

/* 5.7 */
suspend fun BundleProtocolAgent.localDelivery(desc: BundleDescriptor) {
    log.debug("bundle:${desc.ID()} - local delivery")
    desc.addConstraint(BundleConstraint.LocalEndpoint)

    if (getRegistrar().localDelivery(desc.bundle)?.deliver(desc.bundle) == true) {
        log.debug("bundle:${desc.ID()} - bundle is delivered")
        if (desc.bundle.isFlagSet(BundleV7Flags.StatusRequestDelivery)) {
            getAdministrativeAgent().sendStatusReport(this, desc, StatusAssertion.DeliveredBundle, StatusReportReason.NoInformation)
        }
    } else {
        log.debug("bundle:${desc.ID()} - delivery unsuccessful")
    }

    // delete the bundle whether it was delivered or not
    desc.purgeConstraints()
}

/* 5.4 */
suspend fun BundleProtocolAgent.bundleForwarding(desc: BundleDescriptor) {
    log.debug("bundle:${desc.ID()} - bundle forwarding")
    desc.addConstraint(BundleConstraint.ForwardPending)
    desc.removeConstraint(BundleConstraint.DispatchPending)

    if (desc.bundle.hasBlockType(BlockType.HopCountBlock)) {
        val hc = desc.bundle.getHopCountBlockData()
        hc.inc()
        log.debug("bundle:${desc.ID()} - contain hop count block")

        if (hc.isExceeded()) {
            log.debug("bundle:${desc.ID()} - hop count exceeded")
            return
        }
    }

    if (desc.bundle.hasBlockType(BlockType.PreviousNodeBlock)) {
        desc.bundle.getPreviousNodeBlockData().replaceWith(nodeId())
        log.debug("bundle:${desc.ID()} - previous node block updated")
    } else {
        desc.bundle.addBlock(previousNodeBlock(nodeId()))
    }

    var bundleSent = false
    if (getRouter().findRoute(desc.bundle)?.sendBundle(desc.bundle) == true) {
        log.debug("bundle:${desc.ID()} - forwarding succeeded")
        bundleSent = true
    } else {
        log.debug("bundle:${desc.ID()} - forwarding failed")
    }

    if (desc.bundle.hasBlockType(BlockType.HopCountBlock)) {
        desc.bundle.getHopCountBlockData().dec()
        log.debug("bundle:${desc.ID()} - hop count reseted")
    }

    if (bundleSent) {
        if (desc.bundle.isFlagSet(BundleV7Flags.StatusRequestForward)) {
            getAdministrativeAgent().sendStatusReport(this, desc, StatusAssertion.ForwardedBundle, StatusReportReason.NoInformation)
        }
    } else {
        bundleContraindicated(desc)
    }
}

suspend fun BundleProtocolAgent.bundleContraindicated(desc: BundleDescriptor) {
    log.debug("bundle:${desc.ID()} - bundle marked for contraindication")
    desc.addConstraint(BundleConstraint.Contraindicated)
}

suspend fun BundleProtocolAgent.bundleDeletion(desc: BundleDescriptor, reason: StatusReportReason) {
    log.debug("bundle:${desc.ID()} - bundle deletion")

    if (desc.bundle.isFlagSet(BundleV7Flags.StatusRequestDeletion)) {
        getAdministrativeAgent().sendStatusReport(this, desc, StatusAssertion.DeletedBundle, reason)
    }

    desc.purgeConstraints()
}


fun catchException(job: () -> Unit): Exception? {
    return try {
        job()
        null
    } catch (e: Exception) {
        e
    }
}