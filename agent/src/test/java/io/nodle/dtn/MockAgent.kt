package io.nodle.dtn

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.BundleID
import io.nodle.dtn.bpv7.FragmentID
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

    val sto = object: IBundleStorage {
        override fun size(): Int {
            TODO("Not yet implemented")
        }

        override fun gc(now: Long) {
            TODO("Not yet implemented")
        }

        override fun getAllBundleIds(): List<BundleID> {
            TODO("Not yet implemented")
        }

        override fun get(bid: BundleID): BundleDescriptor? {
            TODO("Not yet implemented")
        }

        override fun exists(bid: BundleID): Boolean {
            TODO("Not yet implemented")
        }

        override fun insert(bundle: BundleDescriptor): Long {
            TODO("Not yet implemented")
        }

        override fun delete(bid: BundleID) {
            TODO("Not yet implemented")
        }

        override fun deleteAll() {
            TODO("Not yet implemented")
        }

        override fun getAllFragments(fragmentId: FragmentID): List<FragmentID> {
            TODO("Not yet implemented")
        }

        override fun isBundleWhole(fragmentId: FragmentID) {
            TODO("Not yet implemented")
        }

        override fun getBundleFromFragments(fragmentID: FragmentID): BundleDescriptor? {
            TODO("Not yet implemented")
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

    override fun getBundleStorage() = sto
}