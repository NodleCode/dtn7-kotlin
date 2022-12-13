package io.nodle.dtn.storage

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.interfaces.PrimaryBlockDescriptor
import io.nodle.dtn.storage.data.BundleEntry
import io.nodle.dtn.storage.data.GetAllPrimary
import io.nodle.dtn.storage.data.GetNPrimary
import io.nodle.dtn.utils.decodeFromBase64
import io.nodle.dtn.utils.encodeToBase64
import java.net.URI
import kotlin.math.exp

fun BundleEntry.toBundleDescriptor() =
    BundleDescriptor(
        bundle = BundleConverter.toBundle(bundle),
        created = created,
        constraints = StringListConverter.fromString(constraints).toMutableList(),
        tags = StringListConverter.fromString(tags).toMutableList()
    )

fun GetAllPrimary.toPrimaryBlockDescriptor() =
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
        constraints = StringListConverter.fromString(constraints).toMutableList(),
        tags = StringListConverter.fromString(tags).toMutableList(),
        payloadSize = payload_size,
        expireAt = expire
    )

fun GetNPrimary.toPrimaryBlockDescriptor() =
    PrimaryBlockDescriptor(
        primaryBlock = PrimaryBlock(
            source = URI.create(source),
            destination = URI.create(destination),
            reportTo = URI.create(report),
            creationTimestamp = timestamp,
            sequenceNumber = sequence,
            fragmentOffset = offset,
            appDataLength = appdata
        ),
        created = created,
        constraints = StringListConverter.fromString(constraints).toMutableList(),
        tags = StringListConverter.fromString(tags).toMutableList(),
        payloadSize = payload_size,
        expireAt = expire
    )

object StringListConverter {
    fun fromString(str: String): List<String> {
        if (str == "") {
            return listOf()
        }
        return str.split(",").map { it }
    }

    fun toString(set: List<String>): String {
        return set.joinToString(separator = ",")
    }
}

object BundleConverter {
    fun fromBundle(bundle: Bundle): String {
        return bundle.cborMarshal().encodeToBase64()
    }

    fun toBundle(bundle: String): Bundle {
        return cborUnmarshalBundle(bundle.decodeFromBase64())
    }
}