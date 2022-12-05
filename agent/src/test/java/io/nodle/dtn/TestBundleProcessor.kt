package io.nodle.dtn

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * @author Lucien Loiseau on 18/02/21.
 */
@ExperimentalCoroutinesApi
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TestBundleProcessor : MockAgent(MockBundle.localNodeId) {

    @Test
    fun stage0_testSimpleDelivery() {
        runTest {
            receive(MockBundle.inBundle1)
            Assert.assertTrue(delivered.size == 1)
        }
    }

    @Test
    fun stage1_testDeliveryWithAck() {
        runTest {
            receive(MockBundle.inBundle2)
            Assert.assertTrue(delivered.size == 1)
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage2_testSimpleTransmission() {
        runTest {
            receive(MockBundle.outBundle1)
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage3_testTransmissionWithForwardAck() {
        runTest {
            receive(MockBundle.outBundle2)
            Assert.assertTrue(transmitted.size == 2)
        }
    }

    @Test
    fun stage4_testTransmissionWithForwardAndReceptionAck() {
        runTest {
            receive(MockBundle.outBundle3)
            Assert.assertTrue(transmitted.size == 3)
        }
    }

}