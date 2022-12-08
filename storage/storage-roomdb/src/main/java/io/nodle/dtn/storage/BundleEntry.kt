package io.nodle.dtn.storage

import android.util.Base64
import android.util.Base64.NO_WRAP
import androidx.annotation.NonNull
import androidx.room.*
import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.cborMarshal
import io.nodle.dtn.bpv7.cborUnmarshalBundle
import io.nodle.dtn.bpv7.getPayloadSize
import io.nodle.dtn.interfaces.*

/**
 * @author Lucien Loiseau on 15/02/21.
 */
@Entity(tableName = "BundleEntry",
        indices = [
            Index(
                    value = ["bid"],
                    unique = true
            )
        ]
)
data class BundleEntry(
        @ColumnInfo(name = "bid") val bid: String,
        @ColumnInfo(name = "fid") val fid: String,
        // primaryblock (metadata)
        @ColumnInfo(name = "destination") val destination: String,
        @ColumnInfo(name = "source") val source: String,
        @ColumnInfo(name = "offset") val offset: Long,
        @ColumnInfo(name = "payload_size") val payloadSize: Long,
        @ColumnInfo(name = "appdata") val appdata: Long,
        // descriptor
        @ColumnInfo(name = "constraints") val constraints: List<String>,
        @ColumnInfo(name = "tags") val tags: List<String>,
        @ColumnInfo(name = "created") val created: Long,
        @ColumnInfo(name = "expire") val expire: Long,
        // bundle
        @ColumnInfo(name = "bundle") val bundle: Bundle
) {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "row_id", index = true)
    var id: Long = 0

    constructor(desc: BundleDescriptor) : this(
            bid = desc.ID(),
            fid = desc.fragmentedID(),

            destination = desc.bundle.primaryBlock.destination.toASCIIString(),
            source = desc.bundle.primaryBlock.source.toASCIIString(),
            offset = desc.bundle.primaryBlock.fragmentOffset,
            payloadSize = desc.bundle.getPayloadSize(),
            appdata = desc.bundle.primaryBlock.appDataLength,

            constraints = desc.constraints,
            tags = desc.tags,
            created = desc.created,
            expire = desc.expireAt(),

            bundle = desc.bundle
    )

    fun toBundleDescriptor() : BundleDescriptor {
        return BundleDescriptor(
                bundle = bundle,
                created = created,
                constraints = constraints.toMutableList(),
                tags = tags.toMutableList()).apply {
                    tags.add(BundleTag.OriginStorage.code)
        }
    }
}

fun BundleDescriptor.toBundleEntry() = BundleEntry(this)

class StringListConverter {
    @TypeConverter
    fun fromString(str: String): List<String> {
        if (str == "") {
            return listOf()
        }
        return str.split(",").map { it }
    }

    @TypeConverter
    fun toString(set: MutableList<String>): String {
        return set.joinToString(separator = ",")
    }
}

class BundleConverter {
    @TypeConverter
    fun fromBundle(bundle: Bundle): String {
        return Base64.encodeToString(bundle.cborMarshal(), NO_WRAP)
    }

    @TypeConverter
    fun toBundle(bundle: String): Bundle {
        return cborUnmarshalBundle(Base64.decode(bundle, NO_WRAP))
    }
}
