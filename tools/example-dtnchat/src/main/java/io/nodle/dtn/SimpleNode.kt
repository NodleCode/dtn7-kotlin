package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.cla.http.ClaHttpClient
import io.nodle.dtn.cla.http.ClaHttpServer
import io.nodle.dtn.cla.http.ClaHttpServerConfig
import io.nodle.dtn.cla.http.doPeriodicHttpPolling
import io.nodle.dtn.interfaces.DeliveryStatus
import java.net.URI

fun createBpNodeClient(uri: URI, path: String, handler: (Bundle) -> Unit) =
    BpNode(uri, Bpv7MemoryStorage()).apply {
        // register all services
        applicationAgent.handlePath(path) {
            handler(it)
            DeliveryStatus.DeliverySuccessful
        }

        // register the egress channel in the router
        val cla = ClaHttpClient(
            agent = this,
            httpEndpoint = URI.create("http://127.0.0.1:12345"),
            peerEndpointId = URI.create("dtn://server/")
        )
        router.setDefaultRoute(cla)
        doPeriodicHttpPolling(scope, cla, 5, 5)
    }

fun createBpNodeServer(uri: URI, path: String, handler: (Bundle) -> Unit) =
    BpNode(uri, Bpv7MemoryStorage()).apply {
        applicationAgent
            .handlePath(path) {
                handler(it)
                DeliveryStatus.DeliverySuccessful
            }

        ClaHttpServer(
            agent = this,
            config = ClaHttpServerConfig(listenPort = 12345)
        ).start()
    }