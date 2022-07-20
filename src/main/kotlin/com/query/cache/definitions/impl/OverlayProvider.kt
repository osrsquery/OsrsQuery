package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import java.io.DataOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class OverlayDefinition(
    override val id: Int = 0,
    var rgbColor: Int = 0,
    var secondaryRgbColor: Int = -1,
    var textureId: Int = -1,
    var hideUnderlay: Boolean = true,
    var hue: Int = 0,
    var saturation: Int = 0,
    var lightness: Int = 0,
    var otherHue: Int = 0,
    var otherSaturation: Int = 0,
    var otherLightness: Int = 0,
    var textureResolution: Int = 512,
    var aBoolean397: Boolean = true,
    var anInt398: Int = 8,
    var aBoolean391: Boolean = false,
    var anInt392: Int = 1190717,
    var anInt395: Int = 64,
    var anInt388: Int = 127
): Definition {


    fun encode(dos: DataOutputStream) {
        if (rgbColor != 0) {
            dos.writeByte(1)
            dos.writeByte(rgbColor shr 16)
            dos.writeByte(rgbColor shr 8)
            dos.writeByte(rgbColor)
        }

        if (textureId != -1) {
            dos.writeByte(2)
            dos.writeShort(textureId)
        }


        if (textureId != -1) {
            dos.writeByte(3)
            dos.writeShort(textureId)
        }


        if (!hideUnderlay) {
            dos.writeByte(5)
        }

        if (secondaryRgbColor != -1) {
            dos.writeByte(7)
            dos.writeByte(secondaryRgbColor shr 16)
            dos.writeByte(secondaryRgbColor shr 8)
            dos.writeByte(secondaryRgbColor)
        }

        if (textureResolution != 512) {
            dos.writeByte(9)
            dos.writeShort(textureResolution)
        }

        if (!aBoolean397) {
            dos.writeByte(10)
        }

        if (anInt398 != 8) {
            dos.writeByte(11)
            dos.writeByte(anInt398)
        }

        if (!aBoolean391) {
            dos.writeByte(12)
        }

        if (anInt392 != 1190717) {
            dos.writeByte(13)
            dos.writeByte(anInt392 shr 16)
            dos.writeByte(anInt392 shr 8)
            dos.writeByte(anInt392)
        }

        if (anInt395 != 64) {
            dos.writeByte(14)
            dos.writeShort(anInt395)
        }

        if (anInt388 != 127) {
            dos.writeByte(16)
            dos.writeByte(anInt388)
        }

        dos.writeByte(0)
    }

    private fun calculateHsl(var1: Int) {
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
        val var16 = (var8 + var10) / 2.0
        if (var10 != var8) {
            if (var16 < 0.5) {
                var14 = (var10 - var8) / (var10 + var8)
            }
            if (var16 >= 0.5) {
                var14 = (var10 - var8) / (2.0 - var10 - var8)
            }
            if (var2 == var10) {
                var12 = (var4 - var6) / (var10 - var8)
            } else if (var4 == var10) {
                var12 = 2.0 + (var6 - var2) / (var10 - var8)
            } else if (var10 == var6) {
                var12 = 4.0 + (var2 - var4) / (var10 - var8)
            }
        }
        var12 /= 6.0
        hue = (256.0 * var12).toInt()
        saturation = (var14 * 256.0).toInt()
        lightness = (var16 * 256.0).toInt()
        if (saturation < 0) {
            saturation = 0
        } else if (saturation > 255) {
            saturation = 255
        }
        if (lightness < 0) {
            lightness = 0
        } else if (lightness > 255) {
            lightness = 255
        }
    }

}

class OverlayProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(OverlayDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.OVERLAY.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), OverlayDefinition(it))
        }
        return Serializable(DefinitionsTypes.OVERLAYS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: OverlayDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> definition.rgbColor = buffer.medium
            2 -> definition.textureId = buffer.uByte
            3 -> buffer.uShort.let {
                definition.textureId = if (it > Short.MAX_VALUE) -1 else it
            }
            5 -> definition.hideUnderlay = false
            7 -> definition.secondaryRgbColor = buffer.medium
            8 -> {}
            9 ->  definition.textureResolution = buffer.readUnsignedShort() shl 2
            10 -> definition.aBoolean397 = false
            11 -> definition.anInt398 = buffer.readUnsignedByte()
            12 -> definition.aBoolean391 = true
            13 -> definition.anInt392 = buffer.readUnsignedMedium()
            14 -> definition.anInt395 = buffer.readUnsignedByte() shl 2
            16 -> definition.anInt388 = buffer.readUnsignedByte()
            0 -> break
            else -> logger.warn { "Unhandled overlay definition opcode with id: ${opcode}." }
        } while (true)

        return definition
    }


}