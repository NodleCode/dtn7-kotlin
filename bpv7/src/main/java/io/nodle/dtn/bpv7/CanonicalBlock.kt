package io.nodle.dtn.bpv7

import io.nodle.dtn.utils.LastBufferOutputStream

/**
 * @author Lucien Loiseau on 12/02/21.
 */
enum class BlockType(val code: Int) {
    PayloadBlock(1),
    BundleAuthenticationBlock(2),
    PayloadIntegrityBlock(3),
    PayloadConfidentialityBlock(4),
    PreviousHopInsertionBlock(5),
    PreviousNodeBlock(6),
    BundleAgeBlock(7),
    MetadataExtensionBlock(8),
    ExtensionSecurityBlock(9),
    HopCountBlock(10),
    BlockIntegrityBlock(40),
    BlockConfidentialityBlock(41),
}

data class CanonicalBlock(
    var blockType: Int = 0,
    var blockNumber: Int = 0,
    var procV7flags: Long = 0,
    var crcType: CRCType = CRCType.NoCRC,
    var data : ExtensionBlockData = BlobBlockData())

interface ExtensionBlockData

fun CanonicalBlock.number(n : Int) : CanonicalBlock {
    if(blockType != BlockType.PayloadBlock.code) {
        this.blockNumber = n
    } else {
        this.blockNumber = 1
    }
    return this
}

fun CanonicalBlock.procV7Flags(flags : Long) : CanonicalBlock {
    this.procV7flags = flags
    return this
}

fun CanonicalBlock.crcType(type : CRCType) : CanonicalBlock {
    this.crcType = type
    return this
}

fun CanonicalBlock.cloneHeader(other : CanonicalBlock) : CanonicalBlock {
    blockType = other.blockType
    blockNumber = other.blockNumber
    procV7flags = other.procV7flags
    crcType = other.crcType
    return this
}

fun CanonicalBlock.checkCRC(crc : ByteArray) : Boolean {
    val buf = when(crcType) {
        CRCType.CRC16 -> LastBufferOutputStream(2)
        CRCType.CRC32 -> LastBufferOutputStream(4)
        else -> return true
    }
    cborMarshal(buf)
    return buf.last().contentEquals(crc)
}




