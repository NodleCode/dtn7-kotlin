package io.nodle.dtn.bpv7

/**
 * @author Lucien Loiseau on 12/02/21.
 */
data class PayloadBlock(var buffer: ByteArray) : CanonicalBlock(BlockType.PayloadBlock.code) {
    init {
        number = 1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PayloadBlock) return false

        if (!buffer.contentEquals(other.buffer)) return false

        return true
    }

    override fun hashCode(): Int {
        return buffer.contentHashCode()
    }
}


