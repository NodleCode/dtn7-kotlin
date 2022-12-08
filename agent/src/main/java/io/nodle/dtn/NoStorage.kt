package io.nodle.dtn

import io.nodle.dtn.bpv7.BundleID
import io.nodle.dtn.bpv7.FragmentID
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.interfaces.IBundleStorage
import io.nodle.dtn.interfaces.IStorage

object NoStorage : IStorage{
    override fun init() {
    }

    override fun clearAllTables() {
    }

    override fun close() {
    }

    override val bundleStore = object: IBundleStorage {
        override fun size() = 0
        override fun gc(now: Long) {}
        override fun getAllBundleIds() = listOf<BundleID>()
        override fun get(bid: BundleID) = null
        override fun exists(bid: BundleID) = false
        override fun insert(desc: BundleDescriptor) = null
        override fun delete(bid: BundleID) {}
        override fun deleteAll() {}
        override fun getAllFragments(fragmentId: FragmentID) = listOf<BundleID>()
        override fun isBundleWhole(fragmentId: FragmentID) = false
        override fun getBundleFromFragments(fragmentId: FragmentID) = null
        override fun deleteAllFragments(fragmentId: FragmentID) {}
    }


}