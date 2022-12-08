package io.nodle.dtn.storage

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.interfaces.*

class LinuxBundleStorageImpl(database: Database) : IBundleStorage {

    private val dao = database.bundleEntryQueries

    override fun size() = dao.size().executeAsOne().toInt()

    override fun gc(now: Long) {
        dao.gc(now)
    }

    override fun getAllBundleIds() = dao.getAllBundleIds().executeAsList()

    override fun get(bid: String): BundleDescriptor? {
        return dao.get(bid).executeAsOneOrNull()?.toBundleDescriptor()
    }

    override fun exists(bid: String) = dao.exists(bid).executeAsOne()

    override fun insert(desc: BundleDescriptor): Long {
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
        return dao.last_insert_rowid().executeAsOne()
    }

    override fun delete(bid: String) {
        dao.delete(bid)
    }

    override fun deleteAll() {
        dao.deleteAll()
    }

    override fun getAllFragments(fragmentId: FragmentID): List<BundleID> =
        dao.getAllFragments(fragmentId).executeAsList().map { it.bid }

    override fun isBundleWhole(fragmentId: FragmentID): Boolean {
        return dao.getAllFragments(fragmentId).executeAsList().fold(Pair(0L, 0L)) { acc, elem ->
            if(acc.second != elem.offset) {
                return@isBundleWhole false
            }
            Pair(elem.appdata, acc.second + elem.payload_size)
        }.run {
            first == second
        }
    }

    override fun getBundleFromFragments(fragmentId: FragmentID): BundleDescriptor? {
        return dao.getAllFragments(fragmentId).executeAsList().fold(null as BundleDescriptor?) { acc, elem ->
            acc?.apply {
                bundle.getPayloadBlockData().buffer += get(elem.bid)!!.bundle.getPayloadBlockData().buffer
            } ?: get(elem.bid)?.apply {
                this.bundle.primaryBlock.unsetProcV7Flags(BundleV7Flags.IsFragment)
            }
        }
    }

    override fun deleteAllFragments(fragmentId: FragmentID) = dao.deleteAllFragments(fragmentId)
}