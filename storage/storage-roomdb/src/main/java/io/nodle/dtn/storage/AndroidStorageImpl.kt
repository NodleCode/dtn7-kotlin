package io.nodle.dtn.storage

import android.content.Context
import androidx.room.Room
import io.nodle.dtn.interfaces.IBundleStorage
import io.nodle.dtn.interfaces.IStorage

class AndroidStorageImpl(context: Context, inMemory: Boolean) : IStorage {
    private val sdkDb = if (inMemory) {
        Room.inMemoryDatabaseBuilder(
            context,
            AndroidDatabase::class.java
        )
            .fallbackToDestructiveMigration()
            .build()
    } else {
        Room.databaseBuilder(
            context,
            AndroidDatabase::class.java,
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

    override val bundleStore: IBundleStorage by lazy { AndroidBundleStorageImpl(sdkDb.bundleStore()) }

}