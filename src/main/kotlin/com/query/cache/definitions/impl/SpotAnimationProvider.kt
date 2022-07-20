package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import com.runetopic.cache.extension.toByteBuffer
import java.io.DataOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class SpotAnimationDefinition(
    override val id: Int = 0,
    var rotation: Int = 0,
    var textureToReplace: ShortArray? = null,
    var textureToFind: ShortArray? = null,
    var resizeY: Int = 128,
    var animationId: Int = 0,
    var recolorToFind: ShortArray? = null,
    var recolorToReplace: ShortArray? = null,
    var resizeX: Int = 128,
    var modelId: Int = -1,
    var ambient: Int = 0,
    var contrast: Int = 0,
): Definition {


    fun encode(dos: DataOutputStream) {
        if (modelId != 0) {
            dos.writeByte(1)
            dos.writeInt(modelId)
        }
        if (animationId != -1) {
            dos.writeByte(2)
            dos.writeInt(animationId)
        }
        if (resizeX != 128) {
            dos.writeByte(4)
            dos.writeShort(resizeX)
        }
        if (resizeY != 128) {
            dos.writeByte(5)
            dos.writeShort(resizeY)
        }
        if (rotation != 0) {
            dos.writeByte(6)
            dos.writeShort(rotation)
        }
        if (ambient != 0) {
            dos.writeByte(7)
            dos.writeByte(ambient)
        }
        if (contrast != 0) {
            dos.writeByte(8)
            dos.writeByte(contrast)
        }
        if (recolorToFind != null && recolorToReplace != null) {
            dos.writeByte(40)
            val len: Int = recolorToFind!!.size
            dos.writeByte(len)
            for (i in 0 until len) {
                dos.writeShort(recolorToFind!![i].toInt())
                dos.writeShort(recolorToReplace!![i].toInt())
            }
        }

        dos.writeByte(0)
    }
}

class SpotAnimationProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(SpotAnimationDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val definitions : MutableList<Definition> = emptyList<Definition>().toMutableList()
        Constants.store!!.index(21).use { index ->
            (0 until index.expand()).forEach {
                definitions.add(decode(index.group(it ushr 8).file(it and 0xFF).data.toByteBuffer(), SpotAnimationDefinition(it)))
            }
        }

        return Serializable(DefinitionsTypes.SPOTANIMS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: SpotAnimationDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> definition.modelId = buffer.readBigSmart()
            2 -> definition.animationId = buffer.readBigSmart()
            4 -> definition.resizeX = buffer.uShort
            5 -> definition.resizeY = buffer.uShort
            6 -> definition.rotation = buffer.uShort
            7 -> definition.ambient = buffer.uByte
            8 -> definition.contrast = buffer.uByte
            10 -> {}
            40 -> {
                val length: Int = buffer.uByte
                definition.recolorToFind = ShortArray(length)
                definition.recolorToReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.recolorToFind!![it] = (buffer.uShort).toShort()
                    definition.recolorToReplace!![it] = (buffer.uShort).toShort()
                }
            }
            0 -> break
            else -> logger.warn { "Unhandled spotanim definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}