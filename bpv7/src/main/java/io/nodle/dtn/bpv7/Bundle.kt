package io.nodle.dtn.bpv7

import java.lang.Exception

/**
 * @author Lucien Loiseau on 12/02/21.
 */

data class Bundle(
        var primaryBlock: PrimaryBlock,
        var canonicalBlocks: MutableList<CanonicalBlock> = ArrayList())

fun Bundle.addBlock(block: CanonicalBlock): Bundle {
    if (block.blockType != BlockType.PayloadBlock.code || !hasBlock(BlockType.PayloadBlock.code)) {
        canonicalBlocks.add(block.number(canonicalBlocks.size+1))
    }
    canonicalBlocks.sortByDescending { it.blockNumber }
    return this
}

fun Bundle.hasBlock(blockNumber : Int) = canonicalBlocks.map { it.blockType }.contains(blockNumber)

fun Bundle.getBlock(blockNumber: Int) = canonicalBlocks.first { it.blockType == blockNumber }

fun Bundle.getPayloadBlock() = getBlock(BlockType.PayloadBlock.code)