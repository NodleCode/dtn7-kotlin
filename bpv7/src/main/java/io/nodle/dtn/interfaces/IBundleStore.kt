package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.*

interface IBundleStore {

    suspend fun size(): Int
    suspend fun gc(now: Long)
    suspend fun getAllBundleIds(): List<BundleID>
    suspend fun getNBundleIds(n: Int): List<BundleID>
    suspend fun get(bid: BundleID): BundleDescriptor?
    suspend fun exists(bid: BundleID): Boolean
    suspend fun insert(desc: BundleDescriptor)
    suspend fun delete(bid: BundleID)
    suspend fun deleteAll()

    suspend fun getAllPrimaryDesc(
        predicate: (PrimaryBlockDescriptor) -> Boolean
    ): List<PrimaryBlockDescriptor>

    suspend fun getNPrimaryDesc(
        n: Int, predicate: (PrimaryBlockDescriptor) -> Boolean
    ): List<PrimaryBlockDescriptor>

}

suspend fun IBundleStore.deleteAll(predicate: (PrimaryBlockDescriptor) -> Boolean) {
    getAllPrimaryDesc(predicate).map {
        delete(it.ID())
    }
}

suspend fun IBundleStore.getAllFragments(fragmentId: FragmentID): List<PrimaryBlockDescriptor> =
    getAllPrimaryDesc {
        it.primaryBlock.isFlagSet(BundleV7Flags.IsFragment) && it.fragmentedID() == fragmentId
    }.sortedBy {
        it.primaryBlock.fragmentOffset
    }

suspend fun IBundleStore.isBundleWhole(fragmentId: FragmentID): Boolean =
    getAllFragments(fragmentId)
        .fold(Pair(0L, 0L)) { acc, elem ->
            if (acc.second != elem.primaryBlock.fragmentOffset) {
                return@isBundleWhole false
            }
            Pair(elem.primaryBlock.appDataLength, acc.second + elem.payloadSize)
        }.run {
            first == second
        }

suspend fun IBundleStore.getBundleFromFragments(fragmentId: FragmentID): BundleDescriptor? =
    getAllFragments(fragmentId)
        .fold(null as BundleDescriptor?) { acc, elem ->
            acc?.apply {
                bundle.getPayloadBlockData().buffer += get(elem.ID())!!.bundle.getPayloadBlockData().buffer
            } ?: get(elem.ID())?.apply {
                this.bundle.primaryBlock.unsetProcV7Flags(BundleV7Flags.IsFragment)
            }
        }

suspend fun IBundleStore.deleteAllFragments(fragmentId: FragmentID) =
    deleteAll { it.fragmentedID() == fragmentId }
