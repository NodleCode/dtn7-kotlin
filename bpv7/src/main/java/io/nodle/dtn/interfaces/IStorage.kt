package io.nodle.dtn.interfaces

import io.nodle.dtn.BundleDescriptor

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IStorage {

    suspend fun saveBundle(bundle: BundleDescriptor): Int

    suspend fun pullBundle(bundleId: Int): BundleDescriptor

}