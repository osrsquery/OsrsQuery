package com.query.cache.definitions.impl

import com.displee.cache.index.archive.Archive
import com.query.Application
import com.query.Constants
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import java.io.DataOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

data class TextureDefinition(
    override var id: Int,
    var fileIds: IntArray = IntArray(0),
    var field2293: Boolean = false,
    var field2301: IntArray = IntArray(0),
    var field2296: IntArray = IntArray(0),
    var field2295: IntArray = IntArray(0),
    var animationSpeed : Int = 0,
    var animationDirection : Int = 0,
    var averageRGB: Int = -1
) : Definition {

    @Throws(IOException::class)
    fun encode(dos: DataOutputStream, texID: Map<Int, Int>) {

        dos.writeByte(1)
        dos.writeShort(id)

        if(field2293) {
            dos.writeByte(2)
            dos.writeByte(if(field2293) 1 else 0)
        }

        if(!fileIds.contentEquals(IntArray(0))) {
            dos.writeByte(3)
            dos.writeByte(fileIds.size)
        }

        if (!fileIds.contentEquals(IntArray(0))) {
            dos.writeByte(4)
            repeat(fileIds.count()) {
                dos.writeShort(texID[fileIds[it]]!!)
            }
        }

        if (!field2301.contentEquals(IntArray(0))) {
            dos.writeByte(5)
            repeat(field2301.count()) {
                dos.writeByte(field2301[it])
            }
        }

        if (!field2296.contentEquals(IntArray(0))) {
            dos.writeByte(6)
            repeat(field2296.count()) {
                dos.writeByte(field2296[it])
            }
        }

        if (!field2295.contentEquals(IntArray(0))) {
            dos.writeByte(7)
            repeat(field2295.count()) {
                dos.writeInt(field2295[it])
            }
        }

        if (animationSpeed != 0) {
            dos.writeByte(8)
            dos.writeShort(animationSpeed)
        }
        if (animationDirection != 0) {
            dos.writeByte(9)
            dos.writeShort(animationDirection)
        }
        if (averageRGB != -1) {
            dos.writeByte(10)
            dos.writeByte(averageRGB shr 16)
            dos.writeByte(averageRGB shr 8)
            dos.writeByte(averageRGB)
        }
        dos.writeByte(0)
    }
}

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
       
        definition.averageRGB = buffer.uShort
        definition.field2293 = buffer.uByte == 1
        val count: Int = buffer.uByte

        if (count in 1..4) {
            definition.fileIds = IntArray(count)
            for (index in 0 until count) {
                definition.fileIds[index] = buffer.uShort
            }
            if (count > 1) {
                definition.field2301 = IntArray(count - 1)
                for (index in 0 until count - 1) {
                    definition.field2301[index] = buffer.uByte
                }
            }
            if (count > 1) {
                definition.field2296 = IntArray(count - 1)
                for (index in 0 until count - 1) {
                    definition.field2296[index] = buffer.uByte
                }
            }
            definition.field2295 = IntArray(count)
            for (index in 0 until count) {
                definition.field2295[index] = buffer.int
            }
            definition.animationDirection = buffer.uByte
            definition.animationSpeed = buffer.uByte
        } else {
           println("Texture: ${definition.id} Out of range 1..4 [${count}]")
        }

        return definition
    }

}