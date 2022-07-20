package com.runetopic.cryptography.xtea

import com.runetopic.cryptography.ICryptography
import java.nio.ByteBuffer

/**
 * @author Jordan Abraham
 */
internal interface IXTEA : ICryptography<ByteBuffer, ByteArray> {
    fun getRounds(): Int
    fun getKeys(): IntArray
}
