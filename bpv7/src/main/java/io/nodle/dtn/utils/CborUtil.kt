package io.nodle.dtn.utils

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.CborParsingException
import java.io.ByteArrayOutputStream

/**
 * @author Lucien Loiseau on 15/02/21.
 */


@Throws(CborParsingException::class)
fun CBORParser.readStartArray() {
    if (nextToken() != JsonToken.START_ARRAY) {
        throw CborParsingException("expected start array but got $currentName")
    }
}

@Throws(CborParsingException::class)
fun CBORParser.assertStartArray() {
    if (!isExpectedStartArrayToken) {
        throw CborParsingException("expected start array but got $currentName")
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readCloseArray() {
    if (nextToken() != JsonToken.END_ARRAY) {
        throw CborParsingException("expected end array but got $currentName")
    }
}

@Throws(CborParsingException::class)
fun <T : Any> CBORParser.readArray(
    prefetch: Boolean,
    elementParser: (CBORParser) -> T
): MutableList<T> {
    if (!prefetch) {
        readStartArray()
    } else {
        assertStartArray()
    }

    val ret = ArrayList<T>()
    while (true) {
        if (nextToken() == JsonToken.END_ARRAY) {
            break
        }
        ret.add(elementParser(this))
    }
    return ret
}

fun <T : Any> CBORParser.readArray(elementParser: (CBORParser) -> T): MutableList<T> =
    readArray(false, elementParser)

@Throws(CborParsingException::class)
fun <T : Any> CBORParser.readStruct(prefetch: Boolean, elementParser: (CBORParser) -> T): T {
    if (!prefetch) {
        readStartArray()
    } else {
        assertStartArray()
    }

    try {
        return elementParser(this)
    } finally {
        readCloseArray()
    }
}

@Throws(CborParsingException::class)
fun <T : Any> CBORParser.readStruct(elementParser: (CBORParser) -> T): T =
    readStruct(false, elementParser)

@Throws(CborParsingException::class)
fun CBORParser.readBoolean(): Boolean {
    val t = nextToken()
    if (!t.isBoolean) {
        throw CborParsingException("expected boolean but got $currentName")
    }
    return booleanValue
}


@Throws(CborParsingException::class)
fun CBORParser.readInt(): Int {
    val t = nextToken()
    if (!t.isNumeric) {
        throw CborParsingException("expected number but got $currentName")
    }
    return intValue
}

@Throws(CborParsingException::class)
fun CBORParser.readLong(): Long {
    val t = nextToken()
    if (!t.isNumeric) {
        throw CborParsingException("expected number but got $currentName")
    }
    return longValue
}


@Throws(CborParsingException::class)
fun CBORParser.readString(): String {
    if (nextToken() != JsonToken.VALUE_STRING) {
        throw CborParsingException("expected string but got $currentName")
    }
    return text
}

@Throws(CborParsingException::class)
fun CBORParser.readByteArray(): ByteArray {
    if (nextToken() != JsonToken.VALUE_EMBEDDED_OBJECT) {
        throw CborParsingException("expected byte array but got $currentName")
    }
    val buffer = ByteArrayOutputStream()
    readBinaryValue(buffer)
    return buffer.toByteArray()
}