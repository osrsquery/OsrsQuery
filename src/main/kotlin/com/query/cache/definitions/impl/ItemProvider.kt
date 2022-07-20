package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants.store
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import com.runetopic.cache.extension.toByteBuffer
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class ItemDefinition(
    override val id: Int = 0,
    var name: String = "null",
    var resizeX: Int = 128,
    var resizeY: Int = 128,
    var resizeZ: Int = 128,
    var xan2d: Int = 0,
    var category : Int = -1,
    var yan2d: Int = 0,
    var zan2d: Int = 0,
    var cost: Int = 1,
    var isTradeable: Boolean = false,
    var stackable: Int = 0,
    var inventoryModel: Int = 0,
    var members: Boolean = false,
    var colorFind: ShortArray? = null,
    var colorReplace: ShortArray? = null,
    var textureFind: ShortArray? = null,
    var textureReplace: ShortArray? = null,
    var zoom2d: Int = 2000,
    var xOffset2d: Int = 0,
    var yOffset2d: Int = 0,
    var ambient: Int = 0,
    var contrast: Int = 0,
    var countCo: IntArray? = null,
    var countObj: IntArray? = null,
    var options : MutableList<String?> = mutableListOf(null, null, "Take", null, "Drop"),
    var interfaceOptions  : MutableList<String?> = mutableListOf(null, null, null, null, "Drop"),
    var male_equip_main: Int = -1,
    var male_equip_attachment: Int = -1,
    var male_equip_emblem: Int = -1,
    var male_equip_translate_y: Int = 0,
    var male_dialogue_head: Int = -1,
    var equipped_model_male_dialogue_2: Int = -1,
    var female_equip_main: Int = -1,
    var equipped_model_female_2: Int = -1,
    var female_equip_emblem: Int = -1,
    var female_equip_attachment: Int = -1,
    var female_dialogue_head: Int = -1,
    var equipped_model_female_dialogue_2: Int = -1,
    var notedID: Int = -1,
    var notedTemplate: Int = -1,
    var team: Int = 0,
    var shiftClickDropIndex: Int = -2,
    var boughtId: Int = -1,
    var boughtTemplateId: Int = -1,
    var placeholderId: Int = -1,
    var placeholderTemplateId: Int = -1,
    var params: MutableMap<Int, Any> = mutableMapOf(),
    var aByteArray1858: ByteArray? = null,
    var tooltipColor : Int = -1,
    var hasTooltipColor : Boolean = false,
    var wearPos3 : Int = -1,
    var multiStackSize : Int = -1,
    var wearPos2 : Int = -1,
    var wearPos : Int = -1,
    var notedId: Int = -1,
    var lendId: Int = -1,
    var lendTemplateId: Int = -1,
    var maleWearXOffset: Int = 0,
    var maleWearYOffset: Int = 0,
    var maleWearZOffset: Int = 0,
    var femaleWearXOffset: Int = 0,
    var femaleWearYOffset: Int = 0,
    var femaleWearZOffset: Int = 0,
    var anInt1899: Int = -1,
    var anInt1897: Int = -1,
    var anInt1850: Int = -1,
    var anInt1863: Int = -1,
    var customCursorOp1: Int = -1,
    var customCursorId1: Int = -1,
    var customCursorOp2: Int = -1,
    var customCursorId2: Int = -1,
    var quests: IntArray? = null,
    var pickSizeShift: Int = 0,
    var bindId: Int = -1,
    var bindTemplateId: Int = -1,
): Definition

class ItemProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(ItemDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val definitions : MutableList<Definition> = emptyList<Definition>().toMutableList()

        store!!.index(IndexType.ITEMS.number).use { index ->
            (0 until index.expand()).forEach {
                definitions.add(decode(index.group(it ushr 8).file(it and 0xFF).data.toByteBuffer(), ItemDefinition(it)))
            }
        }
        return Serializable(DefinitionsTypes.ITEMS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: ItemDefinition): Definition {
        do when (val opcode: Int = buffer.readUnsignedByte()) {
            0 -> break
            1 -> definition.inventoryModel = buffer.readBigSmart()
            2 -> definition.name = buffer.readString()
            4 -> definition.zoom2d = buffer.uShort
            5 -> definition.xan2d = buffer.uShort
            6 -> definition.yan2d = buffer.uShort
            7 -> buffer.uShort.let {
                definition.xOffset2d = if (it > Short.MAX_VALUE) it - 65536 else it
            }
            8 -> buffer.uShort.let {
                definition.yOffset2d = if (it > Short.MAX_VALUE) it - 65536 else it
            }
            11 -> definition.stackable = 1
            12 -> definition.cost = buffer.int
            13 -> definition.wearPos = buffer.uByte
            14 -> definition.wearPos2 = buffer.uByte
            16 -> definition.members = true
            18 -> definition.multiStackSize = buffer.uShort
            23 -> definition.male_equip_main = buffer.readBigSmart()
            24 -> definition.male_equip_attachment = buffer.readBigSmart()
            25 -> definition.female_equip_main = buffer.readBigSmart()
            26 -> definition.equipped_model_female_2 = buffer.readBigSmart()
            27 -> definition.wearPos3 = buffer.uByte
            in 30..34 -> buffer.readString().let { definition.options[opcode - 30] = it }
            in 35..39 -> buffer.readString().let { definition.interfaceOptions[opcode - 35] = it }
            40 -> {
                val size = buffer.readUnsignedByte()
                val colorToFind = ShortArray(size)
                val colorToReplace = ShortArray(size)
                (0 until size).forEach {
                    colorToFind[it] = buffer.uShort.toShort()
                    colorToReplace[it] = buffer.uShort.toShort()
                }
                definition.colorFind = colorToFind
                definition.colorReplace = colorToReplace
            }
            41 -> {
                val size = buffer.readUnsignedByte()
                val textureToFind = ShortArray(size)
                val textureToReplace = ShortArray(size)
                (0 until size).forEach {
                    textureToFind[it] = buffer.uShort.toShort()
                    textureToReplace[it] = buffer.uShort.toShort()
                }
                definition.textureFind = textureToFind
                definition.textureReplace = textureToReplace
            }
            42 -> {
                val size = buffer.readUnsignedByte()
                val aByteArray1858 = ByteArray(size)
                (0 until size).forEach { aByteArray1858[it] = buffer.byte }
                definition.aByteArray1858 = aByteArray1858
            }
            43 -> {
                definition.tooltipColor = buffer.int
                definition.hasTooltipColor = true
            }
            65 -> definition.isTradeable = true
            78 -> definition.male_equip_emblem = buffer.readBigSmart()
            79 -> definition.female_equip_emblem = buffer.readBigSmart()
            90 -> definition.male_dialogue_head = buffer.readBigSmart()
            91 -> definition.female_dialogue_head = buffer.readBigSmart()
            92 -> definition.equipped_model_male_dialogue_2 = buffer.readBigSmart()
            93 -> definition.equipped_model_female_dialogue_2 = buffer.readBigSmart()
            95 -> definition.zan2d = buffer.uShort
            96 -> definition.category = buffer.readUnsignedByte()
            97 -> definition.notedId = buffer.uShort
            98 -> definition.notedTemplate = buffer.uShort
            in 100..109 -> {
                val countObj = IntArray(10)
                val countCo = IntArray(10)
                countObj[opcode - 100] = buffer.uShort
                countCo[opcode - 100] = buffer.uShort
                definition.countObj = countObj
                definition.countCo = countCo
            }
            110 -> definition.resizeX = buffer.uShort
            111 -> definition.resizeY = buffer.uShort
            112 -> definition.resizeZ = buffer.uShort
            113 -> definition.ambient = buffer.byte.toInt()
            114 -> definition.contrast = buffer.byte.toInt()
            115 -> definition.team = buffer.readUnsignedByte()
            121 -> definition.lendId = buffer.uShort
            122 -> definition.lendTemplateId = buffer.uShort
            125 -> {
                definition.maleWearXOffset = buffer.get().toInt() shl 2
                definition.maleWearYOffset = buffer.get().toInt() shl 2
                definition.maleWearZOffset = buffer.get().toInt() shl 2
            }
            126 -> {
                definition.femaleWearXOffset = buffer.get().toInt() shl 2
                definition.femaleWearYOffset = buffer.get().toInt() shl 2
                definition.femaleWearZOffset = buffer.get().toInt() shl 2
            }
            127 -> {
                definition.anInt1899 = buffer.readUnsignedByte()
                definition.anInt1897 = buffer.uShort
            }
            128 -> {
                definition.anInt1850 = buffer.readUnsignedByte()
                definition.anInt1863 = buffer.uShort
            }
            129 -> {
                definition.customCursorOp1 = buffer.readUnsignedByte()
                definition.customCursorId1 = buffer.uShort
            }
            130 -> {
                definition.customCursorOp2 = buffer.readUnsignedByte()
                definition.customCursorId2 = buffer.uShort
            }
            132 -> {
                val size = buffer.readUnsignedByte()
                val anIntArray1893 = IntArray(size)
                (0 until size).forEach { anIntArray1893[it] = buffer.uShort }
                definition.quests = anIntArray1893
            }
            134 -> definition.pickSizeShift = buffer.readUnsignedByte()
            139 -> definition.bindId = buffer.uShort
            140 -> definition.bindTemplateId = buffer.uShort
            249 -> {
                val size = buffer.readUnsignedByte()
                (0 until size).forEach { _ ->
                    val string = buffer.readUnsignedByte() == 1
                    definition.params[buffer.uMedium] = if (string) buffer.readString() else buffer.int
                }
            }
            else -> logger.warn { "Unhandled item definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }



}