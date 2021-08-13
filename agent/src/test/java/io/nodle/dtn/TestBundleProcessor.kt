package io.nodle.dtn

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule

/**
 * @author Lucien Loiseau on 18/02/21.
 */
@ExperimentalCoroutinesApi
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class TestBundleProcessor : MockAgent(MockBundle.localNodeId) {

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun stage0_testSimpleDelivery() {
        runBlockingTest {
            /* When */
            receive(MockBundle.inBundle1)

            /* Then */
            Assert.assertTrue(delivered.size == 1)
        }
    }

    @Test
    fun stage1_testDeliveryWithAck() {
        runBlockingTest {
            /* When */
            receive(MockBundle.inBundle2)

            /* Then */
            Assert.assertTrue(delivered.size == 1)
        }
    }

    @Test
    fun stage2_testDeliveryWithAck() {
        runBlockingTest {
            /* When */
            receive(MockBundle.inBundle2)

            /* Then */
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage3_testSimpleTransmission() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle1)

            /* Then */
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage4_testTransmissionWithForwardAck() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle2)

            /* Then */
            Assert.assertTrue(transmitted.size == 2)
        }
    }

    @Test
    fun stage5_testTransmissionWithForwardAndReceptionAck() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle3)

            /* Then */
            Assert.assertTrue(transmitted.size == 3)
        }
    }

    @Test
    fun stage6_testSimpleDeliveryExpired() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle4)

            /* Then */
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage7_testSimpleDeliveryHopBlock() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle5)

            /* Then */
            Assert.assertTrue(transmitted.size == 2)
        }
    }

    @Test
    fun stage8_testSimpleDeliveryHopBlockAbove() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle6)

            /* Then */
            Assert.assertTrue(transmitted.size == 0)
        }
    }

    @Test
    fun stage9_testSimpleDeliveryPreviousBlock() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle7)

            /* Then */
            Assert.assertTrue(transmitted.size == 2)
        }
    }

    @Test
    fun stage10_testSimpleDeliveryAdminBlock() {
        runBlockingTest {
            /* When */
            transmit(MockBundle.bundleAdm)

            /* Then */
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage11_testBpaLog() {
        runBlockingTest {
            /* Check */
            Assert.assertNotNull(bpaLog)
        }
    }
}