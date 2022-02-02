package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.ByteBufferExt
import com.query.utils.ConfigType
import com.query.utils.IndexType
import com.query.utils.index
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
        do when (val opcode: Int = buffer.get().toInt() and 0xff) {
            1 -> {
                val length: Int = buffer.get().toInt() and 0xff
                definition.models = IntArray(length)
                (0 until length).forEach {
                    definition.models!![it] = buffer.short.toInt() and 0xffff
                }
            }
            2 -> definition.name = ByteBufferExt.getString(buffer)
            12 -> definition.size = buffer.get().toInt() and 0xff
            13 -> definition.standingAnimation = buffer.short.toInt() and 0xffff
            14 -> definition.walkingAnimation = buffer.short.toInt() and 0xffff
            15 -> definition.rotateLeftAnimation = buffer.short.toInt() and 0xffff
            16 -> definition.rotateRightAnimation = buffer.short.toInt() and 0xffff
            17 -> {
                definition.walkingAnimation = buffer.short.toInt() and 0xffff
                definition.rotate180Animation = buffer.short.toInt() and 0xffff
                definition.rotate90RightAnimation = buffer.short.toInt() and 0xffff
                definition.rotate90LeftAnimation = buffer.short.toInt() and 0xffff
            }
            18 -> definition.category = buffer.short.toInt() and 0xffff
            in 30..34 -> {
                definition.actions[opcode - 30] = ByteBufferExt.getString(buffer)
                if (definition.actions[opcode - 30].equals("Hidden", true)) {
                    definition.actions[opcode - 30] = null
                }
            }
            40 -> {
                val length: Int = buffer.get().toInt() and 0xff
                definition.recolorToFind = ShortArray(length)
                definition.recolorToReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.recolorToFind!![it] = (buffer.short.toInt() and 0xffff).toShort()
                    definition.recolorToReplace!![it] = (buffer.short.toInt() and 0xffff).toShort()
                }
            }
            41 -> {
                val length: Int = buffer.get().toInt() and 0xff
                definition.retextureToFind = ShortArray(length)
                definition.retextureToReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.retextureToFind!![it] = (buffer.short.toInt() and 0xffff).toShort()
                    definition.retextureToReplace!![it] = (buffer.short.toInt() and 0xffff).toShort()
                }
            }
            60 -> {
                val length: Int = buffer.get().toInt() and 0xff
                definition.chatheadModels = IntArray(length)
                (0 until length).forEach {
                    definition.chatheadModels!![it] = buffer.short.toInt() and 0xffff
                }
            }
            93 -> definition.isMinimapVisible = false
            95 -> definition.combatLevel = buffer.short.toInt() and 0xffff
            97 -> definition.widthScale = buffer.short.toInt() and 0xffff
            98 -> definition.heightScale = buffer.short.toInt() and 0xffff
            99 -> definition.hasRenderPriority = true
            100 -> definition.ambient = buffer.get().toInt()
            101 -> definition.contrast = buffer.get().toInt()
            102 -> definition.headIcon = buffer.short.toInt() and 0xffff
            103 -> definition.rotationSpeed = buffer.short.toInt() and 0xffff
            106 -> {
                definition.varbitId = buffer.short.toInt() and 0xffff
                if (definition.varbitId == 65535) {
                    definition.varbitId = -1
                }
                definition.varpIndex = buffer.short.toInt() and 0xffff
                if (definition.varpIndex == 65535) {
                    definition.varpIndex = -1
                }
                val length: Int = buffer.get().toInt() and 0xff
                definition.configs = mutableListOf(length)
                (0..length).forEach {
                    val value = buffer.short.toInt() and 0xffff
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
                definition.varbitId = buffer.short.toInt() and 0xffff
                if (definition.varbitId == 65535) {
                    definition.varbitId = -1
                }
                definition.varpIndex = buffer.short.toInt() and 0xffff
                if (definition.varpIndex == 65535) {
                    definition.varpIndex = -1
                }
                var i: Int = buffer.short.toInt() and 0xffff
                if (i == 0xffff) {
                    i = -1
                }
                val length: Int = buffer.get().toInt() and 0xff
                definition.configs = mutableListOf(length - 1)
                (0..length).forEach {
                    definition.configs!!.add(buffer.short.toInt() and 0xffff)
                    if (definition.configs!![it].toChar() == '\uffff') {
                        definition.configs!![it] = -1
                    }
                }
                definition.configs!![length + 1] = i
            }
            249 -> {
                val length: Int = buffer.get().toInt() and 0xff
                (0 until length).forEach { _ ->
                    val string: Boolean = (buffer.get().toInt() and 0xff) == 1
                    val key: Int = ByteBufferExt.getMedium(buffer)
                    val value: Any = if (string) {
                        ByteBufferExt.getString(buffer)
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