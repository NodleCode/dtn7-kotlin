package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.administrative.StatusAssertion
import io.nodle.dtn.bpv7.administrative.StatusReportReason
import io.nodle.dtn.bpv7.eid.isNullEid
import io.nodle.dtn.bpv7.extensions.*
import io.nodle.dtn.interfaces.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * implementation of the bundle protocol agent
 * https://tools.ietf.org/html/draft-ietf-dtn-bpbis-31
 *
 * @author Lucien Loiseau on 15/02/21.
 */

val bpLog: Logger = LoggerFactory.getLogger("BundleProcessor")

/* 5.2 */
suspend fun IBundleNode.bundleTransmission(desc: BundleDescriptor) {
    bpLog.debug(
        "bundle:${desc.ID()} " +
                "- bundle transmission ${desc.bundle.primaryBlock.source.toASCIIString()} " +
                "-> ${desc.bundle.primaryBlock.destination.toASCIIString()}"
    )

    /* 5.2 - step 1 */
    if (!desc.bundle.primaryBlock.source.isNullEid() && !applicationAgent.isLocal(desc.bundle.primaryBlock.source)) {
        bpLog.debug(
            "bundle:${desc.ID()} - bundle's source is neither dtn:none nor a node's endpoint"
        )
        bundleDeletion(desc, StatusReportReason.NoInformation)
        return
    }

    /* 5.2 - step 2 */
    desc.constraints.add(BundleConstraint.DispatchPending.code)
    bundleDispatching(desc)
}

/* 5.6 */
suspend fun IBundleNode.bundleReceive(desc: BundleDescriptor) {
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
        applicationAgent.administrativeAgent.sendStatusReport(
            this.bundleProtocolAgent,
            desc,
            StatusAssertion.ReceivedBundle,
            StatusReportReason.NoInformation
        )
    }

    /* 5.6 - step 3 */
    // crc checked during deserialization

    /* 5.6 - step 4 */
    bpLog.debug("bundle:${desc.ID()} - processing bundle")
    val iterator = desc.bundle.canonicalBlocks.iterator()
    for (block in iterator) {
        if (isBpv7BlockExtensionKnown(block.blockType)) {
            continue
        }

        bpLog.debug(
            "bundle:${desc.ID()}, " +
                    "block: number=${block.blockNumber}, " +
                    "type=${block.blockType}  - block is unknown "
        )
        if (block.isFlagSet(BlockV7Flags.StatusReportIfNotProcessed)) {
            bpLog.debug(
                "bundle:${desc.ID()}, " +
                        "block: number=${block.blockNumber}, " +
                        "type=${block.blockType}  - unprocessed block requested reporting "
            )
            applicationAgent.administrativeAgent.sendStatusReport(
                this.bundleProtocolAgent,
                desc,
                StatusAssertion.ReceivedBundle,
                StatusReportReason.BlockUnsupported
            )
        }

        if (block.isFlagSet(BlockV7Flags.DiscardIfNotProcessed)) {
            bpLog.debug(
                "bundle:${desc.ID()}, " +
                        "block: number=${block.blockNumber}, " +
                        "type=${block.blockType}  - unprocessed block requested removal from bundle"
            )
            // TODO need to unit test this
            iterator.remove()
        }

        if (block.isFlagSet(BlockV7Flags.DeleteBundleIfNotProcessed)) {
            bpLog.debug(
                "bundle:${desc.ID()}, " +
                        "block: number=${block.blockNumber}, " +
                        "type=${block.blockType}  - unprocessed block requested bundle deletion"
            )
            bundleDeletion(desc, StatusReportReason.BlockUnsupported)
            return
        }
    }

    bundleDispatching(desc)
}

/* 5.3 */
suspend fun IBundleNode.bundleDispatching(desc: BundleDescriptor) {
    bpLog.debug("bundle:${desc.ID()} - dispatching bundle")

    try {
        desc.bundle.checkValid()
    } catch (e: ValidationException) {
        bpLog.debug("bundle:${desc.ID()} - is invalid! ${e.status}")
        bundleDeletion(desc, e.status)
        return
    }

    if (applicationAgent.isLocal(desc.bundle.primaryBlock.destination)) {
        /* 5.3 - step 1 */
        localDelivery(desc)
    } else {
        /* 5.3 - step 2 */
        bundleForwarding(desc)
    }
}

