package com.query.cache.definitions.impl

import com.displee.cache.index.archive.Archive
import com.query.Application
import com.query.Application.logger
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
import java.util.concurrent.CountDownLatch


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
        return Serializable(CacheType.ENUMS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: EnumDefinition): Definition {
        do when (val opcode: Int = buffer.get().toInt() and 0xff) {
            1 -> definition.keyType = (buffer.get().toInt() and 0xff).toChar()
            2 -> definition.valType = (buffer.get().toInt() and 0xff).toChar()
            3 -> definition.defaultString = ByteBufferExt.getString(buffer)
            4 -> definition.defaultInt = buffer.int
            5 -> {
                val length: Int = buffer.short.toInt() and 0xffff
                (0 until length).forEach { _ ->
                    val key = buffer.int
                    val value = ByteBufferExt.getString(buffer)
                    definition.params[key.toLong()] = value
                }
            }
            6 -> {
                val length: Int = buffer.short.toInt() and 0xffff
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