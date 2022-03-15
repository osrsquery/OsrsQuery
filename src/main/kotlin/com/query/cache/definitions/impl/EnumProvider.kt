package com.query.cache.definitions.impl

import com.displee.cache.index.archive.Archive
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
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import com.query.utils.*

data class EnumDefinition(
    override val id: Int = 0,
    var keyType: Char? = null,
    var valType: Char? = null,
    var defaultString: String = "null",
    var defaultInt: Int = 0,
    var params : MutableMap<Long,String> = mutableMapOf()
): Definition

class EnumProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(EnumDefinition::class.java,load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive: Archive = library.index(IndexType.CONFIGS).archive(ConfigType.ENUM.id)!!
        val definitions = archive.fileIds().map {
            decode(ByteBuffer.wrap(archive.file(it)?.data), EnumDefinition(it))
        }
        return Serializable(DefinitionsTypes.ENUMS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: EnumDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> definition.keyType = (buffer.uByte).toChar()
            2 -> definition.valType = (buffer.uByte).toChar()
            3 -> definition.defaultString = buffer.rsString
            4 -> definition.defaultInt = buffer.int
            5 -> {
                val length: Int = buffer.uShort
                (0 until length).forEach { _ ->
                    val key = buffer.int
                    val value = buffer.rsString
                    definition.params[key.toLong()] = value
                }
            }
            6 -> {
                val length: Int = buffer.uShort
                (0 until length).forEach { _ ->
                    val key = buffer.int
                    val value: Int = buffer.int
                    definition.params[key.toLong()] = value.toString()
                }
            }
            0 -> break
            else -> logger.warn { "Unhandled enum definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }



}