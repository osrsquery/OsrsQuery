package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
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
    var actions : MutableList<String?> = mutableListOf<String?>(null, null, null, null, null),
    var isMinimapVisible : Boolean = true,
    var combatLevel : Int = -1,
    var widthScale : Int = 128,
    var heightScale : Int = 128,
    var hasRenderPriority : Boolean = false,
    var ambient : Int = 0,
    var contrast : Int = 0,
    var headIcon : Int = -1,
    var rotationSpeed : Int = 32,
    var configs: MutableList<Int>? = null,
    var varbitId : Int = -1,
    var varpIndex : Int = -1,
    var isInteractable : Boolean = true,
    var rotationFlag : Boolean = true,
    var isPet : Boolean = false,
    var params : MutableMap<Int,String> = mutableMapOf<Int, String>()
): Definition

class NpcProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(NpcDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }


    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.NPC.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), NpcDefinition(it))
        }
        return Serializable(DefinitionsTypes.NPCS,this, definitions,writeTypes)
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
            102 -> definition.headIcon = buffer.uShort
            103 -> definition.rotationSpeed = buffer.uShort
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
            109 -> definition.rotationFlag = false
            111 -> definition.isPet = true
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
            249 -> {
                val length: Int = buffer.uByte
                (0 until length).forEach { _ ->
                    val string: Boolean = (buffer.uByte) == 1
                    val key: Int = buffer.medium
                    val value: Any = if (string) {
                        buffer.rsString
                    } else {
                        buffer.int
                    }
                    definition.params[key] = value.toString()
                }
            }
            0 -> break
            else -> logger.warn { "Unhandled npc definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}