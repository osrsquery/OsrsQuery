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
import com.query.utils.*

data class VarbitDefinition(
    override val id: Int = 0,
    var index: Int = 0,
    var leastSignificantBit: Int = 0,
    var mostSignificantBit: Int = 0
): Definition

class VarbitProvider(val latch: CountDownLatch?, val writeTypes : Boolean = false) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(VarbitDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.VARBIT.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), VarbitDefinition(it))
        }
        return Serializable(DefinitionsTypes.VARBITS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: VarbitDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> {
                definition.index = buffer.uShort
                definition.leastSignificantBit = buffer.uByte
                definition.mostSignificantBit = buffer.uByte
            }
            0 -> break
            else -> logger.warn { "Unhandled varbit definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}