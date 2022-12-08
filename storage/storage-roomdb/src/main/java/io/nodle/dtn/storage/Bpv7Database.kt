package io.nodle.dtn.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        BundleEntry::class
    ], version = 1
)
@TypeConverters(
    StringListConverter::class,
    BundleConverter::class
)
abstract class Bpv7Database : RoomDatabase() {
    abstract fun bundleStore(): BundleEntryDao
}