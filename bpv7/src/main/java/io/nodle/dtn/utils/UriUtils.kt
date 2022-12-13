package io.nodle.dtn.utils

import java.net.URI
import java.net.URLEncoder

fun URI.addPath(path: String): URI {
    val newPath: String =
        if (path.startsWith("/"))
            path.replace("//+".toRegex(), "/")
        else if (this.path.endsWith("/"))
            this.path + path.replace("//+".toRegex(), "/")
        else
            this.path + "/" + path.replace("//+".toRegex(), "/")
    return resolve(newPath).normalize()
}

fun URI.addQueryParameter(param: String, value: String): URI {
    val query = StringBuilder(query?.let{ "$it&" }?:"")
    query.append(URLEncoder.encode(param, "UTF-8"))
    query.append('=')
    query.append(URLEncoder.encode(value, "UTF-8"))
    return URI(scheme, authority, path, query.toString(), fragment)
}