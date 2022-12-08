package io.nodle.dtn.utils

import java.util.*


fun ByteArray.encodeToBase64() : String {
    return Base64.getEncoder().encodeToString(this)
}

fun String.decodeFromBase64() : ByteArray {
    return Base64.getDecoder().decode(this)
}