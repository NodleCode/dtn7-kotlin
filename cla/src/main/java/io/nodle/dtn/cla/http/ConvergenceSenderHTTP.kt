package io.nodle.dtn.cla.http

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.cborMarshal
import io.nodle.dtn.bpv7.readBundle
import io.nodle.dtn.interfaces.IBundleNode
import io.nodle.dtn.interfaces.IConvergenceLayerSender
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/**
 * @author Lucien Loiseau on 17/02/21.
 */
open class ConvergenceSenderHTTP(
    val agent: IBundleNode,
    var url: URI
) : IConvergenceLayerSender {

    private val log = LoggerFactory.getLogger("ConvergenceSenderHTTP")

    override fun getPeerEndpointId(): URI {
        return url
    }

    override suspend fun sendBundle(bundle: Bundle): Boolean = sendBundles(listOf(bundle))

    override suspend fun sendBundles(bundles: List<Bundle>): Boolean {
        log.debug(">> trying to upload bundles ${bundles.joinToString(",") { it.primaryBlock.destination.toASCIIString() }} to $url")
        val url = URL(url.toString())
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
                if (connection.responseCode == HttpURLConnection.HTTP_ACCEPTED ||
                    connection.responseCode == HttpURLConnection.HTTP_OK
                ) {
                    // response may contain multiple bundle
                    parseResponse(connection.inputStream)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                log.debug("error connecting to the endpoint: ${e.message}")
                return@let false
            } finally {
                connection.disconnect()
            }
        }
    }

    private suspend fun parseResponse(inputStream: InputStream) {
        try {
            val parser = CBORFactory().createParser(inputStream)
            while (!parser.isClosed) {
                agent.bundleProtocolAgent.receivePDU(parser.readBundle())
            }
        } catch (e: Exception) {
            log.debug("could not parse the response bundle: ${e.message}")
            //ignore
        }
    }
}
