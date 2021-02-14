package io.nodle.dtn.bpv7.eid

import java.net.URI
import java.net.URISyntaxException

/**
 * @author Lucien Loiseau on 04/09/20.
 */


fun URI.checkValidEid() {
    checkSchemeNotNull()
    when(scheme) {
        "dtn" -> checkValidDtnEid()
        "ipn" -> checkValidIpnEid()
    }
}

/**
 * return only the endpoint part of the URI, that is without the query string nor the fragment.
 * this is only valid for dtn-eid.
 *
 * @return a copy of the URI reduced to the endpoint alone.
 */
fun URI.getEndpoint(): URI {
    return if (!this.isDtnEid() || this.isNullEid()) {
        this
    } else try {
        URI(this.scheme, this.authority, this.path, null, null)
    } catch (e: URISyntaxException) {
        this
    }
}

fun URI.getDemux(): String? {
    return ((if (this.path != null) "" + this.path else "")
            + (if (this.query != null) "?" + this.query else "")
            + if (this.fragment != null) "#" + this.fragment else "")
}

fun URI.hasSameScheme(b: URI): Boolean {
    return try {
        this.checkSchemeNotNull()
        b.checkSchemeNotNull()
        this.scheme == b.scheme
    } catch (e: InvalidDtnEid) {
        false
    }
}

fun URI.hasSameAuthority(b: URI): Boolean {
    return try {
        this.checkSchemeNotNull()
        b.checkSchemeNotNull()
        this.authority == b.authority
    } catch (e: InvalidDtnEid) {
        false
    }
}

fun URI.hasSamePath(b: URI): Boolean {
    return try {
        this.checkSchemeNotNull()
        b.checkSchemeNotNull()
        this.path == b.path
    } catch (e: InvalidDtnEid) {
        false
    }
}

fun URI.matchAuthority(b: URI): Boolean {
    if (!this.hasSameScheme(b)) {
        return false
    }
    return this.hasSameAuthority(b)
}

fun URI.matchEndpoint(b: URI): Boolean {
    if (!this.hasSameScheme(b)) {
        return false
    }
    if (!this.hasSameAuthority(b)) {
        return false
    }
    return this.hasSamePath(b)
}