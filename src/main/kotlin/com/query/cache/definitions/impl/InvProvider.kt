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
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class InvDefinition(
    override val id: Int = 0,
    var size: Int = 0
): Definition() {

    override fun encode(dos: DataOutputStream) {
        TODO("Not yet implemented")
    }

}

class InvProvider(val latch: CountDownLatch?) : Loader, Runnable {

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
        return Serializable(DefinitionsTypes.INVS,this, definitions)
    }

    fun decode(buffer: ByteBuffer, definition: InvDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            2 -> definition.size = buffer.uShort
            0 -> break
            else -> logger.warn { "Unhandled inv definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}