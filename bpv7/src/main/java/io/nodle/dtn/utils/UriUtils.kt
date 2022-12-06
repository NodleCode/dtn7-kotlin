package io.nodle.dtn.utils

import java.net.URI

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