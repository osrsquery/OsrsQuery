package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import java.io.DataOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

data class VarbitDefinition(
    override val id: Int = 0,
    var index: Int = 0,
    var leastSignificantBit: Int = 0,
    var mostSignificantBit: Int = 0
): Definition {

    @Throws(IOException::class)
    fun encode(dos: DataOutputStream) {
        if (index != -1 || leastSignificantBit != -1 || mostSignificantBit != -1) {
            dos.writeByte(1)
            dos.writeShort(index)
            dos.writeByte(leastSignificantBit)
            dos.writeByte(mostSignificantBit)
        }
        dos.writeByte(0)
    }


}

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