package io.nodle.dtn.storage

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.interfaces.*

class AndroidBundleStorageImpl(private val bundleEntryDao: BundleEntryDao) : IBundleStorage {
    override fun size() = bundleEntryDao.size()
    override fun gc(now: Long)  = bundleEntryDao.gc(now)
    override fun getAllBundleIds() = bundleEntryDao.getAllBundleIds()
    override fun get(bid: BundleID) = bundleEntryDao.get(bid)?.toBundleDescriptor()
    override fun exists(bid: BundleID) = bundleEntryDao.exists(bid)
    override fun insert(desc: BundleDescriptor) = bundleEntryDao.insert(BundleEntry(desc))
    override fun delete(bid: BundleID) = bundleEntryDao.delete(bid)
    override fun deleteAll() = bundleEntryDao.deleteAll()

    override fun getAllFragments(fragmentId: FragmentID) =
        bundleEntryDao.getAllFragments(fragmentId).map { it.bid }

    override fun isBundleWhole(fragmentId: FragmentID): Boolean {
        return bundleEntryDao.getAllFragments(fragmentId).fold(Pair(0L, 0L)) { acc, elem ->
            if(acc.second != elem.offset) {
                return@isBundleWhole false
            }
            Pair(elem.appdata, acc.second + elem.payload_size)
        }.run {
            first == second
        }
    }

    override fun getBundleFromFragments(fragmentId: FragmentID): BundleDescriptor? {
        return bundleEntryDao.getAllFragments(fragmentId).fold(null as BundleDescriptor?) { acc, elem ->
            acc?.apply {
                bundle.getPayloadBlockData().buffer += get(elem.bid)!!.bundle.getPayloadBlockData().buffer
            } ?: get(elem.bid)?.apply {
                this.bundle.primaryBlock.unsetProcV7Flags(BundleV7Flags.IsFragment)
            }
        }
    }

    override fun deleteAllFragments(fragmentId: FragmentID) =
        bundleEntryDao.deleteAllFragments(fragmentId)

}