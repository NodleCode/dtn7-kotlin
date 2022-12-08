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

    override suspend fun get(bid: String): BundleDescriptor? {
        return dao.get(bid).executeAsOneOrNull()?.toBundleDescriptor()
    }

    override suspend fun exists(bid: String) = dao.exists(bid).executeAsOne()

    override suspend fun insert(desc: BundleDescriptor) {
        with(desc) {
            dao.insert(
                ID(),
                fragmentedID(),
                bundle.primaryBlock.destination.toASCIIString(),
                bundle.primaryBlock.source.toASCIIString(),
                bundle.primaryBlock.fragmentOffset,
                bundle.getPayloadSize(),
                bundle.primaryBlock.appDataLength,
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

    override suspend fun getAllFragments(fragmentId: FragmentID): List<BundleID> =
        dao.getAllFragments(fragmentId).executeAsList().map { it.bid }

    override suspend fun isBundleWhole(fragmentId: FragmentID): Boolean {
        return dao.getAllFragments(fragmentId).executeAsList().fold(Pair(0L, 0L)) { acc, elem ->
            if(acc.second != elem.offset) {
                return@isBundleWhole false
            }
            Pair(elem.appdata, acc.second + elem.payload_size)
        }.run {
            first == second
        }
    }

    override suspend fun getBundleFromFragments(fragmentId: FragmentID): BundleDescriptor? {
        return dao.getAllFragments(fragmentId).executeAsList().fold(null as BundleDescriptor?) { acc, elem ->
            acc?.apply {
                bundle.getPayloadBlockData().buffer += get(elem.bid)!!.bundle.getPayloadBlockData().buffer
            } ?: get(elem.bid)?.apply {
                this.bundle.primaryBlock.unsetProcV7Flags(BundleV7Flags.IsFragment)
            }
        }
    }

    override suspend fun deleteAllFragments(fragmentId: FragmentID) = dao.deleteAllFragments(fragmentId)
}