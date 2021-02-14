package io.nodle.dtn.utils

/**
 * @author Lucien Loiseau on 14/02/21.
 */


fun <T : Any> MutableList<T>.addElement(e : T) : MutableList<T> {
    this.add(e)
    return this
}

fun <T : Any, U : Any> MutableMap<T,U>.putElement(e : T, f : U) : MutableMap<T,U> {
    this[e] = f
    return this
}