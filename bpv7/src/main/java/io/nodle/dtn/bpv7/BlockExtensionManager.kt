package io.nodle.dtn.bpv7

import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.bpsec.AbstractSecurityBlockData
import io.nodle.dtn.bpv7.bpsec.cborMarshalData
import io.nodle.dtn.bpv7.bpsec.readASBlockData
import io.nodle.dtn.bpv7.extensions.*
import java.io.OutputStream

/**
 * @author Lucien Loiseau on 18/02/21.
 */
typealias BlockExtensionParser = (CBORParser) -> ExtensionBlockData
typealias BlockExtensionSerializer = (ExtensionBlockData, OutputStream) -> Any

private val extensionBlockParserRegister = hashMapOf<Int, BlockExtensionParser>().apply {
    put(BlockType.BlockIntegrityBlock.code) {
        it.readASBlockData()
    }
    put(BlockType.BlockConfidentialityBlock.code) {
        it.readASBlockData()
    }
    put(BlockType.BundleAgeBlock.code) {
        it.readBundleAgeBlockData()
    }
    put(BlockType.HopCountBlock.code) {
        it.readHopCountBlockData()
    }
    put(BlockType.PreviousNodeBlock.code) {
        it.readPreviousNodeBlockData()
    }
}

private val extensionBlockMarshallerRegister = hashMapOf<Int, BlockExtensionSerializer>().apply {
    put(BlockType.BlockIntegrityBlock.code) { data, out ->
        (data as AbstractSecurityBlockData).cborMarshalData(out)
    }
    put(BlockType.BlockConfidentialityBlock.code) { data, out ->
        (data as AbstractSecurityBlockData).cborMarshalData(out)
    }
    put(BlockType.BundleAgeBlock.code) { data, out ->
        (data as BundleAgeBlockData).cborMarshalData(out)
    }
    put(BlockType.HopCountBlock.code) { data, out ->
        (data as HopCountBlockData).cborMarshalData(out)
    }
    put(BlockType.PreviousNodeBlock.code) { data, out ->
        (data as PreviousNodeBlockData).cborMarshalData(out)
    }
}

fun isBpv7BlockExtensionKnown(blockTypeCode: Int): Boolean =
    blockTypeCode == 1 || extensionBlockParserRegister.containsKey(blockTypeCode)

fun getBpv7BlockExtensionParser(blockTypeCode: Int): BlockExtensionParser? =
    extensionBlockParserRegister[blockTypeCode]

fun getBpv7BlockExtensionEncoder(blockTypeCode: Int): BlockExtensionSerializer? =
    extensionBlockMarshallerRegister[blockTypeCode]

fun addBpv7BlockExtension(
    blockTypeCode: Int,
    parser: BlockExtensionParser,
    encoder: BlockExtensionSerializer
): Boolean {
    val ret = (extensionBlockParserRegister.containsKey(blockTypeCode))
    extensionBlockParserRegister[blockTypeCode] = parser
    extensionBlockMarshallerRegister[blockTypeCode] = encoder
    return ret
}