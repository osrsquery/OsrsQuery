package com.query.cache.definitions.provider

import com.query.Application.logger
import com.query.Application.objects
import com.query.Constants.library
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.CacheType
import com.query.utils.ByteBufferExt
import com.query.utils.ConfigType
import com.query.utils.IndexType
import com.query.utils.index
import java.nio.ByteBuffer
import java.util.stream.IntStream


data class AreaDefinition(
    override val id: Int = 0,
    var field3292: IntArray? = null,
    var spriteId: Int = -1,
    var field3294: Int = -1,
    var name: String? = null,
    var tileHash : Int = 0,
    var field3297: Int = -1,
    var field3298: Array<String?> = arrayOfNulls(5),
    var field3300: IntArray? = null,
    var field3308: String? = null,
    var field3309: ByteArray? = null,
    var field3310 : Int = 0
): Definition

class AreaProvider : Loader {

    override fun load(writeTypes : Boolean): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.AREA.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), AreaDefinition(it))
        }
        return Serializable(CacheType.AREAS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: AreaDefinition): Definition {
        do when (val opcode: Int = buffer.get().toInt() and 0xff) {
            1 -> definition.spriteId = buffer.short.toInt() and 0xffff
            2 -> definition.field3294 = buffer.short.toInt() and 0xffff
            3 -> definition.name = ByteBufferExt.getString(buffer)
            4 -> definition.tileHash = ByteBufferExt.getMedium(buffer)
            5 -> ByteBufferExt.getMedium(buffer)
            6 -> definition.field3310 = buffer.get().toInt() and 0xff
            7 -> buffer.get().toInt() and 0xff
            8 -> buffer.get().toInt() and 0xff
            in 10..14 -> definition.field3298[opcode - 10] = ByteBufferExt.getString(buffer)
            15 -> {
                val length: Int = buffer.get().toInt() and 0xff
                definition.field3300 = IntArray(length * 2)
                (0 until length * 2).forEach {
                    definition.field3300!![it] = buffer.short.toInt()
                }
                buffer.int
                val subLength: Int = buffer.get().toInt() and 0xff
                definition.field3292 = IntArray(subLength)
                (0 until subLength).forEach {
                    definition.field3292!![it] = buffer.int
                }
                definition.field3309 = ByteArray(length)
                (0 until length).forEach {
                    definition.field3309!![it] = buffer.get()
                }
            }
            16 -> buffer.get()
            17 -> definition.field3308 = ByteBufferExt.getString(buffer)
            18 -> buffer.short
            19 -> definition.field3297 = buffer.short.toInt() and 0xffff
            21 -> buffer.int
            22 -> buffer.int
            23 -> ByteBufferExt.getMedium(buffer)
            24 -> buffer.short
            25 -> buffer.short
            28 -> buffer.get().toInt() and 0xff
            29 -> buffer.get().toInt() and 0xff
            30 -> buffer.get().toInt() and 0xff
            0 -> break
            else -> logger.warn { "Unhandled area definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}