package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.BundleID
import io.nodle.dtn.bpv7.FragmentID

interface IBundleStore {

    // bundle specific
    suspend fun size(): Int
    suspend fun gc(now: Long)
    suspend fun getAllBundleIds(): List<BundleID>
    suspend fun get(bid: BundleID): BundleDescriptor?
    suspend fun exists(bid: BundleID): Boolean
    suspend fun insert(desc: BundleDescriptor)
    suspend fun delete(bid: BundleID)
    suspend fun deleteAll()

    // fragment specific
    suspend fun getAllFragments(fragmentId: FragmentID): List<BundleID>
    suspend fun isBundleWhole(fragmentId: FragmentID): Boolean
    suspend fun getBundleFromFragments(fragmentId: FragmentID): BundleDescriptor?
    suspend fun deleteAllFragments(fragmentId: FragmentID)

}
