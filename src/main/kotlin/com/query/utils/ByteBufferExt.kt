package com.query.utils

import java.nio.ByteBuffer
import java.nio.charset.Charset

val ByteBuffer.uShort: Int get() = this.short.toInt() and 0xffff
val ByteBuffer.uByte: Int get() = this.get().toInt() and 0xff
val ByteBuffer.byte: Byte get() = this.get()
val ByteBuffer.rsString: String get() = getString(this)
val ByteBuffer.medium: Int get() = this.get().toInt() and 0xff shl 16 or (this.get().toInt() and 0xff shl 8) or (this.get().toInt() and 0xff)


fun ByteBuffer.readBigSmart(): Int {
    return if (this.get(position()) < 0) {
        this.int and 0x7fffffff
    } else {
        val i_1: Int = readUnsignedShort()
        if (i_1 == 32767) -1 else i_1
    }
}

internal fun ByteBuffer.readUnsignedSmart(): Int {
    val peek = get(position()).toInt() and 0xFF
    return if (peek < 128) readUnsignedByte() else (readUnsignedShort()) - 0x8000
}


internal fun ByteBuffer.readUnsignedByte(): Int = get().toInt() and 0xFF
internal fun ByteBuffer.readUnsignedShort(): Int = short.toInt() and 0xFFFF
internal fun ByteBuffer.readUnsignedMedium(): Int = ((readUnsignedByte() shl 16) or (readUnsignedByte() shl 8) or readUnsignedByte())

val ByteBuffer.uMedium: Int get() = ((uByte shl 16) or (uByte shl 8) or uByte)
fun ByteBuffer.skip(amount: Int): ByteBuffer = position(position() + amount)

internal fun ByteBuffer.readUnsignedIntSmartShortCompat(): Int {
    var value = 0
    var i = readUnsignedSmart()
    while (i == 32767) {
        i = readUnsignedSmart()
        value += 32767
    }
    value += i
    return value
}

internal fun ByteBuffer.readString(): String {
    val mark = position()
    var length = 0
    while (get().toInt() != 0) {
        length++
    }
    if (length == 0) return ""
    val byteArray = ByteArray(length)
    position(mark)
    get(byteArray)
    position(position() + 1)
    return String(byteArray, Charset.defaultCharset())
}

val cp1252Identifiers = charArrayOf(
    '\u20ac',
    '\u0000',
    '\u201a',
    '\u0192',
    '\u201e',
    '\u2026',
    '\u2020',
    '\u2021',
    '\u02c6',
    '\u2030',
    '\u0160',
    '\u2039',
    '\u0152',
    '\u0000',
    '\u017d',
    '\u0000',
    '\u0000',
    '\u2018',
    '\u2019',
    '\u201c',
    '\u201d',
    '\u2022',
    '\u2013',
    '\u2014',
    '\u02dc',
    '\u2122',
    '\u0161',
    '\u203a',
    '\u0153',
    '\u0000',
    '\u017e',
    '\u0178'
)



fun getString(buffer: ByteBuffer): String {
    val builder = StringBuilder()
    var b: Int
    while ((buffer.uByte).also { b = it } != 0) {
        builder.append(b.toChar())
    }
    return builder.toString()
}