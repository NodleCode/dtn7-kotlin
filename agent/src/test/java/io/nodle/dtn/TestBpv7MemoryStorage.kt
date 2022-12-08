package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.eid.createDtnEid
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.utils.encodeToBase64
import io.nodle.dtn.utils.wait
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random

class TestBpv7MemoryStorage {

    private fun bundle(s: Int = 10000) = PrimaryBlock()
        .destination(createDtnEid("test-destination"))
        .source(createDtnEid("test-source"))
        .reportTo(createDtnEid("test-report-to"))
        .lifetime(10000)
        .crcType(CRCType.CRC32)
        .makeBundle()
        .addBlock(payloadBlock(Random.nextBytes(array = ByteArray(s))))

    private fun bundles(c: Int, s: Int = 10000) = (0 until c).map { bundle(s) }

    @Test
    fun testInsertGetExists() {
        val storage = Bpv7MemoryStorage()
        val bundle = bundle()

        storage.bundleStore.wait { insert(BundleDescriptor(bundle)) }
        Assert.assertEquals(1, storage.bundleStore.wait { size() })
        Assert.assertEquals(true, storage.bundleStore.wait { exists(bundle.ID()) })

        val b = storage.bundleStore.wait { get(bundle.ID()) }
        Assert.assertEquals(
            b!!.bundle.cborMarshal().encodeToBase64(),
            bundle.cborMarshal().encodeToBase64()
        )
    }

    @Test
    fun testGc() {
        val storage = Bpv7MemoryStorage()
        val bundle = bundle()

        storage.bundleStore.wait { insert(BundleDescriptor(bundle)) }
        Assert.assertEquals(1, storage.bundleStore.wait { size() })

        storage.bundleStore.wait { gc(bundle.primaryBlock.creationTimestamp + 1000) }
        Assert.assertEquals(1, storage.bundleStore.wait { size() })

        storage.bundleStore.wait { gc(bundle.primaryBlock.creationTimestamp + 20000) }
        Assert.assertEquals(0, storage.bundleStore.wait { size() })
    }

    @Test
    fun testGetAllBundleIds() {
        val storage = Bpv7MemoryStorage()
        val bundles = bundles(10)

        bundles.map {
            storage.bundleStore.wait { insert(BundleDescriptor(it)) }
        }
        Assert.assertEquals(10, storage.bundleStore.wait { size() })

        val ids = storage.bundleStore.wait { getAllBundleIds() }
        Assert.assertEquals(10, ids.size)

        val originalIds = bundles.map { it.ID() }
        ids.map {
            Assert.assertEquals(true, originalIds.contains(it))
        }
    }

    @Test
    fun testDelete() {
        val storage = Bpv7MemoryStorage()
        val bundle = bundle()

        storage.bundleStore.wait { insert(BundleDescriptor(bundle)) }
        Assert.assertEquals(1, storage.bundleStore.wait { size() })
        Assert.assertEquals(true, storage.bundleStore.wait { exists(bundle.ID()) })

        storage.bundleStore.wait { delete(bundle.ID()) }
        Assert.assertEquals(0, storage.bundleStore.wait { size() })
        Assert.assertEquals(false, storage.bundleStore.wait { exists(bundle.ID()) })
    }


    @Test
    fun testDeleteAll() {
        val storage = Bpv7MemoryStorage()
        val bundles = bundles(10)

        bundles.map {
            storage.bundleStore.wait { insert(BundleDescriptor(it)) }
        }
        Assert.assertEquals(10, storage.bundleStore.wait { size() })

        storage.bundleStore.wait { deleteAll() }
        Assert.assertEquals(0, storage.bundleStore.wait { size() })
        bundles.map {
            Assert.assertEquals(false, storage.bundleStore.wait { exists(it.ID()) })
        }
    }

    @Test
    fun testGetAllFragment() {
        val storage = Bpv7MemoryStorage()
        val bundle = bundle(10000)
        val fragments = bundle.fragment(300)

        fragments.map {
            storage.bundleStore.wait { insert(BundleDescriptor(it)) }
        }
        Assert.assertEquals(fragments.size, storage.bundleStore.wait { size() })
        Assert.assertEquals(
            fragments.size,
            storage.bundleStore.wait { getAllFragments(bundle.fragmentedID()).size })
    }


    @Test
    fun testIsBundleWhole() {
        val storage = Bpv7MemoryStorage()
        val bundle = bundle(10000)
        val fragments = bundle.fragment(300)

        fragments.mapIndexed { i, it ->
            if (i != 0) { // missing the first one
                storage.bundleStore.wait { insert(BundleDescriptor(it)) }
            }
        }
        Assert.assertEquals(fragments.size - 1, storage.bundleStore.wait { size() })
        Assert.assertEquals(
            false,
            storage.bundleStore.wait { isBundleWhole(bundle.fragmentedID()) })

        storage.bundleStore.wait { insert(BundleDescriptor(fragments[0])) }
        Assert.assertEquals(fragments.size, storage.bundleStore.wait { size() })
        Assert.assertEquals(true, storage.bundleStore.wait { isBundleWhole(bundle.fragmentedID()) })
    }

    @Test
    fun testGetBundleFromFragment() {
        val storage = Bpv7MemoryStorage()
        val bundle = bundle(10000)
        val fragments = bundle.fragment(300)

        fragments.map { storage.bundleStore.wait { insert(BundleDescriptor(it)) } }
        Assert.assertEquals(fragments.size, storage.bundleStore.wait { size() })
        Assert.assertEquals(true, storage.bundleStore.wait { isBundleWhole(bundle.fragmentedID()) })
        val reassembled = storage.bundleStore.wait { getBundleFromFragments(bundle.fragmentedID()) }
        Assert.assertEquals(
            bundle.cborMarshal().encodeToBase64(),
            reassembled!!.bundle.cborMarshal().encodeToBase64()
        )
    }

    @Test
    fun testDeleteAllFragment() {
        val storage = Bpv7MemoryStorage()
        val bundle = bundle(10000)
        val fragments = bundle.fragment(300)

        fragments.map { it ->
            storage.bundleStore.wait { insert(BundleDescriptor(it)) }
        }
        Assert.assertEquals(fragments.size, storage.bundleStore.wait { size() })
        Assert.assertEquals(true, storage.bundleStore.wait { isBundleWhole(bundle.fragmentedID()) })

        storage.bundleStore.wait { deleteAllFragments(bundle.fragmentedID()) }
        Assert.assertEquals(0, storage.bundleStore.wait { size() })
    }
}