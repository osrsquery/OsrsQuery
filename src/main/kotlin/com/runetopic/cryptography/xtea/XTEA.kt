package com.runetopic.cryptography.xtea

import java.nio.ByteBuffer

/**
 * @author Jordan Abraham
 */
internal class XTEA(
    private val rounds: Int,
    private val keys: IntArray = IntArray(4),
    private val offset: Int
) : IXTEA {
    override fun getRounds(): Int = rounds
    override fun getKeys(): IntArray = keys

    override fun from(src: ByteBuffer): ByteArray {
        val position = src.position()
        src.position(offset)
        val count = (src.array().size - offset) / 8
        (0 until count).forEach { _ ->
            var v0 = src.int
            var v1 = src.int
            var sum = (DELTA * rounds)
            (0 until rounds).forEach { _ ->
                v1 -= (v0 shl 4 xor (v0 ushr 5)) + v0 xor sum + keys[sum ushr 11 and 3]
                sum -= DELTA
                v0 -= (v1 shl 4 xor (v1 ushr 5)) + v1 xor sum + keys[sum and 3]
            }
            src.position(src.position() - 8)
            src.putInt(v0)
            src.putInt(v1)
        }
        src.position(position)
        return src.array()
    }

    override fun to(src: ByteBuffer): ByteArray {
        val position = src.position()
        src.position(offset)
        val count = (src.array().size - offset) / 8
        (0 until count).forEach { _ ->
            var v0 = src.int
            var v1 = src.int
            var sum = 0
            (0 until rounds).forEach { _ ->
                v0 += (v1 shl 4 xor (v1 ushr 5)) + v1 xor sum + keys[sum and 3]
                sum += DELTA
                v1 += (v0 shl 4 xor (v0 ushr 5)) + v0 xor sum + keys[sum ushr 11 and 3]
            }
            src.position(src.position() - 8)
            src.putInt(v0)
            src.putInt(v1)
        }
        src.position(position)
        return src.array()
    }

    internal companion object {
        const val DELTA = -0x61C88647
    }
}
