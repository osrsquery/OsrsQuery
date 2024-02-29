package com.query.cache.definitions.impl

import com.displee.cache.index.archive.Archive
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
    var wearPos1: Int = 0,
    var wearPos2: Int = 0,
    var wearPos3: Int = 0,
    var weight: Int = 0,
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
    var options : MutableList<String?> = mutableListOf(null, null, "Take", null, null),
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
    var params : MutableMap<Int,String> = mutableMapOf()
): Definition() {


    @Throws(IOException::class)
    override fun encode(dos: DataOutputStream) {

        if (inventoryModel != 0) {
            dos.writeByte(1)
            dos.writeShort(inventoryModel)
        }

        if (!name.equals("null", ignoreCase = true)) {
            dos.writeByte(2)
            dos.writeString(name)
        }

        if (zoom2d != 2000) {
            dos.writeByte(4)
            dos.writeShort(zoom2d)
        }

        if (xan2d != 0) {
            dos.writeByte(5)
            dos.writeShort(xan2d)
        }

        if (yan2d != 0) {
            dos.writeByte(6)
            dos.writeShort(yan2d)
        }

        if (xOffset2d != 0) {
            dos.writeByte(7)
            dos.writeShort(xOffset2d)
        }

        if (yOffset2d != 0) {
            dos.writeByte(8)
            dos.writeShort(yOffset2d)
        }

        if (stackable == 1) {
            dos.writeByte(11)
        }

        if (cost != 1) {
            dos.writeByte(12)
            dos.writeInt(cost)
        }

        if (wearPos1 != 0) {
            dos.writeByte(13)
            dos.writeByte(wearPos1)
        }

        if (wearPos2 != 0) {
            dos.writeByte(14)
            dos.writeByte(wearPos2)
        }

        if (members) {
            dos.writeByte(16)
        }

        if (male_equip_main != -1 || male_equip_translate_y != 0) {
            dos.writeByte(23)
            dos.writeShort(male_equip_main)
            dos.writeByte(male_equip_translate_y)
        }

        if (male_equip_attachment != -1) {
            dos.writeByte(24)
            dos.writeShort(male_equip_attachment)
        }

        if (female_equip_main != -1 || female_equip_attachment != -1) {
            dos.writeByte(25)
            dos.writeShort(female_equip_main)
            dos.writeByte(female_equip_attachment)
        }

        if (equipped_model_female_2 != -1) {
            dos.writeByte(26)
            dos.writeShort(equipped_model_female_2)
        }

        if (wearPos3 != 0) {
            dos.writeByte(27)
            dos.writeByte(wearPos3)
        }

        if (options != mutableListOf(null, null, "Take", null, null)) {
            for (i in 0 until options.size) {
                if (options[i] == null) {
                    continue
                }
                dos.writeByte(i + 30)
                dos.writeString(options[i]!!)
            }
        }

        if (interfaceOptions != mutableListOf(null, null, null, null, "Drop")) {
            for (i in 0 until interfaceOptions.size) {
                if (interfaceOptions[i] == null) {
                    continue
                }
                dos.writeByte(i + 35)
                dos.writeString(interfaceOptions[i]!!)
            }
        }

        if (colorFind != null && colorReplace != null) {
            dos.writeByte(40)
            val len: Int = colorFind!!.size
            dos.writeByte(len)
            for (i in 0 until len) {
                dos.writeShort(colorFind!![i].toInt())
                dos.writeShort(colorReplace!![i].toInt())
            }
        }

        if (textureFind != null && textureReplace != null) {
            dos.writeByte(41)
            val len: Int = textureReplace!!.size
            dos.writeByte(len)
            for (i in 0 until len) {
                dos.writeShort(textureFind!!.get(i).toInt())
                dos.writeShort(textureReplace!!.get(i).toInt())
            }
        }

        if (shiftClickDropIndex != -2) {
            dos.writeByte(42)
            dos.writeByte(shiftClickDropIndex)
        }

        if (isTradeable) {
            dos.writeByte(65)
        }

        if (weight != 0) {
            dos.writeByte(75)
            dos.writeShort(weight)
        }

        if (male_equip_emblem != -1) {
            dos.writeByte(78)
            dos.writeShort(male_equip_emblem)
        }

        if (female_equip_emblem != -1) {
            dos.writeByte(79)
            dos.writeShort(female_equip_emblem)
        }

        if (male_dialogue_head != -1) {
            dos.writeByte(90)
            dos.writeShort(male_dialogue_head)
        }

        if (female_dialogue_head != -1) {
            dos.writeByte(91)
            dos.writeShort(female_dialogue_head)
        }

        if (equipped_model_male_dialogue_2 != -1) {
            dos.writeByte(92)
            dos.writeShort(equipped_model_male_dialogue_2)
        }

        if (equipped_model_female_dialogue_2 != -1) {
            dos.writeByte(93)
            dos.writeShort(equipped_model_female_dialogue_2)
        }

        if (category != -1) {
            dos.writeByte(94)
            dos.writeShort(category)
        }

        if (zan2d != 0) {
            dos.writeByte(95)
            dos.writeShort(zan2d)
        }

        if (notedID != -1) {
            dos.writeByte(97)
            dos.writeShort(notedID)
        }

        if (notedTemplate != -1) {
            dos.writeByte(98)
            dos.writeShort(notedTemplate)
        }

        if (countObj != null && countCo != null) {
            for (i in countObj!!.indices) {
                dos.writeByte(100 + i)
                dos.writeShort(countObj!![i])
                dos.writeShort(countCo!![i])
            }
        }

        if (resizeX != 128) {
            dos.writeByte(110)
            dos.writeShort(resizeX)
        }

        if (resizeY != 128) {
            dos.writeByte(111)
            dos.writeShort(resizeY)
        }

        if (resizeZ != 128) {
            dos.writeByte(112)
            dos.writeShort(resizeZ)
        }

        if (ambient != 0) {
            dos.writeByte(113)
            dos.writeByte(ambient)
        }

        if (contrast != 0) {
            dos.writeByte(114)
            dos.writeByte(contrast)
        }

        if (team != 0) {
            dos.writeByte(115)
            dos.writeByte(team)
        }

        if (boughtId != -1) {
            dos.writeByte(139)
            dos.writeShort(boughtId)
        }

        if (boughtTemplateId != -1) {
            dos.writeByte(140)
            dos.writeShort(boughtTemplateId)
        }

        if (placeholderId != -1) {
            dos.writeByte(148)
            dos.writeShort(placeholderId)
        }

        if (placeholderTemplateId != -1) {
            dos.writeByte(149)
            dos.writeShort(placeholderTemplateId)
        }

        if (params != mutableMapOf<Int, String>()) {
            dos.writeByte(249)
            dos.writeParams(params)
        }

        dos.writeByte(0)

    }

}

