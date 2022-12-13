package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.interfaces.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.URI

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

        override suspend fun getNBundleIds(n: Int): List<BundleID> =
            mutex.withLock {
                bundles.map { it.value.ID() }.take(n)
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

        override suspend fun getAllPrimaryDesc(predicate: (PrimaryBlockDescriptor) -> Boolean): List<PrimaryBlockDescriptor> =
            mutex.withLock {
                bundles.map {
                    PrimaryBlockDescriptor(
                        primaryBlock = it.value.bundle.primaryBlock.run {
                            PrimaryBlock(
                                procV7Flags = procV7Flags,
                                source = source,
                                destination = destination,
                                reportTo = reportTo,
                                creationTimestamp = creationTimestamp,
                                sequenceNumber = sequenceNumber,
                                fragmentOffset = fragmentOffset,
                                appDataLength = appDataLength
                            )
                        },
                        created = it.value.created,
                        constraints = it.value.constraints.toMutableList(),
                        tags = it.value.tags.toMutableList(),
                        payloadSize = it.value.bundle.getPayloadSize(),
                        expireAt = it.value.expireAt()
                    )
                }.filter(predicate)
            }

        override suspend fun getNPrimaryDesc(
            n: Int,
            predicate: (PrimaryBlockDescriptor) -> Boolean
        ): List<PrimaryBlockDescriptor> =
            getAllPrimaryDesc(predicate)
                .take(n)

    }
}