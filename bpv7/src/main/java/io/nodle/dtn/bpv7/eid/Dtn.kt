package io.nodle.dtn.bpv7.eid

import java.net.URI
import java.net.URISyntaxException
import java.util.*

class InvalidDtnEid : Exception()

const val EID_DTN_IANA_VALUE = 1

@Throws(InvalidDtnEid::class)
fun URI.checkAuthorityNotNull() {
    if (this.authority == null) {
        throw InvalidDtnEid()
    }
}

@Throws(InvalidDtnEid::class)
fun URI.checkSchemeSpecificPartNotNull() {
    if (this.schemeSpecificPart == null) {
        throw InvalidDtnEid()
    }
}

@Throws(InvalidDtnEid::class)
fun URI.checkPathNotNull() {
    if (this.path == null) {
        throw InvalidDtnEid()
    }
}

@Throws(InvalidDtnEid::class)
fun URI.checkSchemeNotNull() {
    if (this.scheme == null) {
        throw InvalidDtnEid()
    }
}

/**
 * a dtn-eid is of the form:
 *
 *
 * dtn-uri  = "dtn:" dtn-hier-part
 * |= "dtn:none"
 *
 *
 * dtn-hier-part = "//" node-name name-delim demux ; a path-rootless
 * node-name     = 1*VCHAR
 * name-delim    = "/"
 * demux         = *VCHAR
 *
 *
 * @param uri a dtn-eid to check
 * @throws InvalidDtnEid if the eid is invalid
 */
@Throws(InvalidDtnEid::class)
fun URI.checkValidDtnEid() {
    this.checkSchemeNotNull()
    this.checkSchemeSpecificPartNotNull()
    if (this.scheme != "dtn") {
        throw InvalidDtnEid()
    }
    if (this.schemeSpecificPart == "none") {
        return
    }
    this.checkAuthorityNotNull()
    this.checkPathNotNull()
    if (this.path == "") {
        throw InvalidDtnEid()
    }
}

fun URI.isDtnEid() : Boolean {
    return try {
        this.checkValidDtnEid()
        true
    } catch (e: InvalidDtnEid) {
        false
    }
}

fun URI.isNullEid(): Boolean {
    return this.toString() == "dtn:none"
}

fun URI.isSingleton(): Boolean {
    return !this.path.startsWith("/~")
}

fun nullDtnEid(): URI {
    return URI.create("dtn:none")
}

fun generateDtnEid(): URI {
    val uuid = UUID.randomUUID().toString().replace("-", "")
    return URI.create("dtn://$uuid/")
}

@Throws(URISyntaxException::class)
fun createDtnEid(node: String): URI {
    return URI("dtn", node, "/", null, null)
}

@Throws(URISyntaxException::class)
fun createDtnEid(node: String, path: String): URI {
    return URI("dtn", node, path, null, null)
}

@Throws(URISyntaxException::class)
fun createDtnEid(node: String, path: String, query: String): URI {
    return URI("dtn", node, path, query, null)
}

@Throws(URISyntaxException::class, InvalidDtnEid::class)
fun createDtnEid(node: String?, path: String?, query: String?, fragment: String?): URI {
    val uri = URI("dtn", node, path, query, fragment)
    uri.checkValidDtnEid()
    return uri
}