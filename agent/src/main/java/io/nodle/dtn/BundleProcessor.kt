package io.nodle.dtn

import io.nodle.dtn.bpv7.BundleV7Flags
import io.nodle.dtn.bpv7.administrative.StatusReport
import io.nodle.dtn.bpv7.administrative.StatusReportReason
import io.nodle.dtn.bpv7.checkValid
import io.nodle.dtn.bpv7.eid.isNullEid
import io.nodle.dtn.interfaces.BundleConstraint
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.interfaces.ID
import io.nodle.dtn.utils.isFlagSet
import org.slf4j.LoggerFactory
import java.lang.Exception

/**
 * implementation of the bundle protocol agent
 * https://tools.ietf.org/html/draft-ietf-dtn-bpbis-31
 *
 * @author Lucien Loiseau on 15/02/21.
 */

val log = LoggerFactory.getLogger("BundleProcessor")

suspend fun DtnAgent.bundleTransmission(desc: BundleDescriptor) {
    /* 5.2 - step 1 */
    log.debug("5.2-1 ${desc.ID()}")
    if (!desc.bundle.primaryBlock.source.isNullEid() && isEndpoint(desc.bundle.primaryBlock.source)) {
        log.debug("bundle:${desc.ID()} - bundle's source is neither dtn:none nor a node's endpoint")
        bundleDeletion(desc, StatusReportReason.NoInformation)
        return
    }

    /* 5.2 - step 2 */
    desc.addConstraint(BundleConstraint.DispatchPending.name)
    bundleDispatching(desc)
}

/* 5.6 */
suspend fun DtnAgent.bundleReceive(desc: BundleDescriptor) {
    log.info("bundle:${desc.ID()} - bundle receive")

    if(!desc.constraints.isEmpty()) {
        log.info("bundle:${desc.ID()} - received bundle is already known")
    }
    /* 5.6 - step 1 */
    desc.addConstraint(BundleConstraint.DispatchPending.name)

    /* 5.6 - step 2 */
    if(desc.bundle.primaryBlock.procV7Flags.isFlagSet(BundleV7Flags.StatusRequestReception.offset)) {
         //SendStatusReport(descriptor BundleDescriptor, status bpv7.StatusInformationPos, reason bpv7.StatusReportReason) {
    }

    /* 5.6 - step 3 */
    // TODO

    bundleProcessor(desc)
}


suspend fun DtnAgent.bundleDispatching(desc: BundleDescriptor) {
    if(!runWithoutException { desc.bundle.checkValid() }) {
        log.debug("bundle:${desc.ID()} - bundle is invalid")
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


/* 5.6 - step 4*/
suspend fun DtnAgent.bundleProcessor(desc: BundleDescriptor) {
    log.info("bundle:${desc.ID()} - processing bundle")
    bundleDispatching(desc)
}


/* 5.7 */
suspend fun DtnAgent.localDelivery(desc: BundleDescriptor) {
    log.info("bundle:${desc.ID()} - local delivery")
    desc.tags

    if(getRegistrar().localDelivery(desc.bundle)?.deliver(desc.bundle) == true) {
        log.info("bundle:${desc.ID()} - bundle is delivered")
    } else {
        log.info("bundle:${desc.ID()} - delivery unsuccessful")
    }
}

/* 5.4 */
suspend fun DtnAgent.bundleForwarding(desc: BundleDescriptor) {
    log.info("bundle:${desc.ID()} - bundle forwarding")
    if(getRouter().findRoute(desc.bundle)?.sendBundle(desc.bundle) == true) {
        log.info("bundle:${desc.ID()} - bundle is forwarded")
    } else {
        log.info("bundle:${desc.ID()} - forwarding failed")
    }

}

suspend fun DtnAgent.bundleDeletion(desc: BundleDescriptor, error: StatusReportReason) {
    log.info("bundle:${desc.ID()} - bundle deletion")
}


fun runWithoutException(job: () -> Unit) : Boolean {
    return try {
        job()
        true
    } catch (e : Exception) {
        false
    }
}