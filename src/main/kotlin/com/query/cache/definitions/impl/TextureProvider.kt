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
import java.util.concurrent.CountDownLatch
import com.query.utils.*
import com.runetopic.cache.extension.toByteBuffer

data class TextureDefinition(
    override var id: Int,
    var field2332 : Boolean = false,
    var fileIds: IntArray = IntArray(0),
    var field2334: IntArray = IntArray(0),
    var field2335: IntArray = IntArray(0),
    var field2329: IntArray = IntArray(0),
    var animationSpeed : Int = 0,
    var animationDirection : Int = 0,
    var sprite: Int = -1,
    var averageRGB: Int = -1
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
        val definitions : MutableList<Definition> = emptyList<Definition>().toMutableList()
        Constants.store!!.index(9).use { index ->
            (0 until index.expand()).forEach {
                definitions.add(decode(index.group(it ushr 8).file(it and 0xFF).data.toByteBuffer(), TextureDefinition(it)))
            }
        }

        return Serializable(DefinitionsTypes.TEXTURES,this, definitions,writeTypes)
    }

    private fun decode(buffer: ByteBuffer, definition: TextureDefinition): Definition {

        val count: Int = buffer.byte.toInt()
        println("Count: ${count}")
        if(count == 0) {
            return TextureDefinition(3);
        }
        val files = IntArray(count)

        for (i in 0 until count) files[i] = buffer.uShort

        definition.fileIds = files

        if (count > 1) {
            definition.field2334 = IntArray(count - 1)
            for (var3 in 0 until count - 1) {
                definition.field2334[var3] = buffer.byte.toInt()
            }
        }

        if (count > 1) {
            definition.field2335 = IntArray(count - 1)
            for (var3 in 0 until count - 1) {
                definition.field2335[var3] = buffer.byte.toInt()
            }
        }

        definition.field2329 = IntArray(count)

        for (var3 in 0 until count) {
            definition.field2329[var3] = buffer.int
        }

        definition.sprite = definition.fileIds[0]

        return definition
    }

}