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
    var field1778 : Boolean = false,
    var fileIds: IntArray = IntArray(0),
    var field1780: IntArray = IntArray(0),
    var field1781: IntArray = IntArray(0),
    var field1786: IntArray = IntArray(0),
    var animationSpeed : Int = 0,
    var animationDirection : Int = 0,
    var sprite: Int = -1,
    var field1777: Int = -1
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

        definition.field1777 = buffer.short.toInt() and 0xffff
        definition.field1778 = buffer.get().toInt() != 0

        val count: Int = buffer.get().toInt()
        val files = IntArray(count)

        for (i in 0 until count) files[i] = buffer.short.toInt() and 0xffff

        definition.fileIds = files

        if (count > 1) {
            definition.field1780 = IntArray(count - 1)
            for (var3 in 0 until count - 1) {
                definition.field1780[var3] = buffer.get().toInt()
            }
        }

        if (count > 1) {
            definition.field1781 = IntArray(count - 1)
            for (var3 in 0 until count - 1) {
                definition.field1781[var3] = buffer.get().toInt()
            }
        }

        definition.field1786 = IntArray(count)

        for (var3 in 0 until count) {
            definition.field1786[var3] = buffer.int
        }

        definition.animationDirection = buffer.get().toInt()
        definition.animationSpeed = buffer.get().toInt()
        definition.sprite = definition.fileIds[0]

        return definition
    }

}