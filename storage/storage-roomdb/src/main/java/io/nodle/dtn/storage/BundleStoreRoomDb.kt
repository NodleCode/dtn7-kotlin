package io.nodle.dtn.storage

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.interfaces.*

class BundleStoreRoomDb(private val bundleEntryDao: BundleEntryDao) : IBundleStore {
    override suspend fun size() = bundleEntryDao.size()
    override suspend fun gc(now: Long) = bundleEntryDao.gc(now)
    override suspend fun getAllBundleIds() = bundleEntryDao.getAllBundleIds()
    override suspend fun getNBundleIds(n: Int): List<BundleID> =
        bundleEntryDao.getNBundleIds(n.toLong())

    override suspend fun get(bid: BundleID) = bundleEntryDao.get(bid)?.toBundleDescriptor()
    override suspend fun exists(bid: BundleID) = bundleEntryDao.exists(bid)
    override suspend fun insert(desc: BundleDescriptor) {
        bundleEntryDao.insert(BundleEntry(desc))
    }

    override suspend fun delete(bid: BundleID) = bundleEntryDao.delete(bid)
    override suspend fun deleteAll() = bundleEntryDao.deleteAll()

    override suspend fun getAllPrimaryDesc(
        predicate: (PrimaryBlockDescriptor) -> Boolean
    ): List<PrimaryBlockDescriptor> =
        bundleEntryDao.getAllPrimary()
            .map { it.toPrimaryBlockDescriptor() }
            .filter(predicate)

    override suspend fun getNPrimaryDesc(
        n: Int,
        predicate: (PrimaryBlockDescriptor) -> Boolean
    ): List<PrimaryBlockDescriptor> =
        bundleEntryDao.getNPrimary(n.toLong())
            .map { it.toPrimaryBlockDescriptor() }
            .filter(predicate)


}