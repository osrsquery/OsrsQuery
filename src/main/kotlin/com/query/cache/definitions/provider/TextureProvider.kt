package com.query.cache.definitions.provider

import com.displee.cache.index.archive.Archive
import com.query.Application
import com.query.Application.sprites
import com.query.Application.textures
import com.query.Constants
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.CacheType
import com.query.utils.IndexType
import com.query.utils.Sprite
import com.query.utils.index
import java.nio.ByteBuffer
import java.util.*
import kotlin.experimental.and

data class TextureDefinition(
    override var id: Int,
    var fileIds: IntArray = IntArray(0),
    var sprite: Sprite? = null
) : Definition

class TextureProvider : Loader {

    override fun load(writeTypes : Boolean): Serializable {
        val archive: Archive = Constants.library.index(IndexType.TEXTURES).first()!!

        val definitions = archive.fileIds().map {
            decode(ByteBuffer.wrap(archive.file(it)?.data), TextureDefinition(it))
        }

        return Serializable(CacheType.TEXTURES,this, definitions,writeTypes)
    }

    private fun decode(buffer: ByteBuffer, definition: TextureDefinition): Definition {
        buffer.short
        buffer.get()
        definition.fileIds = IntArray((buffer.get() and 0xff.toByte()).toInt())
        Arrays.setAll(definition.fileIds) { id -> (buffer.short and 0xffff.toShort()).toInt() }
        definition.sprite = sprites()!![definition.fileIds[0]].sprite
        return definition
    }

}