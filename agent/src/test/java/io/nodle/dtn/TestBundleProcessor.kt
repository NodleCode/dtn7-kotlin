package io.nodle.dtn

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
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
        runBlockingTest {
            receive(MockBundle.inBundle1)
            Assert.assertTrue(delivered.size == 1)
        }
    }

    @Test
    fun stage1_testDeliveryWithAck() {
        runBlockingTest {
            receive(MockBundle.inBundle2)
            Assert.assertTrue(delivered.size == 1)
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage2_testSimpleTransmission() {
        runBlockingTest {
            receive(MockBundle.outBundle1)
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage3_testTransmissionWithForwardAck() {
        runBlockingTest {
            receive(MockBundle.outBundle2)
            Assert.assertTrue(transmitted.size == 2)
        }
    }

    @Test
    fun stage4_testTransmissionWithForwardAndReceptionAck() {
        runBlockingTest {
            receive(MockBundle.outBundle3)
            Assert.assertTrue(transmitted.size == 3)
        }
    }

}