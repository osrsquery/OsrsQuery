package com.query.cache.definitions.impl

import com.displee.cache.index.archive.Archive
import com.query.Application
import com.query.Constants
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.IndexType
import com.query.utils.index
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.experimental.and

data class TextureDefinition(
    override var id: Int,
    var fileIds: IntArray = IntArray(0),
    var sprite: Int = -1
) : Definition

class TextureProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(TextureDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive: Archive = Constants.library.index(IndexType.TEXTURES).first()!!

        val definitions = archive.fileIds().map {
            decode(ByteBuffer.wrap(archive.file(it)?.data), TextureDefinition(it))
        }

        return Serializable(DefinitionsTypes.TEXTURES,this, definitions,writeTypes)
    }

    private fun decode(buffer: ByteBuffer, definition: TextureDefinition): Definition {
        buffer.short
        buffer.get()
        definition.fileIds = IntArray((buffer.get() and 0xff.toByte()).toInt())
        Arrays.setAll(definition.fileIds) { id -> (buffer.short and 0xffff.toShort()).toInt() }
        definition.sprite = definition.fileIds[0]
        return definition
    }

}