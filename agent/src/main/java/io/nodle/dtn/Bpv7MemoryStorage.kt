package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.interfaces.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Bpv7MemoryStorage() : Bpv7Storage {
    override fun init() {
    }

    override fun clearAllTables() {
    }

    override fun close() {
    }

    override val bundleStore = object : IBundleStore {
        private val mutex = Mutex()
        private var bundles = mutableMapOf<String, BundleDescriptor>()

        override suspend fun size() =
            mutex.withLock {
                bundles.size
            }

        override suspend fun gc(now: Long) =
            mutex.withLock {
                bundles = bundles.filterNot { it.value.bundle.isExpired(now) }.toMutableMap()
            }

        override suspend fun getAllBundleIds() =
            mutex.withLock {
                bundles.map { it.value.ID() }
            }

        override suspend fun get(bid: BundleID) =
            mutex.withLock {
                bundles[bid]
            }

        override suspend fun exists(bid: BundleID) =
            mutex.withLock {
                bundles.contains(bid)
            }

        override suspend fun insert(desc: BundleDescriptor) =
            mutex.withLock {
                bundles[desc.ID()] = desc
            }

        override suspend fun delete(bid: BundleID): Unit =
            mutex.withLock {
                bundles.remove(bid)
            }

        override suspend fun deleteAll() =
            mutex.withLock {
                bundles.clear()
            }

        override suspend fun getAllFragments(fragmentId: FragmentID) =
            mutex.withLock {
                bundles
                    .filter { it.value.fragmentedID() == fragmentId }
                    .map { it.value.ID() }
            }

        override suspend fun deleteAllFragments(fragmentId: FragmentID) =
            mutex.withLock {
                bundles = bundles.filterNot { it.value.fragmentedID() == fragmentId }.toMutableMap()
            }

        override suspend fun isBundleWhole(fragmentId: FragmentID): Boolean =
            mutex.withLock {
                bundles
                    .filter { it.value.fragmentedID() == fragmentId }
                    .map { it.value.bundle }
                    .sortedBy { it.primaryBlock.fragmentOffset }
                    .fold(Pair(0L, 0L)) { acc, elem ->
                        if (acc.second != elem.primaryBlock.fragmentOffset) {
                            return@isBundleWhole false
                        }
                        Pair(
                            elem.primaryBlock.appDataLength,
                            acc.second + elem.getPayloadSize()
                        )
                    }.run {
                        first == second
                    }
            }

        override suspend fun getBundleFromFragments(fragmentId: FragmentID): BundleDescriptor? =
            mutex.withLock {
                bundles
                    .filter { it.value.fragmentedID() == fragmentId }
                    .map { it.value }
                    .fold(null as BundleDescriptor?) { acc, elem ->
                        acc?.apply {
                            bundle.getPayloadBlockData().buffer += elem.bundle.getPayloadBlockData().buffer
                        } ?: elem.apply {
                            bundle.primaryBlock.unsetProcV7Flags(BundleV7Flags.IsFragment)
                        }
                    }
            }
    }
}