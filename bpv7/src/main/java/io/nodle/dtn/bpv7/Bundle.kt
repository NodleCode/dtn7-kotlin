package io.nodle.dtn.bpv7

/**
 * @author Lucien Loiseau on 12/02/21.
 */

data class Bundle(
        var primaryBlock: PrimaryBlock,
        var canonicalBlocks: MutableList<CanonicalBlock> = ArrayList())

fun Bundle.addBlock(block: CanonicalBlock, pickNumber : Boolean = true): Bundle {
    if (block.blockType != BlockType.PayloadBlock.code || !hasBlockType(BlockType.PayloadBlock.code)) {
        if(pickNumber) {
            canonicalBlocks.add(block.number(canonicalBlocks.size+1))
        } else {
            canonicalBlocks.add(block)
        }
    }
    canonicalBlocks.sortByDescending { it.blockNumber }
    return this
}

fun Bundle.hasBlockType(blockType : Int) = canonicalBlocks.any{ it.blockType == blockType }

fun Bundle.hasBlockNumber(blockNumber : Int) = canonicalBlocks.any{ it.blockNumber == blockNumber }

fun Bundle.getBlockType(blockType: Int) = canonicalBlocks.first { it.blockType == blockType }

fun Bundle.getBlockNumber(blockNumber: Int) = canonicalBlocks.first { it.blockNumber == blockNumber }

fun Bundle.getPayloadBlock() = getBlockType(BlockType.PayloadBlock.code)