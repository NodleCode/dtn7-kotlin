package io.nodle.dtn.storage

import android.content.Context
import androidx.room.Room
import io.nodle.dtn.interfaces.IBundleStore
import io.nodle.dtn.interfaces.Bpv7Storage

class Bpv7StorageRoomDb(context: Context, inMemory: Boolean) : Bpv7Storage {
    private val sdkDb = if (inMemory) {
        Room.inMemoryDatabaseBuilder(
            context,
            Bpv7Database::class.java
        )
            .fallbackToDestructiveMigration()
            .build()
    } else {
        Room.databaseBuilder(
            context,
            Bpv7Database::class.java,
            "bpv7-db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    override fun init() {
        TODO("Not yet implemented")
    }

    override fun clearAllTables() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override val bundleStore: IBundleStore by lazy { BundleStoreRoomDb(sdkDb.bundleStore()) }

}