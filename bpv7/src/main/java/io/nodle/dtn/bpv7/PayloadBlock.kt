package io.nodle.dtn.bpv7

import io.nodle.dtn.utils.toHex

/**
 * @author Lucien Loiseau on 14/02/21.
 */
data class BlobBlockData(var buffer: ByteArray = byteArrayOf()) : ExtensionBlockData {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlobBlockData) return false
        if (!buffer.contentEquals(other.buffer)) return false
        return true
    }

    override fun hashCode(): Int {
        return buffer.contentHashCode()
    }

    override fun toString() = "BlobBlockData(buffer=0x${buffer.toHex()})"
}

typealias PayloadBlockData = BlobBlockData

fun payloadBlock(buffer: ByteArray) : CanonicalBlock = CanonicalBlock(
    blockType = BlockType.PayloadBlock.code,
    data = BlobBlockData(buffer)
)

fun Bundle.getPayloadBlockData() = getPayloadBlock().data as PayloadBlockData

fun Bundle.getPayloadSize() = getPayloadBlockData().buffer.size.toLong()