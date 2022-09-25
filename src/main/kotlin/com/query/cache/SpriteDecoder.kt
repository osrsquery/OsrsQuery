package com.query.cache

import ByteBufferUtils
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

class SpriteDecoder(
    val width: Int,
    val height: Int,
    val size: Int = 1
) {

    private val frames: Array<BufferedImage?> = arrayOfNulls(size)

    init {
        require(size >= 1)
    }

    fun getFrame(id: Int) = frames[id]

    fun setFrame(id: Int, frame: BufferedImage) {
        require(!(frame.width != width || frame.height != height)) {
            "The frame's dimensions do not match with the sprite's dimensions."
        }
        frames[id] = frame
    }

    fun size() = frames.size


    companion object {

        const val FLAG_VERTICAL = 0x01

        const val FLAG_ALPHA = 0x02

        fun decode(buffer: ByteBuffer): SpriteDecoder {
            /* find the size of this sprite set */
            buffer.position(buffer.limit() - 2)
            val size = buffer.short.toInt() and 0xFFFF

            /* allocate arrays to store info */
            val offsetsX = IntArray(size)
            val offsetsY = IntArray(size)
            val subWidths = IntArray(size)
            val subHeights = IntArray(size)

            /* read the width, height and palette size */buffer.position(buffer.limit() - size * 8 - 7)
            val width = buffer.short.toInt() and 0xFFFF
            val height = buffer.short.toInt() and 0xFFFF
            val palette = IntArray((buffer.get().toInt() and 0xFF) + 1)

            /* and allocate an object for this sprite set */
            val set = SpriteDecoder(width, height, size)

            /* read the offsets and dimensions of the individual sprites */for (i in 0 until size) {
                offsetsX[i] = buffer.short.toInt() and 0xFFFF
            }
            for (i in 0 until size) {
                offsetsY[i] = buffer.short.toInt() and 0xFFFF
            }
            for (i in 0 until size) {
                subWidths[i] = buffer.short.toInt() and 0xFFFF
            }
            for (i in 0 until size) {
                subHeights[i] = buffer.short.toInt() and 0xFFFF
            }

            buffer.position(buffer.limit() - size * 8 - 7 - (palette.size - 1) * 3)

            palette[0] = 0
            for (index in 1 until palette.size) {
                palette[index] = ByteBufferUtils.get24Int(buffer)
                if (palette[index] == 0) palette[index] = 1
            }

            buffer.position(0)
            for (id in 0 until size) {
                val subWidth = subWidths[id]
                val subHeight = subHeights[id]
                val offsetX = offsetsX[id]
                val offsetY = offsetsY[id]

                set.frames[id] = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                val image = set.frames[id]

                /* allocate an array for the palette indices */
                val indices = Array(subWidth) { IntArray(subHeight) }

                val flags = buffer.get().toInt() and 0xFF

                if (image != null) {

                    if (flags and FLAG_VERTICAL != 0) {
                        for (x in 0 until subWidth) {
                            for (y in 0 until subHeight) {
                                indices[x][y] = buffer.get().toInt() and 0xFF
                            }
                        }
                    } else {
                        for (y in 0 until subHeight) {
                            for (x in 0 until subWidth) {
                                indices[x][y] = buffer.get().toInt() and 0xFF
                            }
                        }
                    }

                    if (flags and FLAG_ALPHA != 0) {
                        if (flags and FLAG_VERTICAL != 0) {
                            for (x in 0 until subWidth) {
                                for (y in 0 until subHeight) {
                                    val alpha = buffer.get().toInt() and 0xFF
                                    image.setRGB(x + offsetX, y + offsetY, alpha shl 24 or palette[indices[x][y]])
                                }
                            }
                        } else {
                            for (y in 0 until subHeight) {
                                for (x in 0 until subWidth) {
                                    val alpha = buffer.get().toInt() and 0xFF
                                    image.setRGB(x + offsetX, y + offsetY, alpha shl 24 or palette[indices[x][y]])
                                }
                            }
                        }
                    } else {
                        for (x in 0 until subWidth) {
                            for (y in 0 until subHeight) {
                                val index = indices[x][y]
                                if (index == 0) {
                                    image.setRGB(x + offsetX, y + offsetY, 0)
                                } else {
                                    image.setRGB(x + offsetX, y + offsetY, -0x1000000 or palette[index])
                                }
                            }
                        }
                    }
                }
            }
            return set
        }
    }

}