class ItemProvider(val latch: CountDownLatch?) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(ItemDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive: Archive = library.index(IndexType.CONFIGS).archive(ConfigType.ITEM.id)!!
        val definitions = archive.fileIds().map {
            decode(ByteBuffer.wrap(archive.file(it)?.data), ItemDefinition(it))
        }
        return Serializable(DefinitionsTypes.ITEMS,this, definitions)
    }

    fun decode(buffer: ByteBuffer, definition: ItemDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> definition.inventoryModel = buffer.uShort
            2 -> definition.name = buffer.rsString
            4 -> definition.zoom2d = buffer.uShort
            5 -> definition.xan2d = buffer.uShort
            6 -> definition.yan2d = buffer.uShort
            7 -> {
                definition.xOffset2d = buffer.uShort
                if (definition.xOffset2d > Short.MAX_VALUE) {
                    definition.xOffset2d -= 65536
                }
            }
            8 -> {
                definition.yOffset2d = buffer.uShort
                if (definition.yOffset2d > Short.MAX_VALUE) {
                    definition.yOffset2d -= 65536
                }
            }
            11 -> definition.stackable = 1
            12 -> definition.cost = buffer.int
            13 -> definition.wearPos1 = buffer.uByte
            14 -> definition.wearPos2 = buffer.uByte
            16 -> definition.members = true
            23 -> {
                definition.male_equip_main = buffer.uShort
                definition.male_equip_translate_y = buffer.uByte
            }
            24 -> definition.male_equip_attachment = buffer.uShort
            25 -> {
                definition.female_equip_main = buffer.uShort
                definition.female_equip_attachment = buffer.uByte
            }
            26 -> definition.equipped_model_female_2 = buffer.uShort
            27 -> definition.wearPos3 = buffer.uByte
            in 30..34 -> {
                definition.options[opcode - 30] = buffer.rsString
                if (definition.options[opcode - 30].equals("Hidden", true)) {
                    definition.options[opcode - 30] = null
                }
            }
            in 35..39 -> definition.interfaceOptions[opcode - 35] = buffer.rsString
            40 -> {
                val length: Int = buffer.uByte
                definition.colorFind = ShortArray(length)
                definition.colorReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.colorFind!![it] = (buffer.uShort).toShort()
                    definition.colorReplace!![it] = (buffer.uShort).toShort()
                }
            }
            41 -> {
                val length: Int = buffer.uByte
                definition.textureFind = ShortArray(length)
                definition.textureReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.textureFind!![it] = (buffer.uShort).toShort()
                    definition.textureReplace!![it] = (buffer.uShort).toShort()
                }
            }
            42 -> definition.shiftClickDropIndex = buffer.byte.toInt()
            65 -> definition.isTradeable = true
            75 -> definition.weight = buffer.uShort
            78 -> definition.male_equip_emblem = buffer.uShort
            79 -> definition.female_equip_emblem = buffer.uShort
            90 -> definition.male_dialogue_head = buffer.uShort
            91 -> definition.female_dialogue_head = buffer.uShort
            92 -> definition.equipped_model_male_dialogue_2 = buffer.uShort
            93 -> definition.equipped_model_female_dialogue_2 = buffer.uShort
            94 -> definition.category = buffer.uShort
            95 -> definition.zan2d = buffer.uShort
            97 -> definition.notedID = buffer.uShort
            98 -> definition.notedTemplate = buffer.uShort
            in 100..109 -> {
                if (definition.countObj == null) {
                    definition.countObj = IntArray(10)
                    definition.countCo = IntArray(10)
                }
                definition.countObj!![opcode - 100] = buffer.uShort
                definition.countCo!![opcode - 100] = buffer.uShort
            }
            110 -> definition.resizeX = buffer.uShort
            111 -> definition.resizeY = buffer.uShort
            112 -> definition.resizeZ = buffer.uShort
            113 -> definition.ambient = buffer.byte.toInt()
            114 -> definition.contrast = buffer.byte.toInt()
            115 -> definition.team = buffer.byte.toInt()
            139 -> definition.boughtId = buffer.uShort
            140 -> definition.boughtTemplateId = buffer.uShort
            148 -> definition.placeholderId = buffer.uShort
            149 -> definition.placeholderTemplateId = buffer.uShort
            249 -> buffer.readParams()
            0 -> break
            else -> logger.warn { "Unhandled item definition opcode with id: ${opcode}." }
        } while (true)
        post(definition)
        return definition
    }

    private fun post(definition : ItemDefinition) {
        if (definition.stackable == 1) {
            definition.weight = 0
        }
    }



}