package io.nodle.dtn.bpv7

import java.lang.Exception

/**
 * @author Lucien Loiseau on 12/02/21.
 */

class PayloadMissingException : Exception("payload is missing")

data class Bundle(
        var primaryBlock: PrimaryBlock,
        var canonicalBlocks: MutableCollection<CanonicalBlock> = ArrayList())

fun Bundle.addBlock(block: CanonicalBlock): Bundle {
    if (block.blockType != BlockType.PayloadBlock.code || !hasPayload()) {
        canonicalBlocks.add(block.number(canonicalBlocks.size))
    }
    return this
}

fun Bundle.hasPayload() = canonicalBlocks
        .map { it.blockType }.contains(BlockType.PayloadBlock.code)

@Throws(PayloadMissingException::class)
fun Bundle.getPayload() : PayloadBlock {
    for (block in canonicalBlocks) {
        if (block.blockType == BlockType.PayloadBlock.code) {
            return block as PayloadBlock
        }
    }
    throw PayloadMissingException()
}

