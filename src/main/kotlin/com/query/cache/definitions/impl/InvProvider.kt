package com.query.cache.definitions.impl

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


data class InvDefinition(
    override val id: Int = 0,
    var size: Int = 0
): Definition

class InvProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(InvDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.INV.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), InvDefinition(it))
        }
        return Serializable(DefinitionsTypes.INVS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: InvDefinition): Definition {
        do when (val opcode: Int = buffer.get().toInt() and 0xff) {
            2 -> definition.size = buffer.short.toInt() and 0xffff
            0 -> break
            else -> logger.warn { "Unhandled inv definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}