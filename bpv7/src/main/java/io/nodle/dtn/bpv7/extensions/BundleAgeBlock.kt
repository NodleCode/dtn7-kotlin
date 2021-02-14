package io.nodle.dtn.bpv7.extensions

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.*
import java.io.OutputStream

/**
 * @author Lucien Loiseau on 14/02/21.
 */
data class BundleAgeBlockData(var age: Long) : ExtensionBlockData

fun ageBlock(age : Long) : CanonicalBlock = CanonicalBlock(
    blockType = BlockType.BundleAgeBlock.code,
    data = BundleAgeBlockData(age)
)

@Throws(CborEncodingException::class)
fun BundleAgeBlockData.cborMarshalData(out: OutputStream) {
    val gen = CBORFactory().createGenerator(out)
    gen.writeNumber(age)
    gen.flush()
}

@Throws(CborParsingException::class)
fun CBORParser.readBundleAgeBlockData() : BundleAgeBlockData {
    return BundleAgeBlockData(readLong())
}