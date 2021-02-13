package io.nodle.dtn.utils

/**
 * @author Lucien Loiseau on 13/02/21.
 */

fun ByteArray.toHex() =
    this.joinToString(separator = "") { it.toInt().and(0xff).toString(16).padStart(2, '0') }

fun String.hexToBa() = removePrefix("0x").pureHexToBa()

fun String.pureHexToBa() = ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

fun ByteArray.trimTrailingZeros() : ByteArray = this.dropLastWhile { it == 0x00.toByte() }.toByteArray()

fun Long.isFlagSet(offset: Int) = ((0b1L shl offset) and this) > 0

fun Long.setFlag(offset: Int) = this or (0b1L shl offset)

fun Long.unsetBFlag(offset: Int) = this and (0b1L shl offset).inv()