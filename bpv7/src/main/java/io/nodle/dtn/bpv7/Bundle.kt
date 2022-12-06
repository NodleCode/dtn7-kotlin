package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.administrative.StatusReport
import io.nodle.dtn.bpv7.administrative.cborUnmarshalAdmnistrativeRecord

/**
 * @author Lucien Loiseau on 12/02/21.
 */

data class Bundle(
    var primaryBlock: PrimaryBlock,
    var canonicalBlocks: MutableList<CanonicalBlock> = ArrayList()
)

fun Bundle.addBlock(block: CanonicalBlock, pickNumber: Boolean = true): Bundle {
    if (block.blockType != BlockType.PayloadBlock.code || !hasBlockType(BlockType.PayloadBlock.code)) {
        if (pickNumber) {
            canonicalBlocks.add(block.number(canonicalBlocks.size + 1))
        } else {
            canonicalBlocks.add(block)
        }
    }
    canonicalBlocks.sortByDescending { it.blockNumber }
    return this
}

fun Bundle.hasBlockType(blockType: BlockType) = hasBlockType(blockType.code)

fun Bundle.hasBlockType(blockType: Int) = canonicalBlocks.any { it.blockType == blockType }

fun Bundle.hasBlockNumber(blockNumber: Int) = canonicalBlocks.any { it.blockNumber == blockNumber }

fun Bundle.getBlockType(blockType: Int) = canonicalBlocks.firstOrNull { it.blockType == blockType }

fun Bundle.getBlockNumber(blockNumber: Int) = canonicalBlocks.firstOrNull { it.blockNumber == blockNumber }

fun Bundle.getPayloadBlock() = getBlockType(BlockType.PayloadBlock.code)!!

fun Bundle.getStatusReport() {
    if (primaryBlock.isAdministiveRecord()) {
        getPayloadBlockData().buffer.run {
            cborUnmarshalAdmnistrativeRecord(this).data as StatusReport
        }
    }
}

fun Bundle.ID() = primaryBlock.ID()
fun Bundle.fragmentedID() = primaryBlock.fragmentedID()
