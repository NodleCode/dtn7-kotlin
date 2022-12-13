package io.nodle.dtn.storage

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.interfaces.*

class BundleStoreSql(database: Database) : IBundleStore {

    private val dao = database.bundleEntryQueries

    override suspend fun size() = dao.size().executeAsOne().toInt()

    override suspend fun gc(now: Long) {
        dao.gc(now)
    }

    override suspend fun getAllBundleIds() = dao.getAllBundleIds().executeAsList()

    override suspend fun getNBundleIds(n: Int): List<BundleID> =
        dao.getNBundleIds(n.toLong()).executeAsList()

    override suspend fun get(bid: String): BundleDescriptor? {
        return dao.get(bid).executeAsOneOrNull()?.toBundleDescriptor()
    }

    override suspend fun exists(bid: String) = dao.exists(bid).executeAsOne()

    override suspend fun insert(desc: BundleDescriptor) {
        with(desc) {
            dao.insert(
                ID(),
                bundle.primaryBlock.procV7Flags,
                bundle.primaryBlock.destination.toASCIIString(),
                bundle.primaryBlock.source.toASCIIString(),
                bundle.primaryBlock.reportTo.toASCIIString(),
                bundle.primaryBlock.creationTimestamp,
                bundle.primaryBlock.sequenceNumber,
                bundle.primaryBlock.lifetime,
                bundle.primaryBlock.fragmentOffset,
                bundle.primaryBlock.appDataLength,
                bundle.getPayloadSize(),
                StringListConverter.toString(constraints),
                StringListConverter.toString(tags),
                created,
                expireAt(),
                BundleConverter.fromBundle(bundle)
            )
        }
        dao.last_insert_rowid().executeAsOne()
    }

    override suspend fun delete(bid: String) {
        dao.delete(bid)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun getAllPrimaryDesc(
        predicate: (PrimaryBlockDescriptor) -> Boolean
    ): List<PrimaryBlockDescriptor> =
        dao.getAllPrimary().executeAsList()
            .map {
                it.toPrimaryBlockDescriptor()
            }
            .filter(predicate)

    override suspend fun getNPrimaryDesc(
        n: Int,
        predicate: (PrimaryBlockDescriptor) -> Boolean
    ): List<PrimaryBlockDescriptor> =
        dao.getNPrimary(n.toLong()).executeAsList()
            .map {
                it.toPrimaryBlockDescriptor()
            }
            .filter(predicate)
}