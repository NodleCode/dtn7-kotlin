package io.nodle.dtn.storage

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.eid.createDtnEid
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.utils.encodeToBase64
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class StorageRoomTest {

    private fun bundle(s: Int = 10000) = PrimaryBlock()
        .destination(createDtnEid("test-destination"))
        .source(createDtnEid("test-source"))
        .reportTo(createDtnEid("test-report-to"))
        .lifetime(1000)
        .crcType(CRCType.CRC32)
        .makeBundle()
        .addBlock(payloadBlock(Random.nextBytes(array = ByteArray(s))))

    private fun bundles(c: Int, s: Int = 10000) = (0 until c).map { bundle(s) }

    @Test
    fun testInsertGetExists() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = AndroidStorageImpl(ctx, true)
        val bundle = bundle()

        storage.bundleStore.insert(BundleDescriptor(bundle))
        Assert.assertEquals(1, storage.bundleStore.size())
        Assert.assertEquals(true, storage.bundleStore.exists(bundle.ID()))

        val b = storage.bundleStore.get(bundle.ID())
        Assert.assertEquals(b!!.bundle.cborMarshal().encodeToBase64(), bundle.cborMarshal().encodeToBase64())
    }

    @Test
    fun testGc() {
        val storage = AndroidStorageImpl(ApplicationProvider.getApplicationContext(), true)
        val bundle = bundle()

        storage.bundleStore.insert(BundleDescriptor(bundle))
        Assert.assertEquals(1, storage.bundleStore.size())

        storage.bundleStore.gc(bundle.primaryBlock.creationTimestamp + 500)
        Assert.assertEquals(1, storage.bundleStore.size())

        storage.bundleStore.gc(bundle.primaryBlock.creationTimestamp + 2000)
        Assert.assertEquals(0, storage.bundleStore.size())
    }

    @Test
    fun testGetAllBundleIds() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = AndroidStorageImpl(ctx, true)
        val bundles = bundles(10)

        bundles.map {
            storage.bundleStore.insert(BundleDescriptor(it))
        }
        Assert.assertEquals(10, storage.bundleStore.size())

        val ids = storage.bundleStore.getAllBundleIds()
        Assert.assertEquals(10, ids.size)

        ids.map {
            Assert.assertEquals(true, bundles.map { it.ID() }.contains(it))
        }
    }

    @Test
    fun testDelete() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = AndroidStorageImpl(ctx, true)
        val bundle = bundle()

        storage.bundleStore.insert(BundleDescriptor(bundle))
        Assert.assertEquals(1, storage.bundleStore.size())
        Assert.assertEquals(true, storage.bundleStore.exists(bundle.ID()))

        storage.bundleStore.delete(bundle.ID())
        Assert.assertEquals(0, storage.bundleStore.size())
        Assert.assertEquals(false, storage.bundleStore.exists(bundle.ID()))
    }


    @Test
    fun testDeleteAll() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = AndroidStorageImpl(ctx, true)
        val bundles = bundles(10)

        bundles.map {
            storage.bundleStore.insert(BundleDescriptor(it))
        }
        Assert.assertEquals(10, storage.bundleStore.size())

        storage.bundleStore.deleteAll()
        Assert.assertEquals(0, storage.bundleStore.size())
        bundles.map {
            Assert.assertEquals(false, storage.bundleStore.exists(it.ID()))
        }
    }

    @Test
    fun testGetAllFragment() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = AndroidStorageImpl(ctx, true)
        val bundle = bundle(10000)
        val fragments = bundle.fragment(300)

        fragments.map {
            storage.bundleStore.insert(BundleDescriptor(it))
        }
        Assert.assertEquals(fragments.size, storage.bundleStore.size())
        Assert.assertEquals(fragments.size, storage.bundleStore.getAllFragments(bundle.fragmentedID()).size)
    }


    @Test
    fun testIsBundleWhole() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = AndroidStorageImpl(ctx, true)
        val bundle = bundle(10000)
        val fragments = bundle.fragment(300)

        fragments.mapIndexed { i, it ->
            if (i != 0) { // missing the first one
                storage.bundleStore.insert(BundleDescriptor(it))
            }
        }
        Assert.assertEquals(fragments.size - 1, storage.bundleStore.size())
        Assert.assertEquals(false, storage.bundleStore.isBundleWhole(bundle.fragmentedID()))

        storage.bundleStore.insert(BundleDescriptor(fragments[0]))
        Assert.assertEquals(fragments.size, storage.bundleStore.size())
        Assert.assertEquals(true, storage.bundleStore.isBundleWhole(bundle.fragmentedID()))
    }

    @Test
    fun testGetBundleFromFragment() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = AndroidStorageImpl(ctx, true)
        val bundle = bundle(10000)
        val fragments = bundle.fragment(300)

        fragments.map {
            storage.bundleStore.insert(BundleDescriptor(it))
        }
        Assert.assertEquals(fragments.size, storage.bundleStore.size())
        Assert.assertEquals(true, storage.bundleStore.isBundleWhole(bundle.fragmentedID()))
        val reassembled = storage.bundleStore.getBundleFromFragments(bundle.fragmentedID())
        Assert.assertEquals(bundle.cborMarshal().encodeToBase64(), reassembled!!.bundle.cborMarshal().encodeToBase64())
    }

    @Test
    fun testDeleteAllFragment() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = AndroidStorageImpl(ctx, true)
        val bundle = bundle(10000)
        val fragments = bundle.fragment(300)

        fragments.map {
            storage.bundleStore.insert(BundleDescriptor(it))
        }
        Assert.assertEquals(fragments.size, storage.bundleStore.size())
        Assert.assertEquals(true, storage.bundleStore.isBundleWhole(bundle.fragmentedID()))

        storage.bundleStore.deleteAllFragments(bundle.fragmentedID())
        Assert.assertEquals(0, storage.bundleStore.size())
    }
}