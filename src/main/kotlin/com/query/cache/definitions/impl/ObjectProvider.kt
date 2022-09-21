package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.*
import java.io.DataOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.stream.IntStream


data class ObjectDefinition(
    override val id: Int = 0,
    var retextureToFind: ShortArray? = null,
    var decorDisplacement : Int = 16,
    var isHollow : Boolean = false,
    var name: String  = "null",
    var objectModels: IntArray? = null,
    var objectTypes: IntArray? = null,
    var recolorToFind: ShortArray? = null,
    var mapAreaId: Int = -1,
    var retextureToReplace: ShortArray? = null,
    var sizeX: Int = 1,
    var sizeY: Int = 1,
    var anInt2083: Int = 0,
    var ambientSoundIds: IntArray? = null,
    var offsetX: Int = 0,
    var nonFlatShading: Boolean = false,
    var wallOrDoor: Int = -1,
    var animationId: Int = -1,
    var varbitId: Int = -1,
    var ambient: Int = 0,
    var contrast: Int = 0,
    var actions : MutableList<String?> = mutableListOf(null, null, null, null, null),
    var clipType: Int = 2,
    var mapSceneID: Int = -1,
    var blockingMask: Int = 0,
    var recolorToReplace: ShortArray? = null,
    var clipped: Boolean = true,
    var modelSizeX: Int = 128,
    var modelSizeZ: Int = 128,
    var modelSizeY: Int = 128,
    var offsetZ: Int = 0,
    var offsetY: Int = 0,
    var obstructsGround: Boolean = false,
    var randomizeAnimStart: Boolean = false,
    var contouredGround: Int = -1,
    var category : Int = -1,
    var supportsItems: Int = -1,
    var configs: IntArray? = null,
    var isRotated: Boolean = false,
    var varpId: Int = -1,
    var ambientSoundId: Int = -1,
    var modelClipped: Boolean = false,
    var anInt2112: Int = 0,
    var anInt2113: Int = 0,
    var blocksProjectile: Boolean = true,
    var params : MutableMap<Int,String> = mutableMapOf()
): Definition {

    @Throws(IOException::class)
    fun encode(dos: DataOutputStream) {

        if (objectModels != null) {
            if (objectTypes != null) {
                dos.writeByte(1)
                dos.writeByte(objectModels!!.size)
                if (objectModels!!.size > 0) {
                    for (i in objectModels!!.indices) {
                        dos.writeShort(objectModels!![i])
                        dos.writeByte(objectTypes!![i])
                    }
                }
            } else {
                dos.writeByte(5)
                dos.writeByte(objectModels!!.size)
                if (objectModels!!.size > 0) {
                    for (i in 0 until objectModels!!.size) {
                        dos.writeShort(objectModels!![i])
                    }
                }
            }
        }

        if (name != "null") {
            dos.writeByte(2)
            dos.writeString(name)
        }

        if (sizeX != 1) {
            dos.writeByte(14)
            dos.writeByte(sizeX)
        }

        if (sizeY != 1) {
            dos.writeByte(15)
            dos.writeByte(sizeY)
        }

        if (clipType == 0) {
            dos.writeByte(17)
            dos.writeByte(0)
        }

        if (!blocksProjectile) {
            dos.writeByte(18)
        }

        if (wallOrDoor != -1) {
            dos.writeByte(19)
            dos.writeByte(wallOrDoor)
        }

        if (contouredGround == 0) {
            dos.writeByte(21)
        }

        if (nonFlatShading) {
            dos.writeByte(22)
        }

        if (modelClipped) {
            dos.writeByte(23)
        }

        if (animationId != -1) {
            dos.writeByte(24)
            dos.writeShort(animationId)
        }

        if (clipType == 1) {
            dos.writeByte(27)
            dos.writeByte(1)
        }

        if (decorDisplacement != 16) {
            dos.writeByte(28)
            dos.writeByte(decorDisplacement)
        }

        if (ambient != 0) {
            dos.writeByte(29)
            dos.writeByte(ambient)
        }

        if (contrast != 0) {
            dos.writeByte(39)
            dos.writeByte(contrast / 25)
        }

        if (actions.any { it != null }) {
            for (i in 0 until actions.size) {
                if (actions[i] == null) {
                    continue
                }
                dos.write(30 + i)
                dos.writeString(actions[i]!!)
            }
        }

        if (recolorToFind != null && recolorToReplace != null) {
            dos.writeByte(40)
            dos.writeByte(recolorToFind!!.size)
            for (i in 0 until recolorToFind!!.size) {
                dos.writeShort(recolorToFind!![i].toInt())
                dos.writeShort(recolorToReplace!![i].toInt())
            }
        }

        if (retextureToFind != null && retextureToReplace != null) {
            dos.writeByte(41)
            dos.writeByte(retextureToFind!!.size)
            for (i in 0 until retextureToFind!!.size) {
                dos.writeShort(retextureToFind!!.get(i).toInt())
                dos.writeShort(retextureToReplace!!.get(i).toInt())
            }
        }

        if (category != -1) {
            dos.writeByte(61)
            dos.writeShort(category)
        }

        if (isRotated) {
            dos.writeByte(62)
        }

        if (!clipped) {
            dos.writeByte(64)
        }

        if (modelSizeX != 128) {
            dos.writeByte(65)
            dos.writeShort(modelSizeX)
        }

        if (modelSizeZ != 128) {
            dos.writeByte(66)
            dos.writeShort(modelSizeZ)
        }

        if (modelSizeY != 128) {
            dos.writeByte(67)
            dos.writeShort(modelSizeY)
        }

        if (mapSceneID != -1) {
            dos.writeByte(68)
            dos.writeShort(mapSceneID)
        }

        if (blockingMask != 0) {
            dos.writeByte(69)
            dos.writeByte(blockingMask)
        }

        if (offsetX != 0) {
            dos.writeByte(70)
            dos.writeShort(offsetX)
        }

        if (offsetZ != 0) {
            dos.writeByte(71)
            dos.writeShort(offsetZ)
        }

        if (offsetY != 0) {
            dos.writeByte(72)
            dos.writeShort(offsetY)
        }

        if (obstructsGround) {
            dos.writeByte(73)
        }

        if (isHollow) {
            dos.writeByte(74)
        }

        if (supportsItems != -1) {
            dos.writeByte(75)
            dos.writeByte(supportsItems)
        }

        if (ambientSoundId != -1 || anInt2083 != 0) {
            dos.writeByte(78)
            dos.writeShort(ambientSoundId)
            dos.writeByte(anInt2083)
        }

        if (anInt2112 != 0 || anInt2113 != 0 || anInt2083 != 0 && ambientSoundIds != null) {
            dos.writeByte(79)
            dos.writeShort(anInt2112)
            dos.writeShort(anInt2113)
            dos.writeByte(anInt2083)
            dos.writeByte(ambientSoundIds!!.size)
            for (i in ambientSoundIds!!.indices) {
                dos.writeShort(ambientSoundIds!![i])
            }
        }

        if (contouredGround != -1) {
            dos.writeByte(81)
            dos.writeByte(contouredGround)
        }

        if (mapAreaId != -1) {
            dos.writeByte(82)
            dos.writeShort(mapAreaId)
        }

        if (!randomizeAnimStart) {
            dos.writeByte(89)
        }

        if ((varbitId != -1 || varpId != -1) && configs != null && configs!!.isNotEmpty()) {
            val value: Int = configs!![configs!!.size - 1]
            dos.writeByte(if (value != -1) 92 else 77)
            dos.writeShort(varbitId)
            dos.writeShort(varpId)
            if (value != -1) {
                dos.writeShort(configs!![configs!!.size - 1])
            }
            dos.writeByte(configs!!.size - 2)
            for (i in 0..configs!!.size - 2) {
                dos.writeShort(configs!![i])
            }
        }

        if (params != mutableMapOf<Int, String>()) {
            dos.writeByte(249)
            dos.writeParams(params)
        }

        dos.writeByte(0)

    }

}

class ObjectProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(ObjectDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val archive = library.index(IndexType.CONFIGS).archive(ConfigType.OBJECT.id)!!
        val definitions = archive.fileIds().map {
           decode(ByteBuffer.wrap(archive.file(it)?.data), ObjectDefinition(it))
        }
        return Serializable(DefinitionsTypes.OBJECTS,this, definitions,writeTypes)
    }

    private fun decode(buffer: ByteBuffer, definition: ObjectDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            1 -> {
                val length: Int = buffer.uByte
                when {
                    length > 0 -> {
                        definition.objectTypes = IntArray(length)
                        definition.objectModels = IntArray(length)
                        (0 until length).forEach {
                            definition.objectModels!![it] = buffer.uShort
                            definition.objectTypes!![it] = buffer.uByte
                        }
                    }
                }
            }
            2 -> definition.name = buffer.rsString
            5 -> {
                val length: Int = buffer.uByte
                when {
                    length > 0 -> {
                        definition.objectTypes = null
                        definition.objectModels = IntStream.range(0, length).map {
                            buffer.uShort
                        }.toArray()
                    }
                }
            }
            14 -> definition.sizeX = buffer.uByte
            15 -> definition.sizeY = buffer.uByte
            17 -> {
                definition.clipType = 0
                definition.blocksProjectile = false
            }
            18 -> definition.blocksProjectile = false
            19 -> definition.wallOrDoor = buffer.uByte
            21 -> definition.contouredGround = 0
            22 -> definition.nonFlatShading = true
            23 -> definition.modelClipped = true
            24 -> {
                definition.animationId = buffer.uShort
                if (definition.animationId == 65535) {
                    definition.animationId = -1
                }
            }
            27 -> definition.clipType = 1
            28 -> definition.decorDisplacement = buffer.uByte
            29 -> definition.ambient = buffer.byte.toInt()
            39 -> definition.contrast = buffer.byte.toInt() * 25
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
                    definition.recolorToFind!![it] = buffer.short
                    definition.recolorToReplace!![it] = buffer.short
                }
            }
            41 -> {
                val length: Int = buffer.uByte
                definition.retextureToFind = ShortArray(length)
                definition.retextureToReplace = ShortArray(length)
                (0 until length).forEach {
                    definition.retextureToFind!![it] = buffer.short
                    definition.retextureToReplace!![it] = buffer.short
                }
            }
            61 -> definition.category = buffer.uShort
            62 -> definition.isRotated = true
            64 -> definition.clipped = false
            65 -> definition.modelSizeX = buffer.uShort
            66 -> definition.modelSizeZ = buffer.uShort
            67 -> definition.modelSizeY = buffer.uShort
            68 -> definition.mapSceneID = buffer.uShort
            69 -> definition.blockingMask = buffer.byte.toInt()
            70 -> definition.offsetX = buffer.uShort
            71 -> definition.offsetZ = buffer.uShort
            72 -> definition.offsetY = buffer.uShort
            73 -> definition.obstructsGround = true
            74 -> definition.isHollow = true
            75 -> definition.supportsItems = buffer.uByte
            77 -> {
                var varbitId: Int = buffer.uShort
                if (varbitId == 65535) {
                    varbitId = -1
                }
                definition.varbitId = varbitId
                var varpId: Int = buffer.uShort
                if (varpId == 65535) {
                    varpId = -1
                }
                definition.varpId = varpId
                val length: Int = buffer.uByte
                val configChangeDest = IntArray(length + 2)
                IntStream.rangeClosed(0, length).forEach {
                    configChangeDest[it] = buffer.uShort
                    when {
                        configChangeDest[it] == 65535 -> {
                            configChangeDest[it] = -1
                        }
                    }
                }
                configChangeDest[length + 1] = -1
                definition.configs = configChangeDest
            }
            78 -> {
                definition.ambientSoundId = buffer.uShort
                definition.anInt2083 = buffer.uByte
            }
            79 -> {
                definition.anInt2112 = buffer.uShort
                definition.anInt2113 = buffer.uShort
                definition.anInt2083 = buffer.uByte
                val length: Int = buffer.uByte
                definition.ambientSoundIds = IntStream.range(0, length).map {
                    buffer.uShort
                }.toArray()
            }
            81 -> definition.contouredGround = (buffer.uByte) * 256
            60,82 -> definition.mapAreaId = buffer.uShort
            89 -> definition.randomizeAnimStart = true
            92 -> {
                var varbitId: Int = buffer.uShort
                if (varbitId == 65535) {
                    varbitId = -1
                }
                definition.varbitId = varbitId
                var varpId: Int = buffer.uShort
                if (varpId == 65535) {
                    varpId = -1
                }
                definition.varpId = varpId
                var value: Int = buffer.uShort
                if (value == 65535) {
                    value = -1
                }
                val length: Int = buffer.uByte
                val configChangeDest = IntArray(length + 2)
                IntStream.rangeClosed(0, length).forEach {
                    configChangeDest[it] = buffer.uShort
                    when {
                        configChangeDest[it] == 65535 -> {
                            configChangeDest[it] = -1
                        }
                    }
                }
                configChangeDest[length + 1] = value
                definition.configs = configChangeDest
            }
            249 -> buffer.readParams()
            0 -> break
            else -> logger.warn { "Unhandled object definition opcode with id: ${opcode}." }
        }
        while (true)
        post(definition)
        return definition
    }

    private fun post(definition: ObjectDefinition) {
        if (definition.wallOrDoor == -1) {
            definition.wallOrDoor = 0
            if (definition.objectModels != null && (definition.objectTypes == null || definition.objectTypes!![0] == 10)) {
                definition.wallOrDoor = 1
            }
            for (it in 0..4) {
                if (definition.actions[it] != null) {
                    definition.wallOrDoor = 1
                    break
                }
            }
        }
        if (definition.supportsItems == -1) {
            definition.supportsItems = if (definition.clipType != 0) 1 else 0
        }
    }

}