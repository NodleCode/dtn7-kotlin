package io.nodle.dtn.cla.http

import io.nodle.dtn.interfaces.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.URI

fun IBundleNode.doPeriodicHttpPolling(
    scope: CoroutineScope,
    claHttpClient: ClaHttpClient,
    periodSec: Int,
    maxBundlePerRequest: Int
) {
    scope.launch(CoroutineName("cla-http-polling")) {
        while (true) {
            coroutineContext.ensureActive()

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
                    launch {
                        bpa.resumeForwarding(it) { desc, cancelled ->
                            bulkSender.queue(desc, cancelled)
                        }
                    }
                }.joinAll()
            }

            delay(periodSec * 1000L)
        }
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
    private var numberOfBundle: Int,
    private val claHttpClient: ClaHttpClient
) {
    private val queue = mutableListOf<BundleDescriptor>()
    private val mutex = Mutex(true)
    private var transmissionStatus: TransmissionStatus = TransmissionStatus.TransmissionTemporaryUnavailable

    suspend fun queue(desc: BundleDescriptor, cancelled: Boolean): TransmissionStatus {
        if (!cancelled) {
            queue.add(desc)
        }
        numberOfBundle--
        return checkSend()
    }

    private suspend fun checkSend(): TransmissionStatus {
        if (numberOfBundle == 0) {
            transmissionStatus = claHttpClient.sendBundles(queue.map { it.bundle })
            mutex.unlock()
        } else {
            mutex.withLock {}
        }
        return transmissionStatus
    }
}