/* 5.7 */
suspend fun IBundleNode.localDelivery(desc: BundleDescriptor) {
    bpLog.debug("bundle:${desc.ID()} - local delivery")

    /* step 1 TODO reassembly */
    if (desc.bundle.primaryBlock.isFragment()) {
        desc.constraints.add(BundleConstraint.ReassemblyPending.code)
        aduReassembly(desc)?.let{
            localDelivery(it)
        }
        return
    }

    /* step 2 */
    when (applicationAgent.receiveForLocalDelivery(desc.bundle)) {
        DeliveryStatus.DeliverySuccessful -> {
            bpLog.debug("bundle:${desc.ID()} - bundle is delivered")
            desc.tags.add(BundleTag.Delivered.code)

            /* step 3 */
            if (desc.bundle.isFlagSet(BundleV7Flags.StatusRequestDelivery)) {
                applicationAgent.administrativeAgent.sendStatusReport(
                    this.bundleProtocolAgent,
                    desc,
                    StatusAssertion.DeliveredBundle,
                    StatusReportReason.NoInformation
                )
            }
        }

        DeliveryStatus.DeliveryTemporaryUnavailable -> {
            bpLog.debug("bundle:${desc.ID()} - bundle cannot be delivered right now")
            /* defer */
        }

        DeliveryStatus.EndpointNotFound, DeliveryStatus.DeliveryRejected -> {
            bpLog.debug("bundle:${desc.ID()} - delivery unsuccessful")
            desc.constraints.clear()
        }
    }
}

/* 5.9 */
suspend fun IBundleNode.aduReassembly(desc: BundleDescriptor): BundleDescriptor? {
    val fid = desc.fragmentedID()
    bundleStorage.insert(desc)

    if (bundleStorage.isBundleWhole(fid)) {
        bpLog.debug("fragments:${desc.fragmentedID()} - all fragments have been recovered")
        return bundleStorage.getBundleFromFragments(fid)?.apply {
            bpLog.debug("bundle:${desc.ID()} - bundle has been reassembled")
            desc.constraints.clear()
            bundleStorage.deleteAllFragments(fid)
        }
    } else {
        bpLog.debug("fragments:${desc.fragmentedID()} - fragments missing")
    }
    return null
}

/* 5.4 */
suspend fun IBundleNode.bundleForwarding(desc: BundleDescriptor) {
    bpLog.debug("bundle:${desc.ID()} - bundle forwarding")

    /* step 1 */
    desc.constraints.add(BundleConstraint.ForwardPending.code)
    desc.constraints.remove(BundleConstraint.DispatchPending.code)

    /* step 2 and step 3 managed in dispatching at checkValid() */

    /* step 4 */
    // replace previous node block, or insert if none
    desc.bundle.getPreviousNodeBlockData()
        ?.apply { bpLog.debug("bundle:${desc.ID()} - updating previous node block") }
        ?.replaceWith(applicationAgent.nodeId())
        ?: desc.bundle.addBlock(previousNodeBlock(applicationAgent.nodeId()))

    // check and increase hop count, if any
    desc.bundle.getHopCountBlockData()
        ?.apply { bpLog.debug("bundle:${desc.ID()} - increase hop count block") }
        ?.inc()
        ?.apply {
            if (isExceeded()) {
                bpLog.debug("bundle:${desc.ID()} - hop count exceed limit")
                bundleDeletion(desc, StatusReportReason.HopLimitExceeded)
                return@bundleForwarding
            }
        }

    // update age block, if any
    desc.bundle.getAgeBlockData()
        ?.apply { bpLog.debug("bundle:${desc.ID()} - update age block") }
        ?.apply {
            age += (dtnTimeNow() - desc.created)
        }

    if (desc.bundle.isExpired()) {
        bpLog.debug("bundle:${desc.ID()} - is expired")
        bundleDeletion(desc, StatusReportReason.LifetimeExpired)
        return
    }

    /* step 5 */
    if (router.findRoute(desc.bundle)?.sendBundle(desc.bundle) == true) {
        bpLog.debug("bundle:${desc.ID()} - forwarding succeeded")
        if (desc.bundle.isFlagSet(BundleV7Flags.StatusRequestForward)) {
            applicationAgent.administrativeAgent.sendStatusReport(
                this.bundleProtocolAgent,
                desc,
                StatusAssertion.ForwardedBundle,
                StatusReportReason.NoInformation
            )
        }

        // delete afterward
        desc.tags.add(BundleTag.Forwarded.code)
        desc.constraints.clear()
    } else {
        bpLog.debug("bundle:${desc.ID()} - forwarding failed")
        bundleContraindicated(desc)
    }
}

suspend fun IBundleNode.bundleContraindicated(desc: BundleDescriptor) {
    bpLog.debug("bundle:${desc.ID()} - bundle marked for contraindication")
    desc.constraints.add(BundleConstraint.Contraindicated.code)
}

/* 5.10 */
suspend fun IBundleNode.bundleDeletion(desc: BundleDescriptor, reason: StatusReportReason) {
    bpLog.debug("bundle:${desc.ID()} - bundle deletion")

    /* step 1 */
    if (desc.bundle.isFlagSet(BundleV7Flags.StatusRequestDeletion)) {
        applicationAgent.administrativeAgent.sendStatusReport(
            this.bundleProtocolAgent,
            desc,
            StatusAssertion.DeletedBundle,
            reason
        )
    }

    /* step 2 */
    desc.constraints.clear()
}