package io.nodle.dtn.storage

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.cborMarshal
import io.nodle.dtn.bpv7.cborUnmarshalBundle
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.storage.data.BundleEntry
import io.nodle.dtn.utils.Base64.decodeFromBase64
import io.nodle.dtn.utils.Base64.encodeToBase64

fun BundleEntry.toBundleDescriptor() =
    BundleDescriptor(
        bundle = BundleConverter.toBundle(bundle),
        created = created,
        constraints = StringListConverter.fromString(constraints).toMutableList(),
        tags = StringListConverter.fromString(tags).toMutableList()
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