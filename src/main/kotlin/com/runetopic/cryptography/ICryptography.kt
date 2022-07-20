package com.runetopic.cryptography

/**
 * @author Jordan Abraham
 */
internal interface ICryptography<S, T> {
    fun from(src: S): T
    fun to(src: S): T
}
