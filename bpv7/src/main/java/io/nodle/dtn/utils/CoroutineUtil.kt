package io.nodle.dtn.utils

import kotlinx.coroutines.runBlocking


fun <T,U>T.wait(method: suspend T.() -> U) : U{
    return runBlocking {
        method()
    }
}