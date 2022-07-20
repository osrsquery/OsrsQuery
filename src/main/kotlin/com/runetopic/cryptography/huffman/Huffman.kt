package com.runetopic.cryptography.huffman

class Huffman(
    private val sizes: ByteArray
) {
    private var masks: IntArray = IntArray(sizes.size)
    private var keys: IntArray = IntArray(8)

    init {
        val values = IntArray(33)
        var key = 0

        for (index in sizes.indices) {
            val size = sizes[index]
            if (size.toInt() == 0) continue
            val i_7 = 1 shl 32 - size
            val value = values[size.toInt()]
            masks[index] = value
            val i_9: Int
            var keyIndex: Int
            var count: Int
            var i_12: Int
            if (value and i_7 != 0)
                i_9 = values[size - 1]
            else {
                i_9 = value or i_7

                keyIndex = size - 1
                while (keyIndex >= 1) {
                    count = values[keyIndex]
                    if (count != value)
                        break

                    i_12 = 1 shl 32 - keyIndex
                    if (count and i_12 != 0) {
                        values[keyIndex] = values[keyIndex - 1]
                        break
                    }

                    values[keyIndex] = count or i_12
                    --keyIndex
                }
            }

            values[size.toInt()] = i_9

            keyIndex = size + 1
            while (keyIndex <= 32) {
                if (values[keyIndex] == value)
                    values[keyIndex] = i_9
                keyIndex++
            }

            keyIndex = 0

            count = 0
            while (count < size) {
                i_12 = Integer.MIN_VALUE.ushr(count)
                if (value and i_12 != 0) {
                    if (keys[keyIndex] == 0)
                        keys[keyIndex] = key

                    keyIndex = keys[keyIndex]
                } else
                    ++keyIndex

                if (keyIndex >= keys.size) {
                    val keysCopy = IntArray(keys.size * 2)
                    keys.indices.forEach { keysCopy[it] = keys[it] }
                    keys = keysCopy
                }

                count++
            }

            keys[keyIndex] = index.inv()
            if (keyIndex >= key)
                key = keyIndex + 1
        }
    }

    fun compress(text: String, output: ByteArray): Int {
        var key = 0

        val input = text.toByteArray()

        var bitpos = 0
        for (pos in text.indices) {
            val data = input[pos].toInt() and 255
            val size = this.sizes[data]
            val mask = masks[data]

            if (size.toInt() == 0) throw RuntimeException("Size is equal to zero for Data = $data")

            var remainder = bitpos and 7
            key = key and (-remainder shr 31)
            var offset = bitpos shr 3
            bitpos += size.toInt()
            val i_41_ = (-1 + (remainder - -size) shr 3) + offset
            remainder += 24
            key = key or mask.ushr(remainder)
            output[offset] = key.toByte()
            if (i_41_.inv() < offset.inv()) {
                remainder -= 8
                key = mask.ushr(remainder)
                output[++offset] = key.toByte()
                if (offset.inv() > i_41_.inv()) {
                    remainder -= 8
                    key = mask.ushr(remainder)
                    output[++offset] = key.toByte()
                    if (offset.inv() > i_41_.inv()) {
                        remainder -= 8
                        key = mask.ushr(remainder)
                        output[++offset] = key.toByte()
                        if (i_41_ > offset) {
                            remainder -= 8
                            key = mask shl -remainder
                            output[++offset] = key.toByte()
                        }
                    }
                }
            }
        }

        return 7 + bitpos shr 3
    }

    fun decompress(compressed: ByteArray, decompressed: ByteArray, decompressedLength: Int): Int {
        var decompressedLen = decompressedLength
        val i_2 = 0
        var i_4 = 0
        if (decompressedLength == 0)
            return 0
        else {
            var i_7 = 0
            decompressedLen += i_4
            var i_8 = i_2

            while (true) {
                val b_9 = compressed[i_8]
                if (b_9 < 0)
                    i_7 = keys[i_7]
                else
                    ++i_7

                var i_10: Int
                i_10 = keys[i_7]
                if (i_10 < 0) {
                    decompressed[i_4++] = i_10.inv().toByte()
                    if (i_4 >= decompressedLength)
                        break

                    i_7 = 0
                }

                if (b_9.toInt() and 0x40 != 0)
                    i_7 = keys[i_7]
                else
                    ++i_7

                i_10 = keys[i_7]
                if (i_10 < 0) {
                    decompressed[i_4++] = i_10.inv().toByte()
                    if (i_4 >= decompressedLength)
                        break

                    i_7 = 0
                }

                if (b_9.toInt() and 0x20 != 0)
                    i_7 = keys[i_7]
                else
                    ++i_7

                i_10 = keys[i_7]
                if (i_10 < 0) {
                    decompressed[i_4++] = i_10.inv().toByte()
                    if (i_4 >= decompressedLength)
                        break

                    i_7 = 0
                }

                if (b_9.toInt() and 0x10 != 0)
                    i_7 = keys[i_7]
                else
                    ++i_7

                i_10 = keys[i_7]
                if (i_10 < 0) {
                    decompressed[i_4++] = i_10.inv().toByte()
                    if (i_4 >= decompressedLength)
                        break

                    i_7 = 0
                }

                if (b_9.toInt() and 0x8 != 0)
                    i_7 = keys[i_7]
                else
                    ++i_7

                i_10 = keys[i_7]
                if (i_10 < 0) {
                    decompressed[i_4++] = i_10.inv().toByte()
                    if (i_4 >= decompressedLength)
                        break

                    i_7 = 0
                }

                if (b_9.toInt() and 0x4 != 0)
                    i_7 = keys[i_7]
                else
                    ++i_7

                i_10 = keys[i_7]
                if (i_10 < 0) {
                    decompressed[i_4++] = i_10.inv().toByte()
                    if (i_4 >= decompressedLength)
                        break

                    i_7 = 0
                }

                if (b_9.toInt() and 0x2 != 0)
                    i_7 = keys[i_7]
                else
                    ++i_7

                i_10 = keys[i_7]
                if (i_10 < 0) {
                    decompressed[i_4++] = i_10.inv().toByte()
                    if (i_4 >= decompressedLength)
                        break

                    i_7 = 0
                }

                if (b_9.toInt() and 0x1 != 0)
                    i_7 = keys[i_7]
                else
                    ++i_7

                i_10 = keys[i_7]
                if (i_10 < 0) {
                    decompressed[i_4++] = i_10.inv().toByte()
                    if (i_4 >= decompressedLength)
                        break

                    i_7 = 0
                }

                ++i_8
            }

            return i_8 + 1 - i_2
        }
    }
}
