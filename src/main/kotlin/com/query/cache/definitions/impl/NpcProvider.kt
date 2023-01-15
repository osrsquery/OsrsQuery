package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.definitions.Definition
import com.query.cache.definitions.Loader
import com.query.cache.definitions.Serializable
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import java.io.DataOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

data class NpcDefinition(
    override val id: Int = 0,
    var name: String = "null",
    var size : Int = 1,
    var category : Int = -1,
    var models: IntArray? = null,
    var chatheadModels: IntArray? = null,
    var standingAnimation : Int = -1,
    var rotateLeftAnimation : Int = -1,
    var rotateRightAnimation : Int = -1,
    var walkingAnimation : Int = -1,
    var rotate180Animation : Int = -1,
    var rotate90RightAnimation : Int = -1,
    var rotate90LeftAnimation : Int = -1,
    var recolorToFind: ShortArray? = null,
    var recolorToReplace: ShortArray? = null,
    var retextureToFind: ShortArray? = null,
    var retextureToReplace: ShortArray? = null,
    var actions : MutableList<String?> = mutableListOf(null, null, null, null, null),
    var isMinimapVisible : Boolean = true,
    var combatLevel : Int = -1,
    var widthScale : Int = 128,
    var heightScale : Int = 128,
    var hasRenderPriority : Boolean = false,
    var ambient : Int = 0,
    var contrast : Int = 0,
    var headIconArchiveIds: IntArray? = null,
    var headIconSpriteIndex: IntArray? = null,
    var rotation : Int = 32,
    var configs: MutableList<Int>? = null,
    var varbitId : Int = -1,
    var varpIndex : Int = -1,
    var isInteractable : Boolean = true,
    var isClickable : Boolean = true,
    var isPet : Boolean = false,
    var runSequence : Int = -1,
    var runBackSequence : Int = -1,
    var runRightSequence : Int = -1,
    var runLeftSequence : Int = -1,
    var crawlSequence : Int = -1,
    var crawlBackSequence : Int = -1,
    var crawlRightSequence : Int = -1,
    var crawlLeftSequence : Int = -1,
    var params : MutableMap<Int,String> = mutableMapOf()
): Definition() {

    @Throws(IOException::class)
    override fun encode(dos: DataOutputStream) {

        if (models != null && models!!.isNotEmpty()) {
            dos.writeByte(1)
            dos.writeByte(models!!.size)
            for (i in models!!.indices) {
                dos.writeShort(models!![i])
            }
        }

        if (name != "null") {
            dos.writeByte(2)
            dos.writeString(name)
        }

        if (size != -1) {
            dos.writeByte(12)
            dos.writeByte(size)
        }

        if (standingAnimation != -1) {
            dos.writeByte(13)
            dos.writeShort(standingAnimation)
        }

        if (walkingAnimation != -1) {
            dos.writeByte(14)
            dos.writeShort(walkingAnimation)
        }

        if (rotateLeftAnimation != -1) {
            dos.writeByte(15)
            dos.writeShort(rotateLeftAnimation)
        }

        if (rotateRightAnimation != -1) {
            dos.writeByte(16)
            dos.writeShort(rotateRightAnimation)
        }

        if (walkingAnimation != -1 || rotate180Animation != -1 || rotate90RightAnimation != -1 || rotate90LeftAnimation != -1) {
            dos.writeByte(17)
            dos.writeShort(walkingAnimation)
            dos.writeShort(rotate180Animation)
            dos.writeShort(rotate90RightAnimation)
            dos.writeShort(rotate90LeftAnimation)
        }

        if (category != -1) {
            dos.writeByte(18)
            dos.writeShort(category)
        }

        if (actions.any { it != null }) {
            for (i in 0 until actions.size) {
                if (actions[i] == null) {
                    continue
                }
                dos.writeByte(30 + i)
                dos.writeString(actions[i]!!)
            }
        }

        if (recolorToFind != null && recolorToReplace != null) {
            dos.writeByte(40)
            dos.writeByte(recolorToFind!!.size)
            repeat(recolorToFind!!.size) {
                dos.writeShort(recolorToFind!![it].toInt())
                dos.writeShort(recolorToReplace!![it].toInt())
            }
        }

        if (retextureToFind != null && retextureToReplace != null) {
            dos.writeByte(41)
            dos.writeByte(retextureToFind!!.size)
            repeat(retextureToFind!!.size) {
                dos.writeShort(retextureToFind!![it].toInt())
                dos.writeShort(retextureToReplace!![it].toInt())
            }
        }

        if (chatheadModels != null) {
            dos.writeByte(60)
            dos.writeByte(chatheadModels!!.size)
            for (i in chatheadModels!!.indices) {
                dos.writeShort(chatheadModels!![i])
            }
        }

        if (!isMinimapVisible) {
            dos.writeByte(93)
        }
        if (combatLevel != -1) {
            dos.writeByte(95)
            dos.writeShort(combatLevel)
        }

        if (widthScale != 128) {
            dos.writeByte(97)
            dos.writeShort(widthScale)
        }

        if (heightScale != 128) {
            dos.writeByte(98)
            dos.writeShort(heightScale)
        }

        if (hasRenderPriority) {
            dos.writeByte(99)
        }

        if (ambient != 0) {
            dos.writeByte(100)
            dos.writeByte(ambient)
        }

        if (contrast != 0) {
            dos.writeByte(101)
            dos.writeByte(contrast)
        }

        if (headIconArchiveIds != null) {
            dos.writeByte(102)
            dos.writeShort(headIconArchiveIds!!.size)
            repeat(headIconArchiveIds!!.size) {
                dos.writeShort(headIconArchiveIds!![it])
                dos.writeShort(headIconSpriteIndex!![it])
            }
        }

        if (rotation != 32) {
            dos.writeByte(103)
            dos.writeShort(rotation)
        }

        if ((varbitId != -1 || varpIndex != -1) && configs != null) {
            val `var` = configs!![configs!!.size - 1]
            dos.writeByte(if (`var` != -1) 118 else 106)
            dos.writeShort(varbitId)
            dos.writeShort(varpIndex)
            if (`var` != -1) {
                dos.writeShort(`var`)
            }
            dos.writeByte(configs!!.size - 2)
            for (i in 0..configs!!.size - 2) {
                dos.writeShort(configs!![i])
            }
        }

        if (!isInteractable) {
            dos.writeByte(107)
        }

        if (!isClickable) {
            dos.writeByte(109)
        }

        if (isPet) {
            dos.writeByte(111)
        }

        if (runSequence != -1) {
            dos.writeByte(114)
            dos.writeShort(runSequence)
        }

        if (runSequence != -1) {
            dos.writeByte(115)
            dos.writeShort(runSequence)
            dos.writeShort(runBackSequence)
            dos.writeShort(runRightSequence)
            dos.writeShort(runLeftSequence)
        }

        if (crawlSequence != -1) {
            dos.writeByte(116)
            dos.writeShort(crawlSequence)
        }

        if (crawlSequence != -1) {
            dos.writeByte(117)
            dos.writeShort(crawlSequence)
            dos.writeShort(crawlBackSequence)
            dos.writeShort(crawlRightSequence)
            dos.writeShort(crawlLeftSequence)
        }

        if (params != mutableMapOf<Int, String>()) {
            dos.writeByte(249)
            dos.writeParams(params)
        }

        dos.writeByte(0)
    }

}

