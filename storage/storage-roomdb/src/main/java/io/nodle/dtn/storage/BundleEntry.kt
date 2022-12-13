package io.nodle.dtn.storage

import android.util.Base64
import android.util.Base64.NO_WRAP
import androidx.annotation.NonNull
import androidx.room.*
import io.nodle.dtn.bpv7.*
import io.nodle.dtn.interfaces.*
import java.net.URI

/**
 * @author Lucien Loiseau on 15/02/21.
 */
@Entity(
    tableName = "BundleEntry",
    indices = [
        Index(
            value = ["bid"],
            unique = true
        )
    ]
)
data class BundleEntry(
    @ColumnInfo(name = "bid") val bid: String,

    // primaryblock (metadata)
    @ColumnInfo(name = "flag") val flag: Long,
    @ColumnInfo(name = "destination") val destination: String,
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "report") val report: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "sequence") val sequence: Long,
    @ColumnInfo(name = "lifetime") val lifetime: Long,
    @ColumnInfo(name = "offset") val offset: Long,
    @ColumnInfo(name = "appdata") val appdata: Long,
    @ColumnInfo(name = "payload_size") val payloadSize: Long,

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

        flag = desc.bundle.primaryBlock.procV7Flags,
        destination = desc.bundle.primaryBlock.destination.toASCIIString(),
        source = desc.bundle.primaryBlock.source.toASCIIString(),
        report = desc.bundle.primaryBlock.reportTo.toASCIIString(),
        timestamp = desc.bundle.primaryBlock.creationTimestamp,
        sequence = desc.bundle.primaryBlock.sequenceNumber,
        lifetime = desc.bundle.primaryBlock.lifetime,
        offset = desc.bundle.primaryBlock.fragmentOffset,
        appdata = desc.bundle.primaryBlock.appDataLength,
        payloadSize = desc.bundle.getPayloadSize(),

        constraints = desc.constraints,
        tags = desc.tags,
        created = desc.created,
        expire = desc.expireAt(),

        bundle = desc.bundle
    )

    fun toBundleDescriptor(): BundleDescriptor {
        return BundleDescriptor(
            bundle = bundle,
            created = created,
            constraints = constraints.toMutableList(),
            tags = tags.toMutableList(),
        ).apply {
            tags.add(BundleTag.OriginStorage.code)
        }
    }
}

class BundleMetadata(
    @ColumnInfo(name = "flag")
    var flag: Long,
    @ColumnInfo(name = "source")
    var source: String,
    @ColumnInfo(name = "destination")
    var destination: String,
    @ColumnInfo(name = "report")
    var report: String,
    @ColumnInfo(name = "timestamp")
    var timestamp: Long,
    @ColumnInfo(name = "sequence")
    var sequence: Long,
    @ColumnInfo(name = "offset")
    var offset: Long,
    @ColumnInfo(name = "appdata")
    var appdata: Long,
    @ColumnInfo(name = "payload_size")
    var payloadSize: Long,
    @ColumnInfo(name = "constraints")
    var constraints: List<String>,
    @ColumnInfo(name = "tags")
    var tags: List<String>,
    @ColumnInfo(name = "created")
    var created: Long,
    @ColumnInfo(name = "expire")
    var expire: Long,
)

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

fun BundleMetadata.toPrimaryBlockDescriptor() : PrimaryBlockDescriptor =
    PrimaryBlockDescriptor(
        primaryBlock = PrimaryBlock(
            procV7Flags = flag,
            source = URI.create(source),
            destination = URI.create(destination),
            reportTo = URI.create(report),
            creationTimestamp = timestamp,
            sequenceNumber = sequence,
            fragmentOffset = offset,
            appDataLength = appdata
        ),
        created = created,
        constraints = constraints.toMutableList(),
        tags = tags.toMutableList(),
        payloadSize = payloadSize,
        expireAt = expire
    ).apply {
        tags.add(BundleTag.OriginStorage.code)
    }


