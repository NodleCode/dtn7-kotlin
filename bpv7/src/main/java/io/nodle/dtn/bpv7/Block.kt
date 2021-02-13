package io.nodle.dtn.bpv7

/**
 * @author Lucien Loiseau on 12/02/21.
 */

enum class CRCType(val code : Int) {
    NoCRC(0), CRC16(1), CRC32(2);

    companion object {
        fun fromInt(value: Int) = CRCType.values().first { it.code == value }
    }
}

interface Block {
    fun hasCRC(): Boolean

    fun getCRCType(): CRCType

    fun setCRCType(type : CRCType)
}