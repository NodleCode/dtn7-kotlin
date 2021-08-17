package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.administrative.StatusAssertion
import io.nodle.dtn.bpv7.administrative.StatusReportReason
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.interfaces.IAgent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

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
    fun stage01_testSimpleDelivery() {
        runBlockingTest {
            /* When */
            receive(MockBundle.inBundle1)

            /* Then */
            Assert.assertTrue(delivered.size == 1)
        }
    }

    @Test
    fun stage02_testDeliveryWithAck() {
        runBlockingTest {
            /* When */
            receive(MockBundle.inBundle2)

            /* Then */
            Assert.assertTrue(delivered.size == 1)
        }
    }

    @Test
    fun stage03_testDeliveryWithAck() {
        runBlockingTest {
            /* When */
            receive(MockBundle.inBundle2)

            /* Then */
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage04_testSimpleTransmission() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle1)

            /* Then */
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage05_testTransmissionWithForwardAck() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle2)

            /* Then */
            Assert.assertTrue(transmitted.size == 2)
        }
    }

    @Test
    fun stage06_testTransmissionWithForwardAndReceptionAck() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle3)

            /* Then */
            Assert.assertTrue(transmitted.size == 3)
        }
    }

    @Test
    fun stage07_testSimpleDeliveryExpired() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle4)

            /* Then */
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage08_testSimpleDeliveryHopBlock() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle5)

            /* Then */
            Assert.assertTrue(transmitted.size == 2)
        }
    }

    @Test
    fun stage09_testSimpleDeliveryHopBlockAbove() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle6)

            /* Then */
            Assert.assertTrue(transmitted.size == 0)
        }
    }

    @Test
    fun stage10_testSimpleDeliveryPreviousBlock() {
        runBlockingTest {
            /* When */
            receive(MockBundle.outBundle7)

            /* Then */
            Assert.assertTrue(transmitted.size == 2)
        }
    }

    @Test
    fun stage11_testSimpleDeliveryAdminBlock() {
        runBlockingTest {
            /* When */
            transmit(MockBundle.bundleAdm)

            /* Then */
            Assert.assertTrue(transmitted.size == 1)
        }
    }

    @Test
    fun stage12_testBpaLog() {
        runBlockingTest {
            /* Check */
            Assert.assertNotNull(bpaLog)
        }
    }

    @Test
    fun stage13_testAdminAgentFlag() {
        runBlockingTest {
            /* Given */
            val core = mock<IAgent>()
            val desc = mock<BundleDescriptor>() {
                on { mock.bundle } doReturn MockBundle.bundleAdm
            }
            val assertion = mock<StatusAssertion>()
            val reason = mock<StatusReportReason>()

            /* When */
            adm.sendStatusReport(core, desc, assertion, reason)

            /* Then */
            verify(desc, atLeastOnce()).bundle
        }
    }

    @Test
    fun stage14_testCanonStatusNotProcessed() {
        runBlockingTest {
            /* Given */
            val bundle = MockBundle.outBundle8
            val canon = CanonicalBlock()
                .setFlag(BlockV7Flags.StatusReportIfNotProcessed)

            /* When */
            bundle.addBlock(canon)
            receive(bundle)

            /* Then */
            Assert.assertTrue(transmitted.size == 3)
        }
    }

    @Test
    fun stage15_testCanonDeleteBundleIfNotProcessed() {
        runBlockingTest {
            /* Given */
            val bundle = MockBundle.outBundle8
            val canon = CanonicalBlock()
                .setFlag(BlockV7Flags.DeleteBundleIfNotProcessed)

            /* When */
            bundle.addBlock(canon)
            receive(bundle)

            /* Then */
            Assert.assertTrue(transmitted.size == 0)
        }
    }

    @Test
    fun stage16_testCanonDiscardIfNotProcessed() {
        runBlockingTest {
            /* Given */
            val bundle = MockBundle.outBundle9
            val canon = CanonicalBlock()
                .setFlag(BlockV7Flags.DiscardIfNotProcessed)

            /* When */
            bundle.addBlock(canon)
            receive(bundle)

            /* Then */
            Assert.assertTrue(transmitted.size == 2)
        }
    }

    @Test
    fun stage17_testCanonCloneHeader() {
        runBlockingTest {
            /* Given */
            val bundle = MockBundle.outBundle9
            val canon = CanonicalBlock()
                .setFlag(BlockV7Flags.DiscardIfNotProcessed)
            val canonDouble = CanonicalBlock()
                .setFlag(BlockV7Flags.DeleteBundleIfNotProcessed)

            /* When */
            canon.cloneHeader(canonDouble)
            bundle.addBlock(canon)
            receive(bundle)

            /* Then */
            Assert.assertTrue(transmitted.size == 0)
        }
    }
}