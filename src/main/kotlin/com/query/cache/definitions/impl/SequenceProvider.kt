package com.query.cache.definitions.impl

import ByteBufferUtils
import com.query.Application
import com.query.Application.logger
import com.query.Constants
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import com.runetopic.cache.extension.toByteBuffer
import java.io.DataOutputStream
import java.io.IOException
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
): Definition {

    @Throws(IOException::class)
    fun encode(dos: DataOutputStream) {
        if (frameLengths != null && frameIDs != null) {
            dos.writeByte(1)
            dos.writeShort(frameLengths!!.size)
            for (i in 0 until frameLengths!!.size) {
                dos.writeShort(frameLengths!![i])
            }
            for (i in 0 until frameIDs!!.size) {
                dos.writeShort(frameIDs!![i])
            }
            for (i in 0 until frameIDs!!.size) {
                dos.writeShort(frameIDs!![i] shr 16)
            }
        }

        if (frameStep != -1) {
            dos.writeByte(2)
            dos.writeShort(frameStep)
        }

        if (interleaveLeave != null) {
            dos.writeByte(3)
            dos.writeByte(interleaveLeave!!.size - 1)
            for (i in 0 until interleaveLeave!!.size) {
                dos.writeByte(interleaveLeave!![i])
            }
        }
        if (stretches) {
            dos.writeByte(4)
        }

        if (forcedPriority != 5) {
            dos.writeByte(5)
            dos.writeByte(forcedPriority)
        }
        if (leftHandItem != -1) {
            dos.writeByte(6)
            dos.writeShort(leftHandItem)
        }
        if (rightHandItem != -1) {
            dos.writeByte(7)
            dos.writeShort(rightHandItem)
        }
        if (maxLoops != 99) {
            dos.writeByte(8)
            dos.writeByte(maxLoops)
        }
        if (precedenceAnimating != -1) {
            dos.writeByte(9)
            dos.writeByte(precedenceAnimating)
        }
        if (priority != -1) {
            dos.writeByte(10)
            dos.writeByte(priority)
        }
        if (replyMode != 2) {
            dos.writeByte(11)
            dos.writeByte(replyMode)
        }
        if (chatFrameIds != null && chatFrameIds!!.size > 0) {
            dos.writeByte(12)
            dos.writeByte(chatFrameIds!!.size)
            for (i in 0 until chatFrameIds!!.size) {
                dos.writeShort(chatFrameIds!![i])
            }
            for (i in 0 until chatFrameIds!!.size) {
                dos.writeShort(chatFrameIds!![i] shr 16)
            }
        }
        if (frameSounds != null && frameSounds!!.isNotEmpty()) {
            dos.writeByte(13)
            dos.writeByte(frameSounds!!.size)
            for (i in 0 until frameSounds!!.size) {
                dos.writeByte(frameSounds!![i] shr 16)
                dos.writeByte(frameSounds!![i] shr 8)
                dos.writeByte(frameSounds!![i])
            }
        }
        dos.writeByte(0)
    }

}

class SequenceProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader , Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(SequenceDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val definitions : MutableList<Definition> = emptyList<Definition>().toMutableList()
        Constants.store!!.index(2).use { index ->
            (0 until index.expand()).forEach {
                definitions.add(decode(index.group(12).file(it and 0xFF).data.toByteBuffer(), SequenceDefinition(it)))
            }
        }

        return Serializable(DefinitionsTypes.SEQUENCES,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: SequenceDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> {
                val length: Int = buffer.uShort
                definition.frameLengths = IntArray(length)
                (0 until length).forEach {
                    definition.frameLengths!![it] = buffer.uShort
                }
                definition.frameIDs = IntArray(length)
                (0 until length).forEach {
                    definition.frameIDs!![it] = buffer.uShort
                }
                (0 until length).forEach {
                    definition.frameIDs!![it] += (buffer.uShort) shl 16
                }
            }
            2 -> definition.frameStep = buffer.uShort
            3 -> {
                val length: Int = buffer.uByte
                definition.interleaveLeave = IntArray(length + 1)
                (0 until length).forEach {
                    definition.interleaveLeave!![it] = buffer.uByte
                }
                definition.interleaveLeave!![length] = 9_999_999
            }
            4 -> definition.stretches = true
            5 -> definition.forcedPriority = buffer.uByte
            6 -> definition.leftHandItem = buffer.uShort
            7 -> definition.rightHandItem = buffer.uShort
            8 -> definition.maxLoops = buffer.uByte
            9 -> definition.precedenceAnimating = buffer.uByte
            10 -> definition.priority = buffer.uByte
            11 -> definition.replyMode = buffer.uByte
            12 -> {
                val length: Int = buffer.uByte
                definition.chatFrameIds = IntArray(length)
                (0 until length).forEach {
                    definition.chatFrameIds!![it] = buffer.uShort
                }
                (0 until length).forEach {
                    definition.chatFrameIds!![it] += (buffer.uShort) shl 16
                }
            }
            13 -> {
                val length: Int = buffer.uByte
                definition.frameSounds = IntArray(length)
                (0 until length).forEach {
                    definition.frameSounds!![it] = buffer.medium
                }
            }
            0 -> break
            else -> logger.warn { "Unhandled seq definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }



}