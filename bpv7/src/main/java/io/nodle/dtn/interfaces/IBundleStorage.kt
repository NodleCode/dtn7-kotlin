package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.BundleID
import io.nodle.dtn.bpv7.FragmentID

interface IBundleStorage {

    // bundle specific
    fun size(): Int
    fun gc(now: Long)
    fun getAllBundleIds(): List<BundleID>
    fun get(bid: BundleID): BundleDescriptor?
    fun exists(bid: BundleID): Boolean
    fun insert(desc: BundleDescriptor): Long
    fun delete(bid: BundleID)
    fun deleteAll()

    // fragment specific
    fun getAllFragments(fragmentId: FragmentID): List<BundleID>
    fun isBundleWhole(fragmentId: FragmentID): Boolean
    fun getBundleFromFragments(fragmentId: FragmentID): BundleDescriptor?
    fun deleteAllFragments(fragmentId: FragmentID)

}