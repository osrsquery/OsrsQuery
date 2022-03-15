package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class SpotAnimationDefinition(
    override val id: Int = 0,
    var rotation: Int = 0,
    var textureToReplace: ShortArray? = null,
    var textureToFind: ShortArray? = null,
    var resizeY: Int = 128,
    var animationId: Int = -1,
    var recolorToFind: ShortArray? = null,
    var recolorToReplace: ShortArray? = null,
    var resizeX: Int = 128,
    var modelId: Int = 0,
    var ambient: Int = 0,
    var contrast: Int = 0,
): Definition

class SpotAnimationProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(SpotAnimationDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.SPOTANIM.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), SpotAnimationDefinition(it))
        }
        return Serializable(DefinitionsTypes.SPOTANIMS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: SpotAnimationDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> definition.modelId = buffer.uShort
            2 -> definition.animationId = buffer.uShort
            4 -> definition.resizeX = buffer.uShort
            5 -> definition.resizeY = buffer.uShort
            6 -> definition.rotation = buffer.uShort
            7 -> definition.ambient = buffer.uByte
            8 -> definition.contrast = buffer.uByte
            40 -> {
                val length: Int = buffer.uByte
                definition.recolorToFind = ShortArray(length)
                definition.recolorToReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.recolorToFind!![it] = (buffer.uShort).toShort()
                    definition.recolorToReplace!![it] = (buffer.uShort).toShort()
                }
            }
            41 -> {
                val length: Int = buffer.uByte
                definition.textureToFind = ShortArray(length)
                definition.textureToReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.textureToFind!![it] = (buffer.uShort).toShort()
                    definition.textureToReplace!![it] = (buffer.uShort).toShort()
                }
            }
            0 -> break
            else -> logger.warn { "Unhandled spotanim definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}