package io.nodle.dtn.interfaces

interface IStorage {

    fun init()
    fun clearAllTables()
    fun close()

    val bundleStore : IBundleStorage

}