class NpcProvider(val latch: CountDownLatch?) : Loader, Runnable {

    override val revisionMin = 1

    var archiveRevision = 1
    val REV_210_NPC_ARCHIVE_REV = 1493

    fun newHeadIcons() = archiveRevision >= REV_210_NPC_ARCHIVE_REV

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(NpcDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }


    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.NPC.id)!!
        archiveRevision = archive.revision
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), NpcDefinition(it))
        }
        return Serializable(DefinitionsTypes.NPCS,this, definitions)
    }

    fun decode(buffer: ByteBuffer, definition: NpcDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> {
                val length: Int = buffer.uByte
                definition.models = IntArray(length)
                (0 until length).forEach {
                    definition.models!![it] = buffer.uShort
                }
            }
            2 -> definition.name = buffer.rsString
            12 -> definition.size = buffer.uByte
            13 -> definition.standingAnimation = buffer.uShort
            14 -> definition.walkingAnimation = buffer.uShort
            15 -> definition.rotateLeftAnimation = buffer.uShort
            16 -> definition.rotateRightAnimation = buffer.uShort
            17 -> {
                definition.walkingAnimation = buffer.uShort
                definition.rotate180Animation = buffer.uShort
                definition.rotate90RightAnimation = buffer.uShort
                definition.rotate90LeftAnimation = buffer.uShort
            }
            18 -> definition.category = buffer.uShort
            in 30..34 -> {
                definition.actions[opcode - 30] = buffer.rsString
                if (definition.actions[opcode - 30].equals("Hidden", true)) {
                    definition.actions[opcode - 30] = null
                }
            }
            40 -> {
                val length: Int = buffer.uByte
                definition.recolorToFind = ShortArray(length)
                definition.recolorToReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.recolorToFind!![it] = (buffer.uShort).toShort()
                    definition.recolorToReplace!![it] = (buffer.uShort).toShort()
                }
            }
            41 -> {
                val length: Int = buffer.uByte
                definition.retextureToFind = ShortArray(length)
                definition.retextureToReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.retextureToFind!![it] = (buffer.uShort).toShort()
                    definition.retextureToReplace!![it] = (buffer.uShort).toShort()
                }
            }
            60 -> {
                val length: Int = buffer.uByte
                definition.chatheadModels = IntArray(length)
                (0 until length).forEach {
                    definition.chatheadModels!![it] = buffer.uShort
                }
            }
            93 -> definition.isMinimapVisible = false
            95 -> definition.combatLevel = buffer.uShort
            97 -> definition.widthScale = buffer.uShort
            98 -> definition.heightScale = buffer.uShort
            99 -> definition.hasRenderPriority = true
            100 -> definition.ambient = buffer.byte.toInt()
            101 -> definition.contrast = buffer.byte.toInt()
            102 -> {
                if (!newHeadIcons()) {
                    definition.headIconArchiveIds = intArrayOf(-1)
                    definition.headIconSpriteIndex = IntArray(buffer.uShort)
                } else {
                    val bitfield = buffer.uByte
                    var size = 0

                    var pos = bitfield
                    while (pos != 0) {
                        ++size
                        pos = pos shr 1
                    }
                    definition.headIconArchiveIds = IntArray(size)
                    definition.headIconSpriteIndex = IntArray(size)

                    for (i in 0 until size) {
                        if (bitfield and (1 shl i) == 0) {
                            definition.headIconArchiveIds!![i] = -1
                            definition.headIconSpriteIndex!![i] = -1
                        } else {
                            definition.headIconArchiveIds!![i] = buffer.uShort
                            definition.headIconSpriteIndex!![i] = buffer.shortSmart - 1
                        }
                    }


                }
            }
            103 -> definition.rotation = buffer.uShort
            106 -> {
                definition.varbitId = buffer.uShort
                if (definition.varbitId == 65535) {
                    definition.varbitId = -1
                }
                definition.varpIndex = buffer.uShort
                if (definition.varpIndex == 65535) {
                    definition.varpIndex = -1
                }
                val length: Int = buffer.uByte
                definition.configs = mutableListOf(length)
                (0..length).forEach {
                    val value = buffer.uShort
                    definition.configs!!.add(value);
                    if (definition.configs!![it].toChar() == '\uffff') {
                        definition.configs!![it] = -1
                    }
                }
                definition.configs!![length + 1] = -1
            }
            107 -> definition.isInteractable = false
            109 -> definition.isClickable = false
            111 -> definition.isPet = true
            114 -> definition.runSequence = buffer.uShort
            115 -> {
                definition.runSequence = buffer.uShort
                definition.runBackSequence = buffer.uShort
                definition.runRightSequence = buffer.uShort
                definition.runLeftSequence = buffer.uShort
            }
            116 -> definition.crawlSequence = buffer.uShort
            117 -> {
                definition.crawlSequence = buffer.uShort
                definition.crawlBackSequence = buffer.uShort
                definition.crawlRightSequence = buffer.uShort
                definition.crawlLeftSequence = buffer.uShort
            }
            118 -> {
                definition.varbitId = buffer.uShort
                if (definition.varbitId == 65535) {
                    definition.varbitId = -1
                }
                definition.varpIndex = buffer.uShort
                if (definition.varpIndex == 65535) {
                    definition.varpIndex = -1
                }
                var i: Int = buffer.uShort
                if (i == 0xffff) {
                    i = -1
                }
                val length: Int = buffer.uByte
                definition.configs = mutableListOf(length - 1)
                (0..length).forEach {
                    definition.configs!!.add(buffer.uShort)
                    if (definition.configs!![it].toChar() == '\uffff') {
                        definition.configs!![it] = -1
                    }
                }
                definition.configs!![length + 1] = i
            }
            249 -> buffer.readParams()
            0 -> break
            else -> logger.warn { "Unhandled npc definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}