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


data class KitDefinition(
    override val id: Int = 0,
    var recolorToReplace : ShortArray? = null,
    var recolorToFind : ShortArray? = null,
    var retextureToFind : ShortArray? = null,
    var retextureToReplace  : ShortArray? = null,
    var bodyPartId : Int = -1,
    var models : IntArray? = null,
    var chatheadModels : MutableList<Int> = mutableListOf(-1, -1, -1, -1, -1),
    var nonSelectable : Boolean = false
): Definition

class KitProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(KitDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.IDENTKIT.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), KitDefinition(it))
        }
        return Serializable(DefinitionsTypes.KIT,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: KitDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> definition.bodyPartId = buffer.uByte
            2 -> {
                val length: Int = buffer.uByte
                when {
                    length > 0 -> {
                        definition.models = IntArray(length)
                        (0 until length).forEach {
                            definition.models!![it] = buffer.uShort
                        }
                    }
                }
            }
            3 -> definition.nonSelectable = true
            40 -> {
                val length: Int = buffer.uByte
                when {
                    length > 0 -> {
                        definition.recolorToFind = ShortArray(length)
                        definition.recolorToReplace = ShortArray(length)
                        (0 until length).forEach {
                            definition.recolorToFind!![it] = buffer.short
                            definition.recolorToReplace!![it] = buffer.short
                        }
                    }
                }
            }
            41 -> {
                val length: Int = buffer.uByte
                when {
                    length > 0 -> {
                        definition.retextureToFind = ShortArray(length)
                        definition.retextureToReplace = ShortArray(length)
                        (0 until length).forEach {
                            definition.retextureToFind!![it] = buffer.short
                            definition.retextureToReplace!![it] = buffer.short
                        }
                    }
                }
            }
            in 60..70 -> {
                definition.chatheadModels[opcode - 60] = buffer.uShort
            }
            0 -> break
            else -> logger.warn { "Unhandled kit definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}