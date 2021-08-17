package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.eid.nullDtnEid
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.interfaces.ID
import io.nodle.dtn.interfaces.fragmentedID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * @author Niki Izvorski on 12/08/21.
 */
@ExperimentalCoroutinesApi
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class TestBundleDescriptor : MockAgent(MockBundle.localNodeId) {
    private val constrains = mutableListOf<String>()
    private val tags = mutableListOf<String>()
    private val created = System.currentTimeMillis()
    private val mockAgent = mock<BundleProtocolAgent>()

    private val mockPrimaryBlock = mock<PrimaryBlock>() {
        onGeneric { mock.lifetime } doReturn 1000L
        onGeneric { mock.source } doReturn nullDtnEid()
    }
    private val mockBundle = mock<Bundle>() {
        on { mock.primaryBlock } doReturn mockPrimaryBlock
    }

    private val desc = BundleDescriptor(mockBundle, created, tags, constrains)

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun stage0_testDescriptorBundle() {
        runBlockingTest {
            /* Check */
            assertEquals(desc.bundle.primaryBlock.lifetime, 1000)
        }
    }

    @Test
    fun stage1_testDescriptorBundleID() {
        runBlockingTest {
            /* Check */
           assertEquals(desc.ID(), "6cd7adba-9b69-3286-a893-e95f0404a14a")
        }
    }

    @Test
    fun stage2_testDescriptorBundleFragmentID() {
        runBlockingTest {
            /* Check */
            assertEquals(desc.fragmentedID(), "3ef25374-41ff-358b-9269-0a7e0f1697ca")
        }
    }

    @Test
    fun stage3_testDescriptorBundleExist() {
        runBlockingTest {
            /* When */
            desc.bundle = mockBundle

            /* Then */
            assertEquals(desc.bundle, mockBundle)
        }
    }

    @Test
    fun stage4_testDescriptorCreatedTimeExist() {
        runBlockingTest {
            /* When */
            desc.created = created

            /* Then */
            assertEquals(desc.created, created)
        }
    }

    @Test
    fun stage5_testDescriptorConstrainstExist() {
        runBlockingTest {
            /* When */
            desc.constraints = constrains
            desc.constraints.add("test")

            /* Then */
            assertEquals(desc.constraints.size, 1)
        }
    }

    @Test
    fun stage6_testDescriptorTagsExist() {
        runBlockingTest {
            /* When */
            desc.tags = tags
            desc.tags.add("test")

            /* Then */
            assertEquals(desc.tags.size, 1)
        }
    }

    @Test
    fun stage7_testDescriptorConstrains() {
        runBlockingTest {
            /* Given */
            desc.bundle = MockBundle.outBundle3
            desc.constraints = constrains
            desc.constraints.add("test")

            /* When */
            mockAgent.bundleReceive(desc)

            /* Then */
            assertEquals(desc.constraints.size, 1)
        }
    }

    @Test
    fun stage8_testBundleContraindicated() {
        runBlockingTest {
            /* Given */
            desc.constraints = constrains
            desc.bundle = MockBundle.outBundle3

            /* When */
            mockAgent.bundleContraindicated(desc)

            /* Then */
            assertEquals(desc.constraints.size, 1)
        }
    }
}