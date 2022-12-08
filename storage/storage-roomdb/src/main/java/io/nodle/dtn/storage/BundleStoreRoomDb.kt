package io.nodle.dtn.storage

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.interfaces.*

class BundleStoreRoomDb(private val bundleEntryDao: BundleEntryDao) : IBundleStore {
    override suspend fun size() = bundleEntryDao.size()
    override suspend fun gc(now: Long)  = bundleEntryDao.gc(now)
    override suspend fun getAllBundleIds() = bundleEntryDao.getAllBundleIds()
    override suspend fun get(bid: BundleID) = bundleEntryDao.get(bid)?.toBundleDescriptor()
    override suspend fun exists(bid: BundleID) = bundleEntryDao.exists(bid)
    override suspend fun insert(desc: BundleDescriptor) {
        bundleEntryDao.insert(BundleEntry(desc))
    }
    override suspend fun delete(bid: BundleID) = bundleEntryDao.delete(bid)
    override suspend fun deleteAll() = bundleEntryDao.deleteAll()

    override suspend fun getAllFragments(fragmentId: FragmentID) =
        bundleEntryDao.getAllFragments(fragmentId).map { it.bid }

    override suspend fun isBundleWhole(fragmentId: FragmentID): Boolean {
        return bundleEntryDao.getAllFragments(fragmentId).fold(Pair(0L, 0L)) { acc, elem ->
            if(acc.second != elem.offset) {
                return@isBundleWhole false
            }
            Pair(elem.appdata, acc.second + elem.payload_size)
        }.run {
            first == second
        }
    }

    override suspend fun getBundleFromFragments(fragmentId: FragmentID): BundleDescriptor? {
        return bundleEntryDao.getAllFragments(fragmentId).fold(null as BundleDescriptor?) { acc, elem ->
            acc?.apply {
                bundle.getPayloadBlockData().buffer += get(elem.bid)!!.bundle.getPayloadBlockData().buffer
            } ?: get(elem.bid)?.apply {
                this.bundle.primaryBlock.unsetProcV7Flags(BundleV7Flags.IsFragment)
            }
        }
    }

    override suspend fun deleteAllFragments(fragmentId: FragmentID) =
        bundleEntryDao.deleteAllFragments(fragmentId)

}