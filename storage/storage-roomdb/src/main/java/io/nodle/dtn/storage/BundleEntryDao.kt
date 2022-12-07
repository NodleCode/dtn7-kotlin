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

    @Query("SELECT bid, offset, payload_size, appdata FROM bundleEntry WHERE fid = :fragmentID  ORDER BY `offset` ASC")
    fun getAllFragments(fragmentID: FragmentID): List<FragmentTuple>

    @Query("DELETE FROM bundleEntry WHERE bid = :bid")
    fun delete(bid : String)

    @Query("DELETE FROM bundleEntry")
    fun deleteAll()

    @Query("DELETE FROM bundleEntry WHERE fid = :fragmentID")
    fun deleteAllFragments(fragmentID: FragmentID)
}

class FragmentTuple(
    @ColumnInfo(name = "bid")
    var bid: String,

    @ColumnInfo(name = "offset")
    var offset: Long,

    @ColumnInfo(name = "payload_size")
    var payload_size: Long,

    @ColumnInfo(name = "appdata")
    var appdata: Long,
)