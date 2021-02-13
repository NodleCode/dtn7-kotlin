package io.nodle.dtn.bpv7.eid

import java.net.URI
import java.net.URISyntaxException
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Lucien Loiseau on 04/09/20.
 */

class InvalidIpnEid : Exception()

const val EID_IPN_IANA_VALUE = 2
const val IPN_AUTHORITY_FORMAT = "^([0-9]+)\\.([0-9]+)$"

@Throws(InvalidIpnEid::class)
fun URI.checkIpnSchemeAndPath() {
    if (this.scheme != "ipn") {
        throw InvalidIpnEid()
    }
    if (this.path != null) {
        throw InvalidIpnEid()
    }
}

@Throws(InvalidIpnEid::class)
fun checkIpnAuthority(matcher: Matcher) {
    if (!matcher.matches()) {
        throw InvalidIpnEid()
    }
}

@Throws(InvalidIpnEid::class)
fun URI.checkValidIpnEid() {
    this.checkIpnSchemeAndPath()
    val matcher = Pattern.compile(IPN_AUTHORITY_FORMAT).matcher(this.schemeSpecificPart)
    checkIpnAuthority(matcher)
}

fun URI.isIpnEid(): Boolean {
    return try {
        this.checkValidIpnEid()
        true
    } catch (e: InvalidIpnEid) {
        false
    }
}

@Throws(InvalidIpnEid::class)
fun URI.getNodeNumber(): Int {
    this.checkIpnSchemeAndPath()
    val matcher = Pattern.compile(IPN_AUTHORITY_FORMAT).matcher(this.schemeSpecificPart)
    checkIpnAuthority(matcher)
    return matcher.group(1).toInt()
}

fun URI.getNodeNumberUnsafe(): Int {
    return try {
        this.getNodeNumber()
    } catch (e: InvalidIpnEid) {
        throw IllegalArgumentException()
    }
}

@Throws(InvalidIpnEid::class)
fun URI.getServiceNumber(): Int {
    this.checkIpnSchemeAndPath()
    val matcher = Pattern.compile(IPN_AUTHORITY_FORMAT).matcher(this.schemeSpecificPart)
    checkIpnAuthority(matcher)
    return matcher.group(2).toInt()
}

fun URI.getServiceNumberUnsafe(): Int {
    return try {
        this.getServiceNumber()
    } catch (e: InvalidIpnEid) {
        throw IllegalArgumentException()
    }
}

@Throws(URISyntaxException::class, InvalidIpnEid::class)
fun createIpn(node: Int, service: Int): URI {
    val uri = URI("ipn:$node.$service")
    uri.checkValidIpnEid()
    return uri
}
