package com.runetopic.cryptography.whirlpool

import com.runetopic.cryptography.ICryptography
import com.runetopic.cryptography.downUntil
import com.runetopic.cryptography.g8
import com.runetopic.cryptography.p8

/**
 * @author Jordan Abraham
 */
internal interface IWhirlpool : ICryptography<ByteArray, ByteArray> {
    fun getRounds(): Int
    fun getSize(): Int
    fun getBlock(): Array<LongArray>
    fun getBlockRounds(): LongArray
    fun getHash(): LongArray
    fun getBuffer(): ByteArray
    fun getBitSize(): ByteArray
    fun getPosition(): Int
    fun setPosition(position: Int)
    fun getDigestBits(): Int
    fun setDigestBits(digestBits: Int)

    fun maskWithReductionPolynomial(value: Long): Long = if (value >= 256) (value xor 0x11D) else value

    fun hash(src: ByteArray, size: Int): ByteArray {
        NEESIEAddThenFinalize(src, (src.size * 8).toLong())
        val hash = ByteArray(size)
        (0 until 8).forEach { hash.p8(it * 8, getHash()[it]) }
        return hash
    }

    private fun NEESIEAddThenFinalize(src: ByteArray, bits: Long) {
        var value = bits
        var carry = 0
        (31 downTo 0).forEach {
            carry += (getBitSize()[it].toInt() and 0xFF) + (value.toInt() and 0xFF)
            getBitSize()[it] = carry.toByte()
            carry = carry ushr 8
            value = value ushr 8
        }

        val space = (8 - (bits.toInt() and 7)) and 7
        val occupied = getDigestBits() and 7
        var srcPosition = 0
        var byte: Int
        (bits.toInt() downUntil 8 step 8).forEach { _ ->
            byte = (((src[srcPosition].toInt() shl space) and 0xFF) or ((src[srcPosition + 1].toInt() and 0xFF) ushr (8 - space)))
            getBuffer()[getPosition()] = (getBuffer()[getPosition()].toInt() or (byte ushr occupied)).toByte()
            shift(occupied, byte)
            addDigestBits(occupied)
            srcPosition++
        }

        val leftover = bits - srcPosition * 8
        byte = if (leftover > 0) (src[srcPosition].toInt() shl space) and 0xFF else 0
        if (leftover > 0) {
            getBuffer()[getPosition()] = (getBuffer()[getPosition()].toInt() or (byte ushr occupied)).toByte()
        }
        if (occupied + leftover >= 8) {
            shift(occupied, byte)
        }
        addDigestBits(leftover.toInt())
        finalize()
    }

    private fun finalize() {
        getBuffer()[getPosition()] = (getBuffer()[getPosition()].toInt() or (0x80 ushr (getDigestBits() and 7))).toByte()
        incrementPosition()
        if (getPosition() > 32) {
            while (getPosition() < 64) {
                getBuffer()[incrementPositionAndReturn()] = 0
            }
            transform()
            setPosition(0)
        }
        while (getPosition() < 32) {
            getBuffer()[incrementPositionAndReturn()] = 0
        }
        getBitSize().copyInto(getBuffer(), 32, 0, 32)
        transform()
    }

    private fun transform() {
        val block = LongArray(8)
        val state = LongArray(8)
        val roundKey = LongArray(8)
        val result = LongArray(8)

        (0 until 8).forEach {
            block[it] = getBuffer().g8(it * 8)
            roundKey[it] = getHash()[it]
            state[it] = block[it] xor roundKey[it]
        }

        (1..getRounds()).forEach { round ->
            (0 until 8).forEach {
                result[it] = 0
                (0 until 8).forEach { index ->
                    val offset = 56 - (8 * index)
                    result[it] = result[it] xor getBlock()[index][(roundKey[(it - index) and 7] ushr offset).toInt() and 0xFF]
                }
            }
            result.copyInto(roundKey)
            roundKey[0] = roundKey[0] xor getBlockRounds()[round]
            (0 until 8).forEach {
                result[it] = roundKey[it]
                (0 until 8).forEach { index ->
                    val offset = 56 - (8 * index)
                    result[it] = result[it] xor getBlock()[index][(state[(it - index) and 7] ushr offset).toInt() and 0xFF]
                }
            }
            result.copyInto(state)
        }

        (0 until 8).forEach {
            getHash()[it] = getHash()[it] xor state[it] xor block[it]
        }
    }

    private fun shift(offset: Int, int: Int) {
        incrementPosition()
        addDigestBits(8 - offset)
        if (getDigestBits() == 512) {
            transform()
            setDigestBits(0)
            setPosition(0)
        }
        getBuffer()[getPosition()] = ((int shl (8 - offset)) and 0xFF).toByte()
    }

    private fun addDigestBits(amount: Int) {
        setDigestBits(getDigestBits() + amount)
    }

    private fun incrementPosition() {
        setPosition(getPosition() + 1)
    }

    private fun incrementPositionAndReturn(): Int {
        val position = getPosition()
        setPosition(position + 1)
        return position
    }
}
