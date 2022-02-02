package com.query.cache.definitions.impl

import com.displee.compress.decompress
import com.query.Application
import com.query.Constants
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.CacheType
import com.query.utils.IndexType
import com.query.utils.Sprite
import com.query.utils.index
import io.netty.buffer.Unpooled
import java.util.concurrent.CountDownLatch

data class SpriteDefinition(
    override var id: Int,
    var sprite: Sprite
) : Definition

class SpriteProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(SpriteDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val table = Constants.library.index(IndexType.SPRITES)
        val sprites : MutableList<SpriteDefinition> = emptyList<SpriteDefinition>().toMutableList()
        for (i in 0 until table.archives().size) {
            val sector = table.readArchiveSector(i) ?: continue
            val sprite = decode(i,sector.decompress())[0]
            if (sprite.width > 0 || sprite.height > 0) {
                sprites.add(SpriteDefinition(i,sprite))
            }
        }
        return Serializable(CacheType.SPRITES,this, sprites,writeTypes)
    }


    private val flagVertical = 0b01
    private val flagAlpha = 0b10

    fun decode(id: Int, data: ByteArray): Array<Sprite> {
        val buffer = Unpooled.wrappedBuffer(data)

        buffer.readerIndex(buffer.writerIndex() - 2)
        val spriteCount = buffer.readUnsignedShort()

        val sprites = arrayOfNulls<Sprite>(spriteCount)

        buffer.readerIndex(buffer.writerIndex() - 7 - spriteCount * 8)

        val width = buffer.readUnsignedShort()
        val height = buffer.readUnsignedShort()
        val paletteLength = buffer.readUnsignedByte() + 1

        for (i in 0 until spriteCount) {
            sprites[i] = Sprite(
                id,
                frame = i,
                maxWidth = width,
                maxHeight = height
            )
        }

        for (i in 0 until spriteCount) {
            sprites[i]?.offsetX = buffer.readUnsignedShort()
        }

        for (i in 0 until spriteCount) {
            sprites[i]?.offsetY = buffer.readUnsignedShort()
        }

        for (i in 0 until spriteCount) {
            sprites[i]?.width = buffer.readUnsignedShort()
        }

        for (i in 0 until spriteCount) {
            sprites[i]?.height = buffer.readUnsignedShort()
        }

        buffer.readerIndex(buffer.writerIndex() - 7 - spriteCount * 8 - (paletteLength - 1) * 3)

        val palette = IntArray(paletteLength)

        for (i in 1 until paletteLength) {
            palette[i] = buffer.readMedium()
            if (palette[i] == 0) {
                palette[i] = 1
            }
        }

        buffer.readerIndex(0)

        for (i in 0 until spriteCount) {
            val definition = sprites[i] ?: error("Sprite at $i was null, that's not supposed to happen!")

            val spriteWidth = definition.width
            val spriteHeight = definition.height
            val dimension = spriteWidth * spriteHeight
            val pixelPaletteIndices = ByteArray(dimension)
            val pixelAlphas = ByteArray(dimension)

            definition.pixelIdx = pixelPaletteIndices
            definition.palette = palette

            val flags = buffer.readUnsignedByte().toInt()

            if ((flags and flagVertical) == 0) {
                for (j in 0 until dimension) {
                    pixelPaletteIndices[j] = buffer.readByte()
                }
            } else {
                for (j in 0 until spriteWidth) {
                    for (k in 0 until spriteHeight) {
                        pixelPaletteIndices[spriteWidth * k + j] = buffer.readByte()
                    }
                }
            }

            if ((flags and flagAlpha) != 0) {
                if ((flags and flagVertical) == 0) {
                    for (j in 0 until dimension) {
                        pixelAlphas[j] = buffer.readByte()
                    }
                } else {
                    for (j in 0 until spriteWidth) {
                        for (k in 0 until spriteHeight) {
                            pixelAlphas[spriteWidth * k + j] = buffer.readByte()
                        }
                    }
                }
            } else {
                for (j in 0 until dimension) {
                    val index = pixelPaletteIndices[j].toInt()
                    if (index != 0) pixelAlphas[j] = 0xFF.toByte()
                }
            }

            val pixels = IntArray(dimension)

            for (j in 1 until dimension) {
                val index = pixelPaletteIndices[j].toInt() and 0xFF
                pixels[j] = palette[index] or (pixelAlphas[j].toInt() shl 24)
            }

            definition.pixels = pixels
        }

        return sprites.mapNotNull { it }.toTypedArray()
    }

}