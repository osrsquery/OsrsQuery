package com.runetopic.cryptography.isaac

import com.runetopic.cryptography.ICryptography

/**
 * @author Jordan Abraham
 */
internal interface IISAAC : ICryptography<IntArray, IISAAC> {
    fun getNext(): Int
}
