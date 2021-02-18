package io.nodle.dtn.interfaces

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IStorage {
    suspend fun exists(bundleId: String): Boolean

    suspend fun saveBundle(desc: BundleDescriptor): String

    suspend fun pullBundle(bundleId: String): BundleDescriptor?

    suspend fun deleteBundle(bundleId: String)
}