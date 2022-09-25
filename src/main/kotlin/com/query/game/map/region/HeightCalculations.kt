package com.query.game.map.region

import kotlin.math.cos
import kotlin.math.sin

object HeightCalculations {

    private const val JAGEX_CIRCULAR_ANGLE = 2048
    private const val ANGULAR_RATIO = 360.0 / JAGEX_CIRCULAR_ANGLE
    private val JAGEX_RADIAN = Math.toRadians(ANGULAR_RATIO)

    private val SIN = IntArray(JAGEX_CIRCULAR_ANGLE)
    private val COS = IntArray(JAGEX_CIRCULAR_ANGLE)

    init {
        for (i in 0 until JAGEX_CIRCULAR_ANGLE) {
            SIN[i] = (65536.0 * sin(i.toDouble() * JAGEX_RADIAN)).toInt()
            COS[i] = (65536.0 * cos(i.toDouble() * JAGEX_RADIAN)).toInt()
        }
    }

    fun calculate(baseX: Int, baseY: Int, x: Int, y: Int): Int {
        val xc = (baseX shr 3) + 932638 + x
        val yc = (baseY shr 3) + 556190 + y
        var n = (interpolateNoise(xc + '\ub135'.code, yc + 91923, 4) - 128 + (interpolateNoise(
            10294 + xc,
            yc + '\u93bd'.code,
            2
        ) - 128 shr 1)
                + (interpolateNoise(xc, yc, 1) - 128 shr 2))
        n = 35 + (n.toDouble() * 0.3).toInt()
        if (n >= 10) {
            if (n > 60) {
                n = 60
            }
        } else {
            n = 10
        }
        return n
    }

    private fun interpolateNoise(x: Int, y: Int, frequency: Int): Int {
        val intX = x / frequency
        val fracX = x and frequency - 1
        val intY = y / frequency
        val fracY = y and frequency - 1
        val v1 = smoothedNoise1(intX, intY)
        val v2 = smoothedNoise1(intX + 1, intY)
        val v3 = smoothedNoise1(intX, intY + 1)
        val v4 = smoothedNoise1(1 + intX, 1 + intY)
        val i1 = interpolate(v1, v2, fracX, frequency)
        val i2 = interpolate(v3, v4, fracX, frequency)
        return interpolate(i1, i2, fracY, frequency)
    }

    private fun smoothedNoise1(x: Int, y: Int): Int {
        val corners = noise(x - 1, y - 1) + noise(x + 1, y - 1) + noise(x - 1, 1 + y) + noise(x + 1, y + 1)
        val sides = noise(x - 1, y) + noise(1 + x, y) + noise(x, y - 1) + noise(x, 1 + y)
        val center = noise(x, y)
        return center / 4 + sides / 8 + corners / 16
    }

    private fun noise(x: Int, y: Int): Int {
        var n = x + y * 57
        n = n xor (n shl 13)
        return n * (n * n * 15731 + 789221) + 1376312589 and Int.MAX_VALUE shr 19 and 255
    }

    private fun interpolate(a: Int, b: Int, x: Int, y: Int): Int {
        val f = 65536 - COS[1024 * x / y] shr 1
        return (f * b shr 16) + (a * (65536 - f) shr 16)
    }

}