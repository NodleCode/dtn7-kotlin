package io.nodle.dtn.cla.http

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.cborMarshal
import io.nodle.dtn.bpv7.readBundle
import io.nodle.dtn.interfaces.*
import io.nodle.dtn.utils.addQueryParameter
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

data class ClaHttpClientConfig(
    var triggerQueryOnTx: Boolean = false,
    var httpHeader: MutableMap<String, String> = mutableMapOf(),
)


/**
 * @author Lucien Loiseau on 17/02/21.
 * This CLA is used to trigger an HTTP query whenever a Bundle is scheduled for a
 * specific HTTP endpoint. It is acceptable for low velocity network but may trigger a lot
 * of connection request as it is not throttled.
 */
open class ClaHttpClient(
    private val agent: IBundleNode,
    private val httpEndpoint: URI,
    override val peerEndpointId: URI,
    private val config: ClaHttpClientConfig = ClaHttpClientConfig()
) : IConvergenceLayerSender {

    private val log = LoggerFactory.getLogger("ClaHttpClient")

    override val scheduleForTransmission: ClaTxHandler = {
        if (config.triggerQueryOnTx) sendBundle(it)
        else TransmissionStatus.TransmissionTemporaryUnavailable
    }

    suspend fun sendBundle(bundle: Bundle): TransmissionStatus = sendBundles(listOf(bundle))

    suspend fun sendBundles(bundles: List<Bundle>): TransmissionStatus {
        log.debug(
            (if(bundles.isNotEmpty()) ">> trying to upload ${bundles.size} bundles to "
            else ">> polling ") + "$peerEndpointId"
        )

        val url = httpEndpoint
            .addQueryParameter("peerId", agent.applicationAgent.nodeId().toASCIIString())
            .toURL()

        return (url.openConnection() as HttpURLConnection).let { connection ->
            try {
                connection.requestMethod = "POST"
                connection.doInput = true
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                config.httpHeader.map {
                    connection.setRequestProperty(it.key, it.value)
                }
                connection.setRequestProperty("Content-Type", "application/octet-stream")
                connection.instanceFollowRedirects = true
                connection.connect();
                val out = connection.outputStream

                // send bundle
                bundles.map {
                    it.cborMarshal(out)
                }

                // return response code
                when (connection.responseCode) {
                    HttpURLConnection.HTTP_ACCEPTED, HttpURLConnection.HTTP_OK -> {
                        // update metrics db and parse receiving bundle
                        parseResponse(connection.inputStream)
                        TransmissionStatus.TransmissionSuccessful
                    }
                    HttpURLConnection.HTTP_BAD_REQUEST, HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        // acknowledge the bundle to drop it
                        TransmissionStatus.TransmissionRejected
                    }
                    else -> {
                        TransmissionStatus.TransmissionTemporaryUnavailable
                    }
                }
            } catch (e: Exception) {
                log.debug("error connecting to the endpoint: ${e.message}")
                return@let TransmissionStatus.TransmissionTemporaryUnavailable
            } finally {
                connection.disconnect()
            }
        }
    }

    private suspend fun parseResponse(inputStream: InputStream) {
        try {
            val parser = CBORFactory().createParser(inputStream)
            while (!parser.isClosed) {
                agent.bpa.receivePDU(parser.readBundle())
            }
        } catch (e: Exception) {
            //log.debug("could not parse the response bundle: ${e.message}")
            //ignore
        }
    }
}
