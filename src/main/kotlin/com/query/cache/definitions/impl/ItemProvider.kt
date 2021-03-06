package com.query.cache.definitions.impl

import com.displee.cache.index.archive.Archive
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
    var params : MutableMap<Int,String> = mutableMapOf<Int, String>()
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
        val archive: Archive = library.index(IndexType.CONFIGS).archive(ConfigType.ITEM.id)!!
        val definitions = archive.fileIds().map {
            decode(ByteBuffer.wrap(archive.file(it)?.data), ItemDefinition(it))
        }
        return Serializable(DefinitionsTypes.ITEMS,this, definitions,writeTypes)
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
            else -> logger.warn { "Unhandled item definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }



}