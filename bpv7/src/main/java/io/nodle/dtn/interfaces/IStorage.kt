package io.nodle.dtn.interfaces

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IStorage {
    suspend fun exists(desc: BundleDescriptor): Boolean

    suspend fun saveBundle(desc: BundleDescriptor): Int

    suspend fun pullBundle(bundleId: Int): BundleDescriptor?

    suspend fun deleteBundle(bid: Int)
}