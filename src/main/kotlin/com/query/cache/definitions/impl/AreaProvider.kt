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


data class AreaDefinition(
    override val id: Int = 0,
    var field3292: IntArray? = null,
    var spriteId: Int = -1,
    var field3294: Int = -1,
    var name: String? = null,
    var fontColor : Int = 0,
    var field3297: Int = -1,
    var options: Array<String?> = arrayOfNulls(5),
    var field3300: IntArray? = null,
    var field3308: String? = null,
    var field3309: ByteArray? = null,
    var fontSize : Int = 0
): Definition

class AreaProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 142

    override fun run() {
        if(ignore()) {
            latch?.countDown()
            return
        }
        val start: Long = System.currentTimeMillis()
        Application.store(AreaDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.AREA.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), AreaDefinition(it))
        }

        return Serializable(DefinitionsTypes.AREAS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: AreaDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> {
                definition.spriteId = buffer.uShort
            }
            2 -> definition.field3294 = buffer.uShort
            3 -> definition.name = buffer.rsString
            4 -> definition.fontColor = buffer.medium
            5 -> buffer.medium
            6 -> definition.fontSize = buffer.uByte
            7 -> buffer.uByte
            8 -> buffer.uByte
            in 10..14 -> definition.options[opcode - 10] = buffer.rsString
            15 -> {
                val length: Int = buffer.uByte
                definition.field3300 = IntArray(length * 2)
                (0 until length * 2).forEach {
                    definition.field3300!![it] = buffer.short.toInt()
                }
                buffer.int
                val subLength: Int = buffer.uByte
                definition.field3292 = IntArray(subLength)
                (0 until subLength).forEach {
                    definition.field3292!![it] = buffer.int
                }
                definition.field3309 = ByteArray(length)
                (0 until length).forEach {
                    definition.field3309!![it] = buffer.byte
                }
            }
            16 -> buffer.byte
            17 -> definition.field3308 = buffer.rsString
            18 -> buffer.short
            19 -> definition.field3297 = buffer.uShort
            21 -> buffer.int
            22 -> buffer.int
            23 -> buffer.medium
            24 -> buffer.short
            25 -> buffer.short
            28 -> buffer.uByte
            29 -> buffer.uByte
            30 -> buffer.uByte
            0 -> break
            else -> logger.warn { "Unhandled area definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }

    companion object {

        fun fontSizeName(fontSize : Int) = when(fontSize) {
            0 -> "Default"
            1 -> "Medium"
            2 -> "Large"
            else -> "Default"
        }

        fun formatName(name : String) = name.replace("<br>"," ").replace("\\u003cbr","".replace("\\u003e"," "))


    }



}