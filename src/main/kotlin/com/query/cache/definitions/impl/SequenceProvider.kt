package com.query.cache.definitions.impl

import com.displee.cache.index.archive.Archive
import com.query.Application
import com.query.Application.logger
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
import java.util.concurrent.CountDownLatch


data class SequenceDefinition(
    override val id: Int = 0,
    var frameIDs: IntArray? = null,
    var chatFrameIds: IntArray? = null,
    var frameLengths: IntArray? = null,
    var frameSounds: IntArray? = null,
    var frameStep: Int = -1,
    var interleaveLeave: IntArray? = null,
    var stretches: Boolean = false,
    var forcedPriority: Int = 5,
    var leftHandItem: Int = -1,
    var rightHandItem: Int = -1,
    var maxLoops: Int = 99,
    var precedenceAnimating: Int = -1,
    var priority: Int = -1,
    var replyMode: Int = 2
): Definition

class SequenceProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader , Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(SequenceDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive: Archive = library.index(IndexType.CONFIGS).archive(ConfigType.SEQUENCE.id)!!
        val definitions = archive.fileIds().map {
            decode(ByteBuffer.wrap(archive.file(it)?.data), SequenceDefinition(it))
        }
        return Serializable(CacheType.SEQUENCES,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: SequenceDefinition): Definition {
        do when (val opcode: Int = buffer.get().toInt() and 0xff) {
            1 -> {
                val length: Int = buffer.short.toInt() and 0xffff
                definition.frameLengths = IntArray(length)
                (0 until length).forEach {
                    definition.frameLengths!![it] = buffer.short.toInt() and 0xffff
                }
                definition.frameIDs = IntArray(length)
                (0 until length).forEach {
                    definition.frameIDs!![it] = buffer.short.toInt() and 0xffff
                }
                (0 until length).forEach {
                    definition.frameIDs!![it] += (buffer.short.toInt() and 0xffff) shl 16
                }
            }
            2 -> definition.frameStep = buffer.short.toInt() and 0xffff
            3 -> {
                val length: Int = buffer.get().toInt() and 0xff
                definition.interleaveLeave = IntArray(length + 1)
                (0 until length).forEach {
                    definition.interleaveLeave!![it] = buffer.get().toInt() and 0xff
                }
                definition.interleaveLeave!![length] = 9_999_999
            }
            4 -> definition.stretches = true
            5 -> definition.forcedPriority = buffer.get().toInt() and 0xff
            6 -> definition.leftHandItem = buffer.short.toInt() and 0xffff
            7 -> definition.rightHandItem = buffer.short.toInt() and 0xffff
            8 -> definition.maxLoops = buffer.get().toInt() and 0xff
            9 -> definition.precedenceAnimating = buffer.get().toInt() and 0xff
            10 -> definition.priority = buffer.get().toInt() and 0xff
            11 -> definition.replyMode = buffer.get().toInt() and 0xff
            12 -> {
                val length: Int = buffer.get().toInt() and 0xff
                definition.chatFrameIds = IntArray(length)
                (0 until length).forEach {
                    definition.chatFrameIds!![it] = buffer.short.toInt() and 0xffff
                }
                (0 until length).forEach {
                    definition.chatFrameIds!![it] += (buffer.short.toInt() and 0xffff) shl 16
                }
            }
            13 -> {
                val length: Int = buffer.get().toInt() and 0xff
                definition.frameSounds = IntArray(length)
                (0 until length).forEach {
                    definition.frameSounds!![it] = ByteBufferExt.getMedium(buffer)
                }
            }
            0 -> break
            else -> logger.warn { "Unhandled seq definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }



}