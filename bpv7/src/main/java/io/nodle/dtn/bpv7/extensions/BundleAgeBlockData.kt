package io.nodle.dtn.bpv7.extensions

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.*
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.utils.readLong
import java.io.OutputStream

/**
 * @author Lucien Loiseau on 14/02/21.
 */
data class BundleAgeBlockData(var age: Long) : ExtensionBlockData

fun ageBlock(age: Long): CanonicalBlock = CanonicalBlock(
        blockType = BlockType.BundleAgeBlock.code,
        data = BundleAgeBlockData(age)
)

@Throws(CborEncodingException::class)
fun BundleAgeBlockData.cborMarshalData(out: OutputStream) {
    CBORFactory().createGenerator(out).use {
        it.writeNumber(age)
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readBundleAgeBlockData(): BundleAgeBlockData {
    return BundleAgeBlockData(age = readLong())
}