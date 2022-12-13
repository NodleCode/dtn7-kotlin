package io.nodle.dtn.cla.http

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.nodle.dtn.bpv7.cborMarshal
import io.nodle.dtn.bpv7.readBundle
import io.nodle.dtn.interfaces.*
import io.nodle.dtn.utils.wait
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class ClaHttpServerConfig(
    var listenPort: Int = 80,
    var httpHeader: MutableMap<String, String> = mutableMapOf(),
    var maxBundlePerResponse: Int = 5,
    var path: String = "/",
    var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
)


/**
 * @author Lucien Loiseau on 17/02/21.
 * This CLA is used to trigger an HTTP query whenever a Bundle is scheduled for a
 * specific HTTP endpoint. It is acceptable for low velocity network but may trigger a lot
 * of connection request as it is not throttled.
 */
open class ClaHttpServer(
    private val agent: IBundleNode,
    private val config: ClaHttpServerConfig = ClaHttpServerConfig()
) {
    private val log = LoggerFactory.getLogger("ClaHttpServer")

    fun start() {
        val address = InetSocketAddress(config.listenPort)
        val server = HttpServer.create(address, 0)
        server.createContext(config.path) { it ->
            // let's identify our peer
            val peerEid = try {
                URI.create(
                    queryToMap(it.requestURI.query)["peerId"] ?: throw IllegalArgumentException()
                )
            } catch (e: Exception) {
                log.debug(">> got a request from (${it.remoteAddress.address.hostAddress}) but peerId missing - 400")
                val response = "{msg: \"missing the peerId parameter\"}".toByteArray()
                it.sendResponseHeaders(400, response.size.toLong())
                it.responseBody.write(response)
                return@createContext
            }

            // TODO authentify it with JWT?
            log.debug(">> got a request from $peerEid (${it.remoteAddress.address.hostAddress})")

            // parsing the request body for bundles
            parseBundlesFromRequestBody(it.requestBody)

            // get all relevant bundles for this cla
            val bundles = agent.wait {
                fetchBundlesForBulkSend(peerEid, config.maxBundlePerResponse)
            }

            if (bundles.isEmpty()) {
                // send just one byte otherwise the ClaHttpClient seems to "hang"
                // for some unknown reason, probably expecting data and wait for a timeout
                it.sendResponseHeaders(200, 1)
                it.responseBody.write(byteArrayOf(0))
            } else {
                val bulkSender = BulkHttpBundleResponse(bundles.size, it)

                // queue all bundle to the bulkSender, the last one will trigger the transmission
                // of all bundles over a single http query
                bundles.map {
                    config.scope.launch {
                        agent.bpa.resumeForwarding(it) { desc, cancelled ->
                            bulkSender.queue(desc, cancelled)
                        }
                    }
                }
            }
        }
        server.start()
    }

    private fun parseBundlesFromRequestBody(body: InputStream) {
        try {
            val parser = CBORFactory().createParser(body)
            while (!parser.isClosed) {
                val b = parser.readBundle()
                CoroutineScope(Dispatchers.IO).launch { agent.bpa.receivePDU(b) }
            }
        } catch (_: Exception) {
            // ignore
        }
    }
}

fun queryToMap(query: String?): Map<String, String> {
    if (query == null)
        return mapOf()

    val result = mutableMapOf<String, String>()
    for (param in query.split("&".toRegex()).toTypedArray()) {
        val entry = param.split("=".toRegex()).toTypedArray()
        result[entry[0]] =
            if (entry.size > 1) URLDecoder.decode(entry[1], StandardCharsets.UTF_8) else ""
    }
    return result
}

class BulkHttpBundleResponse(
    private var numberOfBundle: Int,
    private val httpExchange: HttpExchange
) {
    private val log = LoggerFactory.getLogger("ClaHttpServer")
    private val queue = mutableListOf<BundleDescriptor>()
    private val mutex = Mutex(true)
    private var transmissionStatus: TransmissionStatus =
        TransmissionStatus.TransmissionSuccessful

    suspend fun queue(desc: BundleDescriptor, cancelled: Boolean): TransmissionStatus {
        if (!cancelled) {
            queue.add(desc)
        }
        numberOfBundle--
        return checkSend()
    }

    private suspend fun checkSend(): TransmissionStatus {
        if (numberOfBundle == 0) {
            val responsePayload = queue.fold(byteArrayOf()) { acc, elem ->
                acc + elem.bundle.cborMarshal()
            }

            try {
                httpExchange.sendResponseHeaders(200, responsePayload.size.toLong())
                httpExchange.responseBody.write(responsePayload)
            } catch (e: Exception) {
                log.debug(">> http response failed with error: ${e.message}")
                transmissionStatus = TransmissionStatus.TransmissionFailed
            }
            mutex.unlock()
        } else {
            mutex.withLock {}
        }
        return transmissionStatus
    }
}




