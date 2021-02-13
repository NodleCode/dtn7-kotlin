package io.nodle.dtn.bpv7

/**
 * @author Lucien Loiseau on 12/02/21.
 */
enum class BlockType(val code: Int) {
    PayloadBlock(1),
    PreviousNodeBlock(6),
    BundleAgeBlock(7),
    HopCountBlock(10),
    SignatureBlock(196)
}

open class CanonicalBlock(
        var blockType: Int = 0,
        var number: Int = 0,
        var procV7flags: Long = 0,
        var crcType: CRCType = CRCType.NoCRC)

fun CanonicalBlock.number(n : Int) : CanonicalBlock {
    if(blockType != BlockType.PayloadBlock.code) {
        this.number = n
    } else {
        this.number = 1
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
    number = other.number
    procV7flags = other.procV7flags
    crcType = other.crcType
    return this
}





