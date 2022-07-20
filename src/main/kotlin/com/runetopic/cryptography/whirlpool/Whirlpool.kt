package com.runetopic.cryptography.whirlpool

/**
 * @author Jordan Abraham
 */
class Whirlpool(
    private val rounds: Int,
    private val size: Int
) : IWhirlpool {
    private val block = Array(8) { LongArray(256) }
    private val blockRounds = LongArray(rounds + 1)
    private val hash = LongArray(8)
    private val buffer = ByteArray(size)
    private val bitSize = ByteArray(32)

    private var position = 0
    private var digestBits = 0

    override fun getRounds(): Int = rounds
    override fun getSize(): Int = size
    override fun getBlock(): Array<LongArray> = block
    override fun getBlockRounds(): LongArray = blockRounds
    override fun getHash(): LongArray = hash
    override fun getBuffer(): ByteArray = buffer
    override fun getBitSize(): ByteArray = bitSize

    override fun getPosition(): Int = position
    override fun setPosition(position: Int) {
        this.position = position
    }

    override fun getDigestBits(): Int = digestBits
    override fun setDigestBits(digestBits: Int) {
        this.digestBits = digestBits
    }

    override fun from(src: ByteArray): ByteArray = throw IllegalAccessError("Whirlpool is a one-way function.")
    override fun to(src: ByteArray): ByteArray = hash(src, size)

    init {
        val sbox = intArrayOf(
            0x1823, 0xC6E8, 0x87B8, 0x14F, 0x36A6, 0xD2F5, 0x796F, 0x9152,
            0x60BC, 0x9B8E, 0xA30C, 0x7B35, 0x1DE0, 0xD7C2, 0x2E4B, 0xFE57,
            0x1577, 0x37E5, 0x9FF0, 0x4ADA, 0x58C9, 0x290A, 0xB1A0, 0x6B85,
            0xBD5D, 0x10F4, 0xCB3E, 0x567, 0xE427, 0x418B, 0xA77D, 0x95D8,
            0xFBEE, 0x7C66, 0xDD17, 0x479E, 0xCA2D, 0xBF07, 0xAD5A, 0x8333,
            0x6302, 0xAA71, 0xC819, 0x49D9, 0xF2E3, 0x5B88, 0x9A26, 0x32B0,
            0xE90F, 0xD580, 0xBECD, 0x3448, 0xFF7A, 0x905F, 0x2068, 0x1AAE,
            0xB454, 0x9322, 0x64F1, 0x7312, 0x4008, 0xC3EC, 0xDBA1, 0x8D3D,
            0x9700, 0xCF2B, 0x7682, 0xD61B, 0xB5AF, 0x6A50, 0x45F3, 0x30EF,
            0x3F55, 0xA2EA, 0x65BA, 0x2FC0, 0xDE1C, 0xFD4D, 0x9275, 0x68A,
            0xB2E6, 0xE1F, 0x62D4, 0xA896, 0xF9C5, 0x2559, 0x8472, 0x394C,
            0x5E78, 0x388C, 0xD1A5, 0xE261, 0xB321, 0x9C1E, 0x43C7, 0xFC04,
            0x5199, 0x6D0D, 0xFADF, 0x7E24, 0x3BAB, 0xCE11, 0x8F4E, 0xB7EB,
            0x3C81, 0x94F7, 0xB913, 0x2CD3, 0xE76E, 0xC403, 0x5644, 0x7FA9,
            0x2ABB, 0xC153, 0xDC0B, 0x9D6C, 0x3174, 0xF646, 0xAC89, 0x14E1,
            0x163A, 0x6909, 0x70B6, 0xD0ED, 0xCC42, 0x98A4, 0x285C, 0xF886
        )

        (0 until sbox.size * 2).forEach {
            val code = sbox[it / 2].toLong()

            val v1 = if ((it and 1) == 0) code ushr 8 else code and 0xFF
            val v2 = maskWithReductionPolynomial(v1 shl 1)
            val v4 = maskWithReductionPolynomial(v2 shl 1)
            val v5 = v4 xor v1
            val v8 = maskWithReductionPolynomial(v4 shl 1)
            val v9 = v8 xor v1

            block[0][it] = (
                (v1 shl 56)
                    or (v1 shl 48)
                    or (v4 shl 40)
                    or (v1 shl 32)
                    or (v8 shl 24)
                    or (v5 shl 16)
                    or (v2 shl 8)
                    or v9
                )

            (1 until 8).forEach { index ->
                block[index][it] = ((block[index - 1][it] ushr 8) or (block[index - 1][it] shl 56))
            }

            blockRounds[0] = 0
            (1..rounds).forEach { round ->
                val offset = 8 * (round - 1)
                blockRounds[round] = (
                    /**/(block[0][offset/**/] and -0x100000000000000L)
                        xor (block[1][offset + 1] and 0x00FF000000000000L)
                        xor (block[2][offset + 2] and 0x0000FF0000000000L)
                        xor (block[3][offset + 3] and 0x000000FF00000000L)
                        xor (block[4][offset + 4] and 0x00000000FF000000L)
                        xor (block[5][offset + 5] and 0x0000000000FF0000L)
                        xor (block[6][offset + 6] and 0x000000000000FF00L)
                        xor (block[7][offset + 7] and 0x00000000000000FFL)
                    )
            }
        }
    }
}
