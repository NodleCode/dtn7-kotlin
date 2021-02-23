package io.nodle.dtn.bpv7.extensions

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.*
import io.nodle.dtn.utils.CloseProtectOutputStream
import java.io.OutputStream
import java.net.URI

/**
 * @author Lucien Loiseau on 18/02/21.
 */
data class PreviousNodeBlockData(var previous: URI) : ExtensionBlockData

fun previousNodeBlock(p : URI) : CanonicalBlock = CanonicalBlock(
        blockType = BlockType.PreviousNodeBlock.code,
        data = PreviousNodeBlockData(p)
)

fun PreviousNodeBlockData.replaceWith(eid: URI) {
    previous = eid
}

fun Bundle.getPreviousNodeBlockData() = (canonicalBlocks
        .first { it.blockType == BlockType.PreviousNodeBlock.code }
        .data as PreviousNodeBlockData)

@Throws(CborEncodingException::class)
fun PreviousNodeBlockData.cborMarshalData(out: OutputStream) {
    CBORFactory().createGenerator(CloseProtectOutputStream(out)).use {
        it.cborMarshal(previous)
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readPreviousNodeBlockData() : PreviousNodeBlockData {
    return PreviousNodeBlockData(previous = readEid())
}