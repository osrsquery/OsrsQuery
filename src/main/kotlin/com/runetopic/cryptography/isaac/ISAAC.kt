package com.runetopic.cryptography.isaac

/**
 * @author Jordan Abraham
 */
class ISAAC : IISAAC {
    private val results = IntArray(SIZE)
    private val mem = IntArray(SIZE)

    private var a = GOLDEN_RATIO
    private var b = GOLDEN_RATIO
    private var c = GOLDEN_RATIO
    private var d = GOLDEN_RATIO
    private var e = GOLDEN_RATIO
    private var f = GOLDEN_RATIO
    private var g = GOLDEN_RATIO
    private var h = GOLDEN_RATIO

    private var randa = 0
    private var randb = 0
    private var randc = 0
    private var randcnt = 0

    override fun getNext(): Int {
        if (randcnt == 0) {
            random()
            randcnt = SIZE
        }
        return results[--randcnt]
    }

    override fun from(src: IntArray): ISAAC = throw IllegalAccessError("ISAAC is a one-way function.")
    override fun to(src: IntArray): ISAAC {
        src.copyInto(results)
        (0..3).forEach { mix(true, it) }
        (0 until SIZE step 8).forEach {
            a += results[it]
            b += results[it + 1]
            c += results[it + 2]
            d += results[it + 3]
            e += results[it + 4]
            f += results[it + 5]
            g += results[it + 6]
            h += results[it + 7]
            mix(false, it)
        }
        (0 until SIZE step 8).forEach {
            a += mem[it]
            b += mem[it + 1]
            c += mem[it + 2]
            d += mem[it + 3]
            e += mem[it + 4]
            f += mem[it + 5]
            g += mem[it + 6]
            h += mem[it + 7]
            mix(false, it)
        }
        random()
        randcnt = SIZE
        return this
    }

    private fun mix(firstPass: Boolean, position: Int) {
        a = a xor (b shl 11); /****/d += a; b += c
        b = b xor (c ushr 2); /****/e += b; c += d
        c = c xor (d shl 8); /*****/f += c; d += e
        d = d xor (e ushr 16); /***/g += d; e += f
        e = e xor (f shl 10); /****/h += e; f += g
        f = f xor (g ushr 4); /****/a += f; g += h
        g = g xor (h shl 8); /*****/b += g; h += a
        h = h xor (a ushr 9); /****/c += h; a += b
        if (firstPass.not()) p8(position)
    }

    private fun p8(position: Int) {
        mem[position] = a
        mem[position + 1] = b
        mem[position + 2] = c
        mem[position + 3] = d
        mem[position + 4] = e
        mem[position + 5] = f
        mem[position + 6] = g
        mem[position + 7] = h
    }

    private fun random() {
        randb += ++randc
        (0 until SIZE).forEach { pos ->
            val position = mem[pos]
            when (pos and 3) {
                0 -> randa = randa xor (randa shl 13)
                1 -> randa = randa xor (randa ushr 6)
                2 -> randa = randa xor (randa shl 2)
                3 -> randa = randa xor (randa ushr 16)
            }
            randa += mem[(pos + SIZE / 2) and (SIZE - 1)]
            (mem[position and MASK shr 2] + randa + randb)
                .also { mem[pos] = it }
                .also { randb = (mem[it shr Long.SIZE_BYTES and MASK shr 2] + position).also { next -> results[pos] = next } }
        }
    }

    private companion object {
        const val GOLDEN_RATIO = 0x9E3779B9.toInt()
        const val SIZE = 1 shl Long.SIZE_BYTES
        const val MASK = SIZE - 1 shl 2
    }
}
