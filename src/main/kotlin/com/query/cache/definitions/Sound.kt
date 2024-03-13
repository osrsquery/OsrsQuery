package com.query.cache.definitions

import com.query.utils.medium
import com.query.utils.uByte
import com.query.utils.uShort
import java.nio.ByteBuffer

data class Sound(
    var id: Int,
    var loops: Int,
    var location: Int,
    var retain: Int
) {
    companion object {
        fun readFrameSound(buffer: ByteBuffer, pre220 : Boolean): Sound? {
            val id: Int
            val loops: Int
            val location: Int
            val retain: Int
            if (pre220) {
                val payload: Int = buffer.medium
                location = payload and 15
                id = payload shr 8
                loops = payload shr 4 and 7
                retain = 0
            } else {
                id = buffer.uShort
                loops = buffer.uByte
                location = buffer.uByte
                retain = buffer.uByte
            }

            return if (id >= 1 && loops >= 1 && location >= 0 && retain >= 0) {
                Sound(id, loops, location, retain)
            } else {
                null
            }
        }
    }
}