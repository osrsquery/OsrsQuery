package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.definitions.Definition
import com.query.cache.definitions.Loader
import com.query.cache.definitions.Serializable
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
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
): Definition() {

    val count : Int = 0

    @Throws(IOException::class)
    override fun encode(dos : DataOutputStream) {

        if (spriteId != -1) {
            dos.writeByte(1)
            val dir = File(FileUtil.getBase(),"/sprites/pink/")
            val newdir = File(FileUtil.getBase(),"/mapFunctions/")
            if(spriteId == 1454) {
                if(!File(newdir,"${6}.png").exists()) {
                    File(dir,"${1454}.png").copyTo(File(newdir,"${6}.png"),true)
                }
                dos.writeShort(6)
            } else {
                val finialID = if(newdir.listFiles().size < 6) newdir.listFiles().size else newdir.listFiles().size + 1
                File(dir,"${spriteId}.png").copyTo(File(newdir,"${finialID}.png"),true)
                dos.writeShort(finialID)
            }
        }

        if (field3294 != -1) {
            dos.writeByte(2)
            dos.writeShort(field3294)
        }

        if (name != null) {
            dos.writeByte(3)
            dos.writeString(name!!)
        }

        if (fontColor != 0) {
            dos.writeByte(4)
            dos.write24bitInt(fontColor)
        }
        if (field3297 != -1) {
            dos.writeByte(5)
            dos.writeInt(field3297)
        }
        if (fontSize != 0) {
            dos.writeByte(6)
            dos.writeByte(fontSize)
        }

        if (options.any { it != null }) {
            dos.writeByte(7)
            dos.writeByte(options.size)
            for (i in options.indices) {
                if (options[i] == null) {
                    continue
                }
                dos.writeString(options[i]!!)
            }
        }

        dos.writeByte(0)
    }
}

class AreaProvider(val latch: CountDownLatch?) : Loader, Runnable {

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

        return Serializable(DefinitionsTypes.AREAS,this, definitions)
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