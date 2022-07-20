package com.query.cache.definitions.impl

import ByteBufferUtils
import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class AreaDefinition(
    override val id: Int = 0,
    var spriteId: Int = -1,
    var iconKey: Int = -1,
    var description: String? = null,
    var fontColor : Int = 0,
    var opcode19: Int = -1,
    var options: Array<String?> = arrayOfNulls(5),
    var type: String? = null,
    var fontSize : Int = 0,
    var varbitSecondary : Int = -1,
    var varSecondary : Int = -1,
    var varMinSecondary : Int = -1,
    var varMaxSecondary : Int = -1,
    var varp : Int = -1,
    var varbit : Int = -1,
    var varValueMin : Int = -1,
    var varValueMax : Int = -1,
    var opcode21 : Int = -1,
    var flags : Int = -1,
    var opcode22 : Int = -1,
    var opcode39 : Int = -1,
    var opcode8: Boolean = true,
    var params : MutableMap<Int,String> = mutableMapOf()
): Definition {

    var aBool1996 : Boolean = false
    var aBool2028: Boolean = true

    fun encode(dos: DataOutputStream) {

        if (spriteId != -1) {
            dos.writeByte(1)
            dos.writeShort(spriteId)
        }
        if (iconKey != -1) {
            dos.writeByte(2)
            dos.writeShort(iconKey)
        }

    }

}

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
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.MAP_AREAS.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), AreaDefinition(it))
        }
        return Serializable(DefinitionsTypes.AREAS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: AreaDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> { definition.spriteId = buffer.uShort }
            2 -> definition.iconKey = buffer.uShort
            3 -> definition.description = buffer.rsString
            4 -> definition.fontColor = buffer.medium
            6 -> definition.fontSize = buffer.uByte
            7 ->  {
                definition.aBool1996 = false
                definition.aBool2028 = true
                definition.flags = buffer.uByte
                if (definition.flags and 0x1 == 0) definition.aBool1996 = false
                if (0x2 and definition.flags == 2) definition.aBool2028 = true
            }
            8 -> definition.opcode8 = (buffer.uByte) == 1
            9 ->  {
                buffer.uByte.let {
                    definition.varbit = if (it == 65535) -1 else it
                }
                buffer.uByte.let {
                    definition.varp = if (it == 65535) -1 else it
                }
                definition.varValueMin = buffer.int
                definition.varValueMax = buffer.int
            }
            in 10..14 -> definition.options[opcode - 10] = buffer.rsString
            17 -> definition.type = buffer.rsString
            19 -> definition.opcode19 = buffer.uShort
            20 -> {
                buffer.uShort.let {
                    definition.varbitSecondary = if (it == 65535) -1 else it
                }
                buffer.uShort.let {
                    definition.varSecondary = if (it == 65535) -1 else it
                }
                definition.varMinSecondary = buffer.int
                definition.varMaxSecondary = buffer.int
            }
            21 -> { definition.opcode21  = buffer.int }
            22 -> { definition.opcode22  = buffer.int }
            39 -> { definition.opcode39 = buffer.uByte }
            249 -> {
                val length: Int = buffer.uByte
                (0 until length).forEach { _ ->
                    val string: Boolean = (buffer.uByte) == 1
                    val key: Int = buffer.medium
                    val value: Any = if (string) {
                        buffer.rsString
                    } else {
                        buffer.int
                    }
                    definition.params[key] = value.toString()
                }
            }
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