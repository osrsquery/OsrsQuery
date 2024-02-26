package com.query.cache.definitions.impl

import com.displee.cache.index.archive.Archive
import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.definitions.Definition
import com.query.cache.definitions.Loader
import com.query.cache.definitions.Serializable
import com.query.cache.definitions.Sound
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import java.io.DataOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class SequenceDefinition(
    override val id: Int = 0,
    var frameIDs: IntArray? = null,
    var chatFrameIds: IntArray? = null,
    var frameLengths: IntArray? = null,
    var frameSounds: Array<Sound?> = emptyArray(),
    var frameStep: Int = -1,
    var interleaveLeave: IntArray? = null,
    var stretches: Boolean = false,
    var forcedPriority: Int = 5,
    var leftHandItem: Int = -1,
    var rightHandItem: Int = -1,
    var maxLoops: Int = 99,
    var precedenceAnimating: Int = -1,
    var priority: Int = -1,
    var skeletalId: Int = -1,
    var skeletalRangeBegin: Int = -1,
    var skeletalRangeEnd: Int = -1,
    var replyMode: Int = 2,
    var skeletalSounds: MutableMap<Int, Sound> = emptyMap<Int, Sound>().toMutableMap(),
    var mask: BooleanArray? = null,
) : Definition() {

    @Throws(IOException::class)
    override fun encode(dos: DataOutputStream) {
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
            for (index in 0 until interleaveLeave!!.size - 1) {
                dos.writeByte(interleaveLeave!![index])
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

        if (chatFrameIds != null && chatFrameIds!!.isNotEmpty()) {
            dos.writeByte(12)
            dos.writeByte(chatFrameIds!!.size)
            for (i in 0 until chatFrameIds!!.size) {
                dos.writeShort(chatFrameIds!!.get(i))
            }
            for (i in 0 until chatFrameIds!!.size) {
                dos.writeShort(chatFrameIds!!.get(i) shr 16)
            }
        }

        if (frameSounds.isNotEmpty()) {
            dos.writeByte(13)
            dos.writeByte(frameSounds.size)
            if (revisionIsOrBefore(119)) {
                frameSounds.filterNotNull().forEach {
                    val payload: Int = (it.location and 15) or ((it.id shl 8) or (it.loops shl 4 and 7))
                    dos.write24bitInt(payload)
                }
            } else {
                dos.writeByte(frameSounds.size)
                frameSounds.filterNotNull().forEach {
                    dos.writeShort(it.id)
                    dos.writeByte(it.loops)
                    dos.writeByte(it.location)
                    dos.writeByte(it.retain)
                }
            }
        }

        if (skeletalId != -1) {
            dos.writeByte(14)
            dos.writeInt(skeletalId)
        }

        if (skeletalSounds.isNotEmpty()) {
            dos.writeByte(15)
            dos.writeShort(skeletalSounds.size)
            if (revisionIsOrBefore(119)) {
                skeletalSounds.forEach {
                    dos.writeShort(it.key)
                    val payload: Int = (it.value.location and 15) or (it.value.id shl 8) or (it.value.loops shl 4 and 7)
                    dos.write24bitInt(payload)
                }
            } else {
                skeletalSounds.forEach {
                    dos.writeShort(it.key)
                    dos.writeByte(it.value.id)
                    dos.writeByte(it.value.location)
                    dos.writeByte(it.value.retain)
                }
            }
        }

        if (skeletalRangeBegin != -1 || skeletalRangeEnd != -1) {
            dos.writeByte(16)
            dos.writeShort(skeletalRangeBegin)
            dos.writeShort(skeletalRangeEnd)
        }

        if (mask != null) {
            dos.writeByte(17)


            dos.writeByte(mask!!.filter { it }.size)
            mask!!.forEachIndexed { index, state ->
                if (state) {
                    dos.writeByte(index)
                }
            }
        }

        dos.writeByte(0)
    }


}

class SequenceProvider(val latch: CountDownLatch?) : Loader, Runnable {

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
        return Serializable(DefinitionsTypes.SEQUENCES, this, definitions)
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
                definition.frameSounds = arrayOfNulls(length)

                for (var4 in 0 until length) {
                    definition.frameSounds[var4] = Sound.readFrameSound(buffer, revisionIsOrBefore(119))
                }
            }

            14 -> definition.skeletalId = buffer.int
            15 -> {
                val size: Int = buffer.uShort
                definition.skeletalSounds = emptyMap<Int, Sound>().toMutableMap()
                for (index in 0 until size) {
                    val frame: Int = buffer.uShort
                    val sound = Sound.readFrameSound(buffer, revisionIsOrBefore(119))
                    if (sound != null) {
                        definition.skeletalSounds[frame] = sound
                    }
                }
            }

            16 -> {
                definition.skeletalRangeBegin = buffer.uShort
                definition.skeletalRangeEnd = buffer.uShort
            }

            17 -> {
                definition.mask = BooleanArray(256)
                repeat(definition.mask!!.size) {
                    definition.mask!![it] = false
                }
                val count = buffer.uByte
                repeat(count) {
                    definition.mask!![buffer.uByte] = true
                }

            }

            0 -> break
            else -> logger.warn { "Unhandled seq definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}