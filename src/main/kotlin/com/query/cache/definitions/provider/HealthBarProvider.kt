package com.query.cache.definitions.provider

import com.query.Application.logger
import com.query.Application.objects
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
import java.util.stream.IntStream


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

class HealthBarProvider : Loader {

    override fun load(writeTypes : Boolean): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.HEALTHBAR.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), HealthBarDefinition(it))
        }
        return Serializable(CacheType.HEALTH,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: HealthBarDefinition): Definition {
        do when (val opcode: Int = buffer.get().toInt() and 0xff) {
            1 -> definition.field3276 = buffer.short.toInt() and 0xffff
            2 -> definition.field3277 = buffer.get().toInt() and 0xff
            3 -> definition.field3278 = buffer.get().toInt() and 0xff
            4 -> definition.field3283 = 0
            5 -> definition.field3275 = buffer.short.toInt() and 0xffff
            6 -> definition.field3272 = buffer.get().toInt() and 0xff
            7 -> definition.healthBarFrontSpriteId = buffer.short.toInt() and 0xffff
            8 -> definition.healthBarBackSpriteId = buffer.short.toInt() and 0xffff
            11 -> definition.field3283 = buffer.short.toInt() and 0xffff
            14 -> definition.healthScale = buffer.get().toInt() and 0xff
            15 -> definition.healthBarPadding = buffer.get().toInt() and 0xff
            0 -> break
            else -> logger.warn { "Unhandled health definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}