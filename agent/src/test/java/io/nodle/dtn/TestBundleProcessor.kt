package io.nodle.dtn

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * @author Lucien Loiseau on 18/02/21.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TestBundleProcessor : MockAgent(MockBundle.localNodeId) {

    @Test
    fun stage0_testSimpleDelivery() {
        runBlocking {
            receive(MockBundle.inBundle1)
            Assert.assertTrue(delivered.size == 1)
        }
    }

    @Test
    fun stage1_testDeliveryWithAck() {
        runBlocking {
            receive(MockBundle.inBundle2)
            Assert.assertTrue(delivered.size == 1)
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage2_testSimpleTransmission() {
        runBlocking {
            receive(MockBundle.outBundle1)
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage3_testTransmissionWithForwardAck() {
        runBlocking {
            receive(MockBundle.outBundle2)
            Assert.assertTrue(transmitted.size == 2)
        }
    }

    @Test
    fun stage4_testTransmissionWithForwardAndReceptionAck() {
        runBlocking {
            receive(MockBundle.outBundle3)
            Assert.assertTrue(transmitted.size == 3)
        }
    }

}