package com.query.utils

import java.nio.ByteBuffer

val ByteBuffer.uShort: Int get() = this.short.toInt() and 0xffff
val ByteBuffer.uByte: Int get() = this.get().toInt() and 0xff
val ByteBuffer.byte: Byte get() = this.get()
val ByteBuffer.rsString: String get() = getString(this)
val ByteBuffer.medium: Int get() = this.get().toInt() and 0xff shl 16 or (this.get().toInt() and 0xff shl 8) or (this.get().toInt() and 0xff)

val ByteBuffer.shortSmart : Int get() {
    val peek = uByte
    return if (peek < 128) peek - 64 else (peek shl 8 or uByte) - 49152
}

fun ByteBuffer.readByteArray(length: Int): ByteArray {
    val array = ByteArray(length)
    get(array)
    return array
}

fun ByteBuffer.readParams(): MutableMap<Int, String> {
    val params : MutableMap<Int,String> = mutableMapOf()
    (0 until uByte).forEach { _ ->
        val string: Boolean = (uByte) == 1
        val key: Int = medium
        val value: Any = if (string) { rsString } else { int }
        params[key] = value.toString()
    }
    return params
}

fun getString(buffer: ByteBuffer): String {
    val builder = StringBuilder()
    var b: Int
    while ((buffer.uByte).also { b = it } != 0) {
        builder.append(b.toChar())
    }
    return builder.toString()
}