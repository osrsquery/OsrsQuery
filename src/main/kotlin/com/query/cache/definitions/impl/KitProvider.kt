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


data class KitDefinition(
    override val id: Int = 0,
    var recolorToReplace : ShortArray? = null,
    var recolorToFind : ShortArray? = null,
    var retextureToFind : ShortArray? = null,
    var retextureToReplace  : ShortArray? = null,
    var bodyPartId : Int = -1,
    var models : IntArray? = null,
    var chatheadModels : IntArray? = null,
): Definition {

    companion object {
        var nonSelectable : Boolean = false
    }

    fun encode(dos: DataOutputStream) {

        if (bodyPartId != -1) {
            dos.writeByte(1)
            dos.writeByte(bodyPartId)
        }

        if (models != null && models!!.isNotEmpty()) {
            dos.writeByte(2)
            dos.writeByte(models!!.size)
            for (i in 0 until models!!.size) {
                dos.writeShort(models!![i])
            }
        }

        if (recolorToReplace != null && recolorToFind != null) {
            dos.writeByte(40)
            dos.writeByte(recolorToReplace!!.size)
            for (i in 0 until recolorToReplace!!.size) {
                dos.writeShort(recolorToFind!![i].toInt())
                dos.writeShort(recolorToReplace!![i].toInt())
            }
        }

        if (retextureToReplace != null && retextureToFind != null) {
            dos.writeByte(41)
            dos.writeByte(retextureToReplace!!.size)
            for (i in 0 until retextureToReplace!!.size) {
                dos.writeShort(retextureToFind!![i].toInt())
                dos.writeShort(retextureToReplace!![i].toInt())
            }
        }

        if (chatheadModels != null) {
            dos.writeByte(60)
            dos.writeByte(chatheadModels!!.size)
            for (i in 0 until chatheadModels!!.size) {
                dos.writeShort(chatheadModels!![i])
            }
        }

        dos.writeByte(0)
    }

}

class KitProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(KitDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.IDENTKIT.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), KitDefinition(it))
        }
        return Serializable(DefinitionsTypes.KIT,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: KitDefinition): Definition {
        definition.chatheadModels = intArrayOf(-1, -1, -1, -1, -1)

        do when (val opcode: Int = buffer.uByte) {
            1 -> definition.bodyPartId = buffer.uByte
            2 -> {
                val length = buffer.uByte
                val models = IntArray(length)
                (0 until length).forEach {
                    models[it] = buffer.readBigSmart()
                }
                definition.models = models
            }
            3 -> { }
            40 -> {
                val length = buffer.uByte
                definition.recolorToFind = ShortArray(length)
                definition.recolorToReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.recolorToFind!![it] = buffer.uShort.toShort()
                    definition.recolorToReplace!![it] = buffer.uShort.toShort()
                }
            }
            41 -> {
                val length = buffer.uByte
                definition.retextureToFind = ShortArray(length)
                definition.retextureToReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.retextureToFind!![it] = buffer.uShort.toShort()
                    definition.retextureToReplace!![it] = buffer.uShort.toShort()
                }
            }
            in 60..70 -> {
                definition.chatheadModels!![opcode - 60] = buffer.readBigSmart()
            }
            0 -> break
            else -> logger.warn { "Unhandled kit definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}