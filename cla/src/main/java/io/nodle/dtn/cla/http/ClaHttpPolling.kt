package io.nodle.dtn.cla.http

import io.nodle.dtn.interfaces.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.URI
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

fun IBundleNode.pollHttpPeriodically(
    scope: CoroutineScope,
    claHttpClient: ClaHttpClient,
    periodSec: Int,
    maxBundlePerRequest: Int
) {
    scope.launch(CoroutineName("cla-http-polling")) {
        while (true) {
            coroutineContext.ensureActive()

            doPollHttp(scope, claHttpClient, maxBundlePerRequest)

            delay(periodSec * 1000L)
        }
    }
}

suspend fun IBundleNode.doPollHttp(
    scope: CoroutineScope,
    claHttpClient: ClaHttpClient,
    maxBundlePerRequest: Int
) {
    // get all relevant bundles for this cla
    val bundles = fetchBundlesForBulkSend(claHttpClient.peerEndpointId, maxBundlePerRequest)
    if (bundles.isEmpty()) {
        // no bundle to send, we still trigger an http query in case bundles are pending
        claHttpClient.sendBundles(listOf())
    } else {
        val bulkSender = BulkHttpBundleSender(bundles.size, claHttpClient)

        // queue all bundle to the bulkSender, the last one will trigger the transmission
        // of all bundles over a single http query
        bundles.map {
            scope.launch {
                bpa.resumeForwarding(it) { desc, cancelled ->
                    bulkSender.queue(desc, cancelled)
                }
            }
        }.joinAll()
    }
}

suspend fun IBundleNode.fetchBundlesForBulkSend(
    peerClaEid: URI,
    maxBundlePerRequest: Int
): List<BundleDescriptor> {
    val bundleClaMatcher = router.getBundleToClaMatcher(peerClaEid)
    return store.bundleStore.getNPrimaryDesc(maxBundlePerRequest) {
        bundleClaMatcher(it)
    }.map {
        store.bundleStore.get(it.ID())!!
    }
}

class BulkHttpBundleSender(
    private val expectedNumberOfBundles: Int,
    private val claHttpClient: ClaHttpClient
) {
    private var numberOfBundle = AtomicInteger(expectedNumberOfBundles)
    private val queue = Collections.synchronizedList(mutableListOf<BundleDescriptor>())
    private val waitQueue = Mutex(true)
    private var transmissionStatus: TransmissionStatus =
        TransmissionStatus.TransmissionTemporaryUnavailable

    suspend fun queue(desc: BundleDescriptor, cancelled: Boolean): TransmissionStatus {
        if (!cancelled) {
            queue.add(desc)
        }

        if(numberOfBundle.addAndGet(-1) == 0) {
            transmissionStatus = claHttpClient.sendBundles(queue.map { it.bundle })
            waitQueue.unlock()
        } else {
            waitQueue.withLock {}
        }
        return transmissionStatus
    }
}




