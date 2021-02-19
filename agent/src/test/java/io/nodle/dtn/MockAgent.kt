package io.nodle.dtn

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.ID
import io.nodle.dtn.interfaces.*
import org.junit.Before
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * @author Lucien Loiseau on 18/02/21.
 */
abstract class MockAgent(val localId : URI) : BundleProtocolAgent() {

    val log = LoggerFactory.getLogger("MockAgent")

    var delivered: MutableList<Bundle> = mutableListOf()
    var transmitted: MutableList<Bundle> = mutableListOf()

    inner class MockRegistrar: IRegistrar {
        override fun localDelivery(bundle: Bundle): IApplicationAgent? {
          return object : IApplicationAgent {
              override suspend fun deliver(bundle: Bundle): Boolean {
                  log.debug("bundle:${bundle.ID()} - bundle delivered")
                  delivered.add(bundle)
                  return true
              }
          }
        }

        override fun register(eid: URI, aa: IApplicationAgent): Boolean {
            TODO("Not yet implemented")
        }

        override fun listEndpoint(): List<URI> {
            TODO("Not yet implemented")
        }
    }

    inner class MockRouter: IRouter {
        override fun findRoute(bundle: Bundle): IConvergenceLayerSender? {
            return object : IConvergenceLayerSender {
                override suspend fun sendBundle(bundle: Bundle): Boolean {
                    log.debug("bundle:${bundle.ID()} - bundle transmitted")
                    transmitted.add(bundle)
                    return true
                }

                override fun getPeerEndpointId(): URI {
                    return URI.create("dtn://cla:peerid/")
                }

            }
        }
    }

    @Before
    fun registerMock() {
        bpRegistrar = MockRegistrar()
        bpRouter = MockRouter()
    }

    override fun nodeId(): URI {
        return URI.create("dtn://test/")
    }

    override fun hasEndpoint(eid: URI): Boolean {
        return eid == nodeId()
    }

    override fun getStorage(): IStorage {
        TODO("Not yet implemented")
    }

    override suspend fun checkStorageForTransmitOpportunity() {
        TODO("Not yet implemented")
    }
}