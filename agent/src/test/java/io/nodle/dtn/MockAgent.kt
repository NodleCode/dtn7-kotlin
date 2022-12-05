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

    val adm = AdministrativeAgent()
    val reg = object: IRegistrar {
        override fun localDelivery(destination: URI): IApplicationAgent {
          return object : IApplicationAgent {
              override fun onRegistrationActive(active: IActiveRegistration) {
                  // ignore
              }

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

    val rou = object: IRouter {
        override fun setDefaultRoute(cla: IConvergenceLayerSender?) {
            // ignore
        }

        override fun findRoute(bundle: Bundle): IConvergenceLayerSender? {
            return object : IConvergenceLayerSender {
                override suspend fun sendBundles(bundles: List<Bundle>): Boolean {
                    bundles.map{sendBundle(bundle)}
                    return true
                }

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

    override fun nodeId(): URI {
        return URI.create("dtn://test/")
    }

    override fun isLocal(eid: URI): Boolean {
        return eid == nodeId()
    }

    override suspend fun isDuplicate(bundle: Bundle): Boolean {
        return false
    }

    override suspend fun doneProcessing(desc: BundleDescriptor) {
    }

    suspend fun checkForwardOpportunity() {
    }

    override fun getAdministrativeAgent() = adm

    override fun getRegistrar()  = reg

    override fun getRouter() = rou
}