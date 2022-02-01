package com.query.utils

import java.nio.ByteBuffer

object ByteBufferExt {

    fun getMedium(buffer: ByteBuffer): Int {
        return buffer.get().toInt() and 0xff shl 16 or (buffer.get().toInt() and 0xff shl 8) or (buffer.get().toInt() and 0xff)
    }

    fun getString(buffer: ByteBuffer): String {
        val builder = StringBuilder()
        var b: Int
        while ((buffer.get().toInt() and 0xff).also { b = it } != 0) {
            builder.append(b.toChar())
        }
        return builder.toString()
    }

}