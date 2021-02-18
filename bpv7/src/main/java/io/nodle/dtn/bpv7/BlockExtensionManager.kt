package io.nodle.dtn.bpv7

import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.bpsec.AbstractSecurityBlockData
import io.nodle.dtn.bpv7.bpsec.cborMarshalData
import io.nodle.dtn.bpv7.bpsec.readASBlockData
import io.nodle.dtn.bpv7.extensions.*
import io.nodle.dtn.utils.putElement
import java.io.OutputStream

/**
 * @author Lucien Loiseau on 18/02/21.
 */

var bpv7ExtensionManager = BlockExtensionManager()

typealias BlockExtensionParser = (CBORParser) -> ExtensionBlockData
typealias BlockExtensionSerializer = (ExtensionBlockData, OutputStream) -> Any

class BlockExtensionManager {
    private val extensionBlockParserRegister = HashMap<Int, BlockExtensionParser>()
            .putElement(BlockType.BlockIntegrityBlock.code, { it.readASBlockData() })
            .putElement(BlockType.BlockConfidentialityBlock.code, { it.readASBlockData() })
            .putElement(BlockType.BundleAgeBlock.code, { it.readBundleAgeBlockData() })
            .putElement(BlockType.HopCountBlock.code, { it.readHopCountBlockData() })

    private val extensionBlockMarshalerRegister = HashMap<Int, BlockExtensionSerializer>()
            .putElement(BlockType.BlockIntegrityBlock.code,
                    { data, out ->
                        (data as AbstractSecurityBlockData).cborMarshalData(out)
                    })
            .putElement(BlockType.BlockConfidentialityBlock.code,
                    { data, out ->
                        (data as AbstractSecurityBlockData).cborMarshalData(out)
                    })
            .putElement(BlockType.BundleAgeBlock.code,
                    { data, out ->
                        (data as BundleAgeBlockData).cborMarshalData(out)
                    })
            .putElement(BlockType.HopCountBlock.code,
                    { data, out ->
                        (data as HopCountBlockData).cborMarshalData(out)
                    })

    fun isKnown(blockTypeCode: Int) : Boolean =
            blockTypeCode == 1 ||
            extensionBlockParserRegister.containsKey(blockTypeCode)

    fun getExtensionParser(blockTypeCode: Int) : BlockExtensionParser? =
            extensionBlockParserRegister[blockTypeCode]

    fun getExtensionEncoder(blockTypeCode: Int) : BlockExtensionSerializer? =
            extensionBlockMarshalerRegister[blockTypeCode]

    fun addExtension(
            blockTypeCode: Int,
            parser: BlockExtensionParser,
            encoder: BlockExtensionSerializer) : Boolean {
        if(extensionBlockParserRegister.containsKey(blockTypeCode)) {
            return false
        }
        extensionBlockParserRegister.putElement(blockTypeCode, parser)
        extensionBlockMarshalerRegister.putElement(blockTypeCode, encoder)
        return true
    }
}