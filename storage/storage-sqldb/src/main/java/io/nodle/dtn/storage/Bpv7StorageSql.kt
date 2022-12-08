package io.nodle.dtn.storage

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.nodle.dtn.interfaces.IBundleStore
import io.nodle.dtn.interfaces.Bpv7Storage
import io.nodle.dtn.utils.wait

class Bpv7StorageSql(inMemory: Boolean) : Bpv7Storage {

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
        bundleStore.wait{ deleteAll() }
    }

    override fun close() {
        driver.close()
    }

    override val bundleStore: IBundleStore by lazy { BundleStoreSql(database) }

}