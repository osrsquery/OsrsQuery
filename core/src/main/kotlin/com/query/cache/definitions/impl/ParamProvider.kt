package com.query.cache.definitions.impl

import com.displee.cache.index.archive.Archive
import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.definitions.Loader
import com.query.cache.definitions.Serializable
import com.query.dump.DefinitionsTypes
import com.query.cache.definitions.Definition
import com.query.utils.*
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class ParamDefinition(
    override val id: Int = 0,
    var type: Char? = null,
    var isMembers: Boolean = true,
    var defaultInt: Int = 0,
    var defaultString: String? = null
): Definition() {
    override fun encode(dos: DataOutputStream) {
        TODO("Not yet implemented")
    }
}

class ParamProvider(val latch: CountDownLatch?) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(ParamDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive: Archive = library.index(IndexType.CONFIGS).archive(ConfigType.PARAMS.id)!!
        val definitions = archive.fileIds().map {
            decode(ByteBuffer.wrap(archive.file(it)?.data), ParamDefinition(it))
        }
        return Serializable(DefinitionsTypes.PARAMS,this, definitions)
    }

    fun decode(buffer: ByteBuffer, definition: ParamDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> definition.type = (buffer.uByte).toChar()
            2 -> definition.defaultInt = buffer.int
            4 -> definition.isMembers = false
            5 -> definition.defaultString = buffer.rsString
            0 -> break
            else -> logger.warn { "Unhandled param definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }



}