@file:JvmName("CryptoExtensions")
package com.runetopic.cryptography

import com.runetopic.cryptography.huffman.Huffman
import com.runetopic.cryptography.isaac.ISAAC
import com.runetopic.cryptography.whirlpool.Whirlpool
import com.runetopic.cryptography.xtea.XTEA
import java.nio.ByteBuffer

/**
 * @author Jordan Abraham
 */
infix fun Int.downUntil(to: Int): IntProgression {
    if (to >= Int.MAX_VALUE) return IntRange.EMPTY
    return this downTo (to + 1)
}

fun ByteArray.decompressHuffman(huffman: Huffman, length: Int, maxLength: Int = 75): String {
    var actualLength = length
    if (actualLength > maxLength) actualLength = maxLength
    val decompressed = ByteArray(256)
    huffman.decompress(this, decompressed, actualLength)
    return String(decompressed, 0, actualLength)
}

fun String.compressHuffman(huffman: Huffman, dest: ByteArray): Int = huffman.compress(this, dest)

fun ByteArray.fromXTEA(rounds: Int, keys: IntArray = IntArray(4), offset: Int = 0): ByteArray = XTEA(rounds, keys, offset).from(
    ByteBuffer.wrap(this)
)
fun ByteArray.toXTEA(rounds: Int, keys: IntArray = IntArray(4), offset: Int = 0): ByteArray = XTEA(rounds, keys, offset).to(
    ByteBuffer.wrap(this)
)

fun ByteArray.toWhirlpool(rounds: Int = 10, size: Int = 64): ByteArray = Whirlpool(rounds, size).to(this)

fun IntArray.toISAAC() = ISAAC().to(this)

internal fun ByteArray.g8(offset: Int): Long {
    return (
        /*******/(this[offset].toLong() and 0xFF shl 56)
            or ((this[offset + 1].toLong() and 0xFF) shl 48)
            or ((this[offset + 2].toLong() and 0xFF) shl 40)
            or ((this[offset + 3].toLong() and 0xFF) shl 32)
            or ((this[offset + 4].toLong() and 0xFF) shl 24)
            or ((this[offset + 5].toLong() and 0xFF) shl 16)
            or ((this[offset + 6].toLong() and 0xFF) shl 8)
            or (this[offset + 7].toLong() and 0xFF)
        )
}

internal fun ByteArray.p8(offset: Int, long: Long) {
    this[offset/**/] = (long shr 56).toByte()
    this[offset + 1] = (long shr 48).toByte()
    this[offset + 2] = (long shr 40).toByte()
    this[offset + 3] = (long shr 32).toByte()
    this[offset + 4] = (long shr 24).toByte()
    this[offset + 5] = (long shr 16).toByte()
    this[offset + 6] = (long shr 8).toByte()
    this[offset + 7] = long.toByte()
}
