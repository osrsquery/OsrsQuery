package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class HealthBarDefinition(
    override val id: Int = 0,
    var field3276: Int = 0,
    var field3277: Int = 255,
    var field3278: Int = 255,
    var field3283: Int = -1,
    var field3272: Int = 1,
    var field3275: Int = 70,
    var healthBarFrontSpriteId: Int = -1,
    var healthBarBackSpriteId: Int = -1,
    var healthScale: Int = 30,
    var healthBarPadding: Int = 0
): Definition

class HealthBarProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader,Runnable {

    override val revisionMin = 189

    override fun run() {
        if(ignore()) {
            latch?.countDown()
            return
        }
        val start: Long = System.currentTimeMillis()
        Application.store(HealthBarDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.HEALTHBAR.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), HealthBarDefinition(it))
        }
        return Serializable(DefinitionsTypes.HEALTH,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: HealthBarDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> definition.field3276 = buffer.uShort
            2 -> definition.field3277 = buffer.uByte
            3 -> definition.field3278 = buffer.uByte
            4 -> definition.field3283 = 0
            5 -> definition.field3275 = buffer.uShort
            6 -> definition.field3272 = buffer.uByte
            7 -> definition.healthBarFrontSpriteId = buffer.uShort
            8 -> definition.healthBarBackSpriteId = buffer.uShort
            11 -> definition.field3283 = buffer.uShort
            14 -> definition.healthScale = buffer.uByte
            15 -> definition.healthBarPadding = buffer.uByte
            0 -> break
            else -> logger.warn { "Unhandled health definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}