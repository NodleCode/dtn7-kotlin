package io.nodle.dtn.bpv7.eid

import java.lang.IllegalArgumentException
import java.net.URI
import java.net.URISyntaxException

/**
 * @author Lucien Loiseau on 22/02/21.
 */
class InvalidApiEid : InvalidEid()

@Throws(InvalidDtnEid::class, InvalidApiEid::class)
fun URI.checkValidApiEid() {
    checkValidDtnEid()
    checkAuthorityNotNull()
    if (authority != "api:me") {
        throw InvalidApiEid()
    }
}

fun URI.isApiEid(): Boolean {
    return try {
        checkValidApiEid()
        true
    } catch (e: InvalidApiEid) {
        false
    } catch (e: InvalidDtnEid) {
        false
    }
}

/**
 * swapApiMeUnsafe replace the authority of uri1 with that of uri2 but keep
 * the scheme, path, query and fragment of uri1 intact.
 *
 * @param uri swap from
 * @return a URI with uri2's scheme and authority and uri1's path,query and fragment.
 * @throws InvalidDtnEid if one of the eid supplied is not a dtn-eid
 * @throws URISyntaxException if it cannot constructs a swapped eid
 */
@Throws(URISyntaxException::class, InvalidDtnEid::class)
fun URI.swapApiMe(uri: URI): URI {
    checkValidDtnEid()
    uri.checkValidDtnEid()
    return createDtnEid(uri.authority, path, query, fragment)
}

fun URI.swapApiMeUnsafe(uri: URI): URI {
    return try {
        swapApiMe(uri)
    } catch (e: URISyntaxException) {
        throw IllegalArgumentException(e)
    } catch (e: InvalidDtnEid) {
        throw IllegalArgumentException(e)
    }
}

fun apiMe(): URI {
    return URI.create("dtn://api:me/")
}

@Throws(URISyntaxException::class, InvalidDtnEid::class, InvalidApiEid::class)
fun apiMe(path: String): URI? {
    return apiMe(path, null, null)
}

@Throws(URISyntaxException::class, InvalidDtnEid::class, InvalidApiEid::class)
fun apiMe(path: String, query: String): URI? {
    return apiMe(path, query, null)
}

@Throws(URISyntaxException::class, InvalidDtnEid::class, InvalidApiEid::class)
fun apiMe(path: String, query: String?, fragment: String?): URI? {
    val uri: URI = createDtnEid("api:me", path, query, fragment)
    uri.checkValidApiEid()
    return uri
}