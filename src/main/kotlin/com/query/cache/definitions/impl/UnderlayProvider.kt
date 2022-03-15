package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.ConfigType
import com.query.utils.IndexType
import com.query.utils.index
import com.query.utils.uByte
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class UnderlayDefinition(
    override val id: Int = 0,
    var color: Int = 0,
    var hue: Int = 0,
    var saturation: Int = 0,
    var lightness: Int = -1,
    var hueMultiplier: Int = 0
): Definition {

    fun calculateHsl() {
        val var1: Int = color
        val var2 = (var1 shr 16 and 255).toDouble() / 256.0
        val var4 = (var1 shr 8 and 255).toDouble() / 256.0
        val var6 = (var1 and 255).toDouble() / 256.0
        var var8 = var2
        if (var4 < var2) {
            var8 = var4
        }
        if (var6 < var8) {
            var8 = var6
        }
        var var10 = var2
        if (var4 > var2) {
            var10 = var4
        }
        if (var6 > var10) {
            var10 = var6
        }
        var var12 = 0.0
        var var14 = 0.0
        val var16 = (var10 + var8) / 2.0
        if (var8 != var10) {
            if (var16 < 0.5) {
                var14 = (var10 - var8) / (var8 + var10)
            }
            if (var16 >= 0.5) {
                var14 = (var10 - var8) / (2.0 - var10 - var8)
            }
            if (var2 == var10) {
                var12 = (var4 - var6) / (var10 - var8)
            } else if (var10 == var4) {
                var12 = 2.0 + (var6 - var2) / (var10 - var8)
            } else if (var10 == var6) {
                var12 = 4.0 + (var2 - var4) / (var10 - var8)
            }
        }
        var12 /= 6.0
        this.saturation = (var14 * 256.0).toInt()
        lightness = (var16 * 256.0).toInt()
        if (this.saturation < 0) {
            this.saturation = 0
        } else if (this.saturation > 255) {
            this.saturation = 255
        }
        if (lightness < 0) {
            lightness = 0
        } else if (lightness > 255) {
            lightness = 255
        }
        if (var16 > 0.5) {
            hueMultiplier = (var14 * (1.0 - var16) * 512.0).toInt()
        } else {
            hueMultiplier = (var14 * var16 * 512.0).toInt()
        }
        if (hueMultiplier < 1) {
            hueMultiplier = 1
        }
        hue = (hueMultiplier.toDouble() * var12).toInt()
    }

}

class UnderlayProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(UnderlayDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.UNDERLAY.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), UnderlayDefinition(it))
        }
        return Serializable(DefinitionsTypes.UNDERLAYS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: UnderlayDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> definition.color = (((buffer.uByte) shl 16) + ((buffer.uByte) shl 8) + (buffer.uByte))
            0 -> break
            else -> logger.warn { "Unhandled underlay definition opcode with id: ${opcode}." }
        } while (true)
        definition.calculateHsl()
        return definition
    }


}