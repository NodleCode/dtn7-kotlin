package io.nodle.dtn

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.interfaces.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.mockito.kotlin.*
import java.net.URI

/**
 * @author Lucien Loiseau on 18/02/21.
 */
@ExperimentalCoroutinesApi
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TestBundleProcessor {

    @Test
    fun stage0_testSimpleDelivery() {
        val delivered: MutableList<Bundle> = mutableListOf()
        val node = BpNode(URI.create("dtn://test/"))
        node.applicationAgent
            .handlePath("/test1") {
                delivered.add(it)
                DeliveryStatus.DeliverySuccessful
            }

        runTest {
            node.bpa.receivePDU(MockBundle.inBundle1)
            Assert.assertTrue(delivered.size == 1)
        }
    }

    @Test
    fun stage1_testDeliveryWithAck() {
        val delivered: MutableList<Bundle> = mutableListOf()
        val transmitted: MutableList<Bundle> = mutableListOf()
        val node = BpNode(URI.create("dtn://test/"))

        node.applicationAgent.handlePath("/test1") {
            delivered.add(it)
            DeliveryStatus.DeliverySuccessful
        }

        node.router.setDefaultRoute(mock {
            on { scheduleForTransmission } doReturn {
                transmitted.add(it)
                TransmissionStatus.TransmissionSuccessful
            }
        })

        runTest {
            node.bpa.receivePDU(MockBundle.inBundle2)
            Assert.assertTrue(delivered.size == 1)
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage2_testSimpleTransmission() {
        val transmitted: MutableList<Bundle> = mutableListOf()
        val node = BpNode(URI.create("dtn://test/"))
        node.router.setDefaultRoute(mock {
            on { scheduleForTransmission } doReturn  {
                transmitted.add(it)
                TransmissionStatus.TransmissionSuccessful
            }
        })

        runTest {
            node.bpa.receivePDU(MockBundle.outBundle1)
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage3_testTransmissionWithForwardAck() {
        val transmitted: MutableList<Bundle> = mutableListOf()
        val node = BpNode(URI.create("dtn://test/"))
        node.router.setDefaultRoute(mock {
            on { scheduleForTransmission } doReturn  {
                transmitted.add(it)
                TransmissionStatus.TransmissionSuccessful
            }
        })

        runTest {
            node.bpa.receivePDU(MockBundle.outBundle2)
            Assert.assertTrue(transmitted.size == 2)
        }
    }

    @Test
    fun stage4_testTransmissionWithForwardAndReceptionAck() {
        val transmitted: MutableList<Bundle> = mutableListOf()
        val node = BpNode(URI.create("dtn://test/"))
        node.router.setDefaultRoute(mock {
            on { scheduleForTransmission } doReturn {
                transmitted.add(it)
                TransmissionStatus.TransmissionSuccessful
            }
        })

        runTest {
            node.bpa.receivePDU(MockBundle.outBundle3)
            Assert.assertTrue(transmitted.size == 3)
        }
    }
}