package io.nodle.dtn.storage

import androidx.room.*
import io.nodle.dtn.bpv7.FragmentID


/**
 * @author Lucien Loiseau on 15/02/21.
 */
@Dao
interface BundleEntryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(bundle: BundleEntry): Long

    @Query("SELECT EXISTS(SELECT bid FROM bundleEntry WHERE bid = :bid)")
    fun exists(bid: String): Boolean

    @Query("SELECT COUNT(bid) FROM bundleEntry")
    fun size(): Int

    @Query("DELETE FROM bundleEntry WHERE expire < :now")
    fun gc(now: Long)

    @Query("SELECT * FROM bundleEntry WHERE bid = :bid")
    fun get(bid: String): BundleEntry?

    @Query("SELECT bid FROM bundleEntry ORDER BY expire DESC ")
    fun getAllBundleIds(): List<String>

    @Query("SELECT bid FROM bundleEntry ORDER BY expire DESC LIMIT :limit")
    fun getNBundleIds(limit: Long): List<String>

    @Query("SELECT flag, destination, source, report, timestamp, sequence, offset, appdata, payload_size, constraints, tags, created, expire FROM bundleEntry ORDER BY expire DESC")
    fun getAllPrimary(): List<BundleMetadata>

    @Query("SELECT flag, destination, source, report, timestamp, sequence, offset, appdata, payload_size, constraints, tags, created, expire FROM bundleEntry ORDER BY expire DESC LIMIT :limit")
    fun getNPrimary(limit: Long): List<BundleMetadata>

    @Query("DELETE FROM bundleEntry WHERE bid = :bid")
    fun delete(bid : String)

    @Query("DELETE FROM bundleEntry")
    fun deleteAll()
}
