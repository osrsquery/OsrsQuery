package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import com.runetopic.cache.extension.toByteBuffer
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class NpcDefinition(
    override val id: Int = 0,
    var aBoolean812: Boolean = true,
    var specialByte: Int = 0,
    var anInt803: Int = -1,
    var aByte806: Byte = -96,
    var renderEmote: Int = -1,
    var moveSpeed: Int = -1,
    var aShort834: Short = 0,
    var anInt847: Int = 0,
    var anInt846: Int = -1,
    var anInt830: Int = -1,
    var anInt831: Int = -1,
    var aByte833: Byte = -1,
    var aByte855: Byte = 0,
    var anInt823: Int = -1,
    var walkMask: Byte = 0,
    var anInt857: Int = -1,
    var anInt861: Int = -1,
    var respawnDirection: Byte = 4,
    var varpId: Int = -1,
    var anInt837: Int = 256,
    var anInt864: Int = -1,
    var aBoolean869: Boolean = false,
    var anInt828: Int = 0,
    var anInt848: Int = 256,
    var anInt852: Int = -1,
    var aShort865: Short = 0,
    var options: Array<String?> = arrayOfNulls(5),
    var quests: ShortArray? = null,
    var anInt854: Int = -1,
    var aBoolean875: Boolean = false,
    var anInt808: Int = 255,
    var aByteArray866: ByteArray? = null,
    var configChangeDest: IntArray? = null,
    var aByte804: Byte = -16,
    var anIntArrayArray845: Array<IntArray?>? = null,
    var anInt817: Int = -1,
    var aByte821: Byte = 0,
    var aByte824: Byte = 0,
    var aByte843: Byte = 0,
    var aBoolean809: Boolean = false,
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
    var params: MutableMap<Int, Any> = mutableMapOf(),
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
        val definitions : MutableList<Definition> = emptyList<Definition>().toMutableList()
        Constants.store!!.index(IndexType.NPCS.number).use { index ->
            (0 until index.expand()).forEach {
                definitions.add(decode(index.group(it ushr 8).file(it and 0xFF).data.toByteBuffer(), NpcDefinition(it)))
            }
        }
        return Serializable(DefinitionsTypes.NPCS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: NpcDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            0 -> break
            1 -> {
                val size = buffer.readUnsignedByte()
                val models = IntArray(size)
                (0 until size).forEach {
                    models[it] = buffer.readBigSmart()
                    if (models[it] == 65535) {
                        models[it] = -1
                    }
                }
                definition.models = models
            }
            2 -> definition.name = buffer.readString()
            12 -> definition.size = buffer.readUnsignedByte()
            in 30..34 -> definition.options[opcode - 30] = buffer.readString()
            40 -> {
                val size = buffer.readUnsignedByte()
                val colorToFind = ShortArray(size)
                val colorToReplace = ShortArray(size)
                (0 until size).forEach {
                    colorToFind[it] = buffer.readUnsignedShort().toShort()
                    colorToReplace[it] = buffer.readUnsignedShort().toShort()
                }
                definition.recolorToFind = colorToFind
                definition.recolorToReplace = colorToReplace
            }
            41 -> {
                val size = buffer.readUnsignedByte()
                val textureToFind = ShortArray(size)
                val textureToReplace = ShortArray(size)
                (0 until size).forEach {
                    textureToFind[it] = buffer.readUnsignedShort().toShort()
                    textureToReplace[it] = buffer.readUnsignedShort().toShort()
                }
                definition.retextureToFind = textureToFind
                definition.retextureToReplace = textureToReplace
            }
            42 -> {
                val size = buffer.readUnsignedByte()
                val aByteArray866 = ByteArray(size)
                (0 until size).forEach {
                    aByteArray866[it] = buffer.get()
                }
                definition.aByteArray866 = aByteArray866
            }
            60 -> {
                val size = buffer.readUnsignedByte()
                val chatheadModels = IntArray(size)
                (0 until size).forEach {
                    chatheadModels[it] = buffer.readBigSmart()
                }
                definition.chatheadModels = chatheadModels
            }
            93 -> definition.isMinimapVisible = false
            95 -> definition.combatLevel = buffer.readUnsignedShort()
            97 -> definition.widthScale = buffer.readUnsignedShort()
            98 -> definition.heightScale = buffer.readUnsignedShort()
            99 -> definition.hasRenderPriority = true
            100 -> definition.ambient = buffer.get().toInt()
            101 -> definition.contrast = buffer.get().toInt()
            102 -> definition.headIcon = buffer.readUnsignedShort()
            103 -> definition.rotationSpeed = buffer.readUnsignedShort()
            106, 118 -> {
                definition.varbitId = buffer.readUnsignedShort()
                if (definition.varbitId == 65535) {
                    definition.varbitId = -1
                }
                definition.varpId = buffer.readUnsignedShort()
                if (definition.varpId == 65535) {
                    definition.varpId = -1
                }
                var value = -1
                if (opcode == 118) {
                    value = buffer.readUnsignedShort()
                    if (value == 65535) {
                        value = -1
                    }
                }
                val size = buffer.readUnsignedByte()
                val configChangeDest = IntArray(size + 2)
                (0..size).forEach {
                    configChangeDest[it] = buffer.readUnsignedShort()
                    if (configChangeDest[it] == 65535) {
                        configChangeDest[it] = -1
                    }
                }
                configChangeDest[size + 1] = value
                definition.configChangeDest = configChangeDest
            }
            107 -> definition.isInteractable = false
            109 -> definition.rotationFlag = false
            111 -> definition.aBoolean812 = false
            113 -> {
                definition.aShort865 = buffer.readUnsignedShort().toShort()
                definition.aShort834 = buffer.readUnsignedShort().toShort()
            }
            114 -> {
                definition.aByte806 = buffer.get()
                definition.aByte804 = buffer.get()
            }
            119 -> definition.walkMask = buffer.get()
            121 -> {
                val anIntArrayArray845 = arrayOfNulls<IntArray>(definition.models!!.size)
                val size = buffer.readUnsignedByte()
                (0 until size).forEach {
                    val i_98_ = buffer.readUnsignedByte()
                    val data = (IntArray(3).also { anIntArrayArray845[i_98_] = it })
                    data[0] = buffer.get().toInt()
                    data[1] = buffer.get().toInt()
                    data[2] = buffer.get().toInt()
                }
            }
            122 -> definition.anInt854 = buffer.readUnsignedShort()
            123 -> definition.anInt852 = buffer.readUnsignedShort()
            125 -> definition.respawnDirection = buffer.get()
            127 -> definition.renderEmote = buffer.readUnsignedShort()
            128 -> definition.moveSpeed = buffer.uByte
            134 -> {
                definition.walkingAnimation = buffer.readUnsignedShort()
                if (definition.walkingAnimation == 65535) {
                    definition.walkingAnimation = -1
                }
                definition.rotate180Animation = buffer.readUnsignedShort()
                if (definition.rotate180Animation == 65535) {
                    definition.rotate180Animation = -1
                }
                definition.rotate90RightAnimation = buffer.readUnsignedShort()
                if (definition.rotate90RightAnimation == 65535) {
                    definition.rotate90RightAnimation = -1
                }
                definition.rotate90LeftAnimation = buffer.readUnsignedShort()
                if (definition.rotate90LeftAnimation == 65535) {
                    definition.rotate90LeftAnimation = -1
                }
                definition.specialByte = buffer.readUnsignedByte()
            }
            135 -> {
                definition.anInt803 = buffer.readUnsignedByte()
                definition.anInt861 = buffer.readUnsignedShort()
            }
            136 -> {
                definition.anInt823 = buffer.readUnsignedByte()
                definition.anInt857 = buffer.readUnsignedShort()
            }
            137 -> definition.anInt831 = buffer.readUnsignedShort()
            138 -> definition.anInt817 = buffer.readUnsignedShort()
            139 -> definition.anInt830 = buffer.readUnsignedShort()
            140 -> definition.anInt808 = buffer.readUnsignedByte()
            141 -> definition.aBoolean875 = true
            142 -> definition.anInt846 = buffer.readUnsignedShort()
            143 -> definition.aBoolean869 = true
            in 150..154 -> { buffer.readString().let { definition.options[opcode -150] = it } }
            155 -> {
                definition.aByte821 = buffer.get()
                definition.aByte824 = buffer.get()
                definition.aByte843 = buffer.get()
                definition.aByte855 = buffer.get()
            }
            158 -> definition.aByte833 = 1
            159 -> definition.aByte833 = 0
            160 -> {
                val size = buffer.readUnsignedByte()
                definition.quests = ShortArray(size)
                (0 until size).forEach {
                    definition.quests!![it] = buffer.readUnsignedShort().toShort()
                }
            }
            162 -> definition.aBoolean809 = true
            163 -> definition.anInt864 = buffer.readUnsignedByte()
            164 -> {
                definition.anInt848 = buffer.readUnsignedShort()
                definition.anInt837 = buffer.readUnsignedShort()
            }
            165 -> definition.anInt847 = buffer.readUnsignedByte()
            168 -> definition.anInt828 = buffer.readUnsignedByte()
            249 -> {
                val length = buffer.readUnsignedByte()
                (0 until length).forEach { _ ->
                    val string = buffer.readUnsignedByte() == 1
                    definition.params[buffer.readUnsignedMedium()] = if (string) buffer.readString() else buffer.int
                }
            }
            else -> logger.warn { "Unhandled npc definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }


}