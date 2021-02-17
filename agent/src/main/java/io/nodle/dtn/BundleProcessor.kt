package io.nodle.dtn

import io.nodle.dtn.bpv7.administrative.StatusReportReason
import io.nodle.dtn.bpv7.checkValid
import io.nodle.dtn.bpv7.eid.isNullEid
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
    log.debug("5.2-1 ${desc.bundle.hashCode()}")
    if (!desc.bundle.primaryBlock.source.isNullEid() && isEndpoint(desc.bundle.primaryBlock.source)) {
        log.debug("bundle's source is neither dtn:none nor a node's endpoint")
        bundleDeletion(desc, StatusReportReason.NoInformation)
    }

    /* 5.2 - step 2 */
    desc.tag(BundleConstraint.DispatchPending.name)
    log.debug("5.2-2 ${desc.hashCode()}")
    bundleDispatching(desc)
}

suspend fun DtnAgent.bundleDispatching(desc: BundleDescriptor) {
    if(!runWithoutException { desc.bundle.checkValid() }) {
        log.debug("bundle is invalid")
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

/* 5.6 */
suspend fun DtnAgent.bundleReceive(desc: BundleDescriptor) {

    /* 5.6 - step 1 */
    log.info("receiving bundle: ${desc.bundle.hashCode()}")
    desc.tag(BundleConstraint.DispatchPending.name)

    /* 5.6 - step 2 */
    // TODO

    /* 5.6 - step 3 */
    // TODO

    log.debug("5.2-2 ${desc.bundle.hashCode()}")
    bundleProcessor(desc)
}

/* 5.6 - step 4*/
suspend fun DtnAgent.bundleProcessor(desc: BundleDescriptor) {
    log.info("receiving bundle: ${desc.bundle.hashCode()}")
    bundleDispatching(desc)
}


/* 5.7 */
suspend fun DtnAgent.localDelivery(desc: BundleDescriptor) {
    log.info("delivering bundle: ${desc.bundle.hashCode()}")

}

/* 5.4 */
suspend fun DtnAgent.bundleForwarding(desc: BundleDescriptor) {
    log.info("forwarding bundle: ${desc.bundle.hashCode()}")
    if(getRouter().findRoute(desc.bundle)?.sendBundle(desc.bundle) == true) {
        log.info("bundle is sent: ${desc.bundle.hashCode()}")
    } else {
        log.info("bundle requires storage: ${desc.bundle.hashCode()}")
    }

}

suspend fun DtnAgent.bundleDeletion(desc: BundleDescriptor, error: StatusReportReason) {
    log.info("deleting bundle: ${desc.bundle.hashCode()}")
}


fun runWithoutException(job: () -> Unit) : Boolean {
    return try {
        job()
        true
    } catch (e : Exception) {
        false
    }
}