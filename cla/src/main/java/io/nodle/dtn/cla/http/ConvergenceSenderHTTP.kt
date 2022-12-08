package io.nodle.dtn.cla.http

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.cborMarshal
import io.nodle.dtn.bpv7.readBundle
import io.nodle.dtn.interfaces.ClaTxHandler
import io.nodle.dtn.interfaces.IBundleNode
import io.nodle.dtn.interfaces.IConvergenceLayerSender
import io.nodle.dtn.interfaces.TransmissionStatus
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/**
 * @author Lucien Loiseau on 17/02/21.
 */
open class ConvergenceSenderHTTP(
    private val agent: IBundleNode,
    override val peerEndpointId: URI
) : IConvergenceLayerSender {

    private val log = LoggerFactory.getLogger("ConvergenceSenderHTTP")

    override val scheduleForTransmission: ClaTxHandler = { sendBundle(it) }

    suspend fun sendBundle(bundle: Bundle): TransmissionStatus = sendBundles(listOf(bundle))

    suspend fun sendBundles(bundles: List<Bundle>): TransmissionStatus {
        log.debug(">> trying to upload bundles "
                + "${bundles.joinToString(",") { it.primaryBlock.destination.toASCIIString() }} "
                + "to $peerEndpointId")

        val url = URL(peerEndpointId.toString())
        return (url.openConnection() as HttpURLConnection).let { connection ->
            try {
                connection.requestMethod = "POST"
                connection.doInput = true
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.setRequestProperty("Content-Type", "application/octet-stream")
                connection.instanceFollowRedirects = true
                connection.connect();
                val out = connection.outputStream

                // send bundle
                bundles.map {
                    it.cborMarshal(out)
                }

                // return response code
                when(connection.responseCode) {
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
                        TransmissionStatus.TransmissionRejected
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
            log.debug("could not parse the response bundle: ${e.message}")
            //ignore
        }
    }
}
