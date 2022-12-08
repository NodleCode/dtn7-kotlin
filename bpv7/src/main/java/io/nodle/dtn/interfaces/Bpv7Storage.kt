package io.nodle.dtn.interfaces

interface Bpv7Storage {

    fun init()
    fun clearAllTables()
    fun close()

    val bundleStore : IBundleStore

}