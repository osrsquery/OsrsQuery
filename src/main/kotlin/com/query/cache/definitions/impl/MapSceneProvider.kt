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
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

data class MapSceneDefinition(
    override val id: Int = 0,
    var spriteId : Int = -1,
    var spritesubindex: Int = -1,
): Definition {

    fun encode(dos: DataOutputStream) {

        if (spriteId != -1) {
            dos.writeByte(1)
            dos.writeShort(spriteId)
        }

        if (spritesubindex != -1) {
            dos.writeByte(2)
            dos.writeByte(spritesubindex shr 16)
            dos.writeByte(spritesubindex shr 8)
            dos.writeByte(spritesubindex)
        }

        dos.writeByte(0)
    }

}

class MapSceneProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override fun run() {

        val start: Long = System.currentTimeMillis()
        Application.store(MapSceneDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.MAP_SPRITES.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), MapSceneDefinition(it))
        }
        return Serializable(DefinitionsTypes.MAPSCENE,this, definitions,writeTypes)
    }

    override val revisionMin: Int = 0

    fun decode(buffer: ByteBuffer, definition: MapSceneDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> definition.spriteId = buffer.uShort
            2 -> definition.spritesubindex = buffer.medium
            3 -> { }
            4 -> { }
            0 -> break
            else -> logger.warn { "Unhandled map scene definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }

}