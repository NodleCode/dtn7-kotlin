package io.nodle.dtn.storage

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.nodle.dtn.interfaces.IBundleStorage
import io.nodle.dtn.interfaces.IStorage

class LinuxStorageImpl(inMemory: Boolean) : IStorage {

    private val driver = if(inMemory) {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    } else {
        JdbcSqliteDriver("jdbc:sqlite:bpv7.db")
    }

    private val database = Database(driver)

    init {
        Database.Schema.create(driver)
    }

    override fun init() {
    }

    override fun clearAllTables() {
        bundleStore.deleteAll()
    }

    override fun close() {
        driver.close()
    }

    override val bundleStore: IBundleStorage by lazy { LinuxBundleStorageImpl(database) }

}