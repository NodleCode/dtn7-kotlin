package io.nodle.dtn

import io.nodle.dtn.bpv7.BundleID
import io.nodle.dtn.bpv7.FragmentID
import io.nodle.dtn.bpv7.PrimaryBlock
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.interfaces.IBundleStore
import io.nodle.dtn.interfaces.Bpv7Storage
import io.nodle.dtn.interfaces.PrimaryBlockDescriptor

object NoBpv7Storage : Bpv7Storage {
    override fun init() {
    }

    override fun clearAllTables() {
    }

    override fun close() {
    }

    override val bundleStore = object : IBundleStore {
        override suspend fun size() = 0
        override suspend fun gc(now: Long) {}
        override suspend fun getAllBundleIds() = listOf<BundleID>()
        override suspend fun getNBundleIds(n: Int) = listOf<BundleID>()
        override suspend fun get(bid: BundleID) = null
        override suspend fun exists(bid: BundleID) = false
        override suspend fun insert(desc: BundleDescriptor) {}
        override suspend fun delete(bid: BundleID) {}
        override suspend fun deleteAll() {}
        override suspend fun getAllPrimaryDesc(predicate: (PrimaryBlockDescriptor) -> Boolean) =
            listOf<PrimaryBlockDescriptor>()

        override suspend fun getNPrimaryDesc(
            n: Int,
            predicate: (PrimaryBlockDescriptor) -> Boolean
        ): List<PrimaryBlockDescriptor> = listOf()
    }


}