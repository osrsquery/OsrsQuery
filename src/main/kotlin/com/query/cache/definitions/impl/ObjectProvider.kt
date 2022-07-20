package com.query.cache.definitions.impl

import com.query.Application
import com.query.Application.logger
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.Constants.store
import com.query.utils.*
import com.runetopic.cache.extension.toByteBuffer
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class ObjectDefinition(
    override val id: Int = 0,
    var objectTypes: ByteArray? = null,
    var objectModels: Array<IntArray>? = null,
    var name: String = "null",
    var sizeX: Int = -1,
    var sizeZ: Int = -1,
    var interactionType: Int = 2,
    var blocksProjectile: Boolean = true,
    var wallOrDoor: Int = -1,
    var contouredGround: Byte = 0,
    var mergeNormals: Boolean = false,
    var occludes: Int = -1,
    var animationId: Int = -1,
    var decorDisplacement: Int = 64,
    var ambient: Int = 0,
    var contrast: Int = 0,
    var actions: Array<String> = Array(5) { "" },
    var recolorToFind: ShortArray? = null,
    var recolorToReplace: ShortArray? = null,
    var texturesToFind: ShortArray? = null,
    var texturesToReplace: ShortArray? = null,
    var aByteArray1118: ByteArray? = null,
    var isRotated: Boolean = false,
    var shadowed: Boolean = false,
    var modelSizeX: Int = 128,
    var modelSizeHeight: Int = 128,
    var modelSizeY: Int = 128,
    var blockingMask: Int = 0,
    var offsetX: Int = 0,
    var offsetHeight: Int = 0,
    var offsetZ: Int = 0,
    var obstructsGround: Boolean = false,
    var isHollow: Boolean = false,
    var support: Int = -1,
    var varbitId: Int = -1,
    var varpId: Int = -1,
    var ambientSoundId: Int = -1,
    var ambientSoundHearDistance: Int = 0,
    var anInt1145: Int = 0,
    var anInt1139: Int = 0,
    var hidden: Boolean = false,
    var aBoolean1151: Boolean = false,
    var randomizeAnimStart: Boolean = false,
    var members: Boolean = false,
    var adjustMapSceneRotation: Boolean = false,
    var hasAnimation: Boolean = false,
    var configChangeDest: IntArray? = null,
    var audioTracks: IntArray? = null,
    var anInt1142: Int = -1,
    var anInt1184: Int = -1,
    var anInt1173: Int = -1,
    var anInt1183: Int = -1,
    var anInt1121: Int = -1,
    var mapSpriteRotation: Int = 0,
    var mapSpriteId: Int = -1,
    var ambientSoundVolume: Int = 255,
    var mapAreaId: Int = -1,
    var flipMapSprite: Boolean = false,
    var animations: IntArray? = null,
    var animations1: IntArray? = null,
    var anIntArray1153: IntArray? = null,
    var aByte1123: Byte = 0,
    var aByte1110: Byte = 0,
    var aByte1169: Byte = 0,
    var aByte1109: Byte = 0,
    var anInt1112: Int = 0,
    var anInt2650: Int = 0,
    var anInt1115: Int = 0,
    var anInt1125: Int = 0,
    var anInt1107: Int = 0,
    var aBoolean1163: Boolean = false,
    var aBoolean1175: Boolean = false,
    var anInt1156: Int = 960,
    var anInt1111: Int = 0,
    var anInt1128: Int = 256,
    var anInt1159: Int = 256,
    var aBoolean1167: Boolean = false,
    var anInt1113: Int = 0,
    var params: MutableMap<Int, Any> = mutableMapOf()
): Definition

class ObjectProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(ObjectDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val definitions : MutableList<Definition> = emptyList<Definition>().toMutableList()
        store!!.index(IndexType.OBJECTS.number).use { index ->
            (0 until index.expand()).forEach {
                definitions.add(decode(index.group(it ushr 8).file(it and 0xFF).data.toByteBuffer(), ObjectDefinition(it)))
            }
        }
        return Serializable(DefinitionsTypes.OBJECTS,this, definitions,writeTypes)
    }

    private fun decode(buffer: ByteBuffer, definition: ObjectDefinition): Definition {
        do when (val opcode: Int = buffer.uByte) {
            0 -> break
            1 -> {
                val size = buffer.readUnsignedByte()
                val types = ByteArray(size)
                val models = Array(size) { intArrayOf() }

                (0 until size).forEach {
                    types[it] = buffer.get()
                    val count = buffer.readUnsignedByte()
                    models[it] = IntArray(count)
                    (0 until count).forEach { it2 ->
                        models[it][it2] = buffer.readBigSmart()
                    }
                }

                definition.objectModels = models
                definition.objectTypes = types
            }
            118 -> {}
            116 -> {}
            114 -> {}
            110 -> {}
            136 -> {}
            179 -> {}
            2 -> definition.name = buffer.readString()
            14 -> definition.sizeX = buffer.readUnsignedByte()
            15 -> definition.sizeZ = buffer.readUnsignedByte()
            17 -> {
                definition.interactionType = 0
                definition.blocksProjectile = false
            }
            18 -> definition.blocksProjectile = false
            19 -> definition.wallOrDoor = buffer.readUnsignedByte()
            21 -> definition.contouredGround = 1
            22 -> definition.mergeNormals = true
            23 -> definition.occludes = 1
            24 -> buffer.readBigSmart().let {
                definition.animationId = if (it == 65535) -1 else it
            }
            27 -> definition.interactionType = 1
            28 -> definition.decorDisplacement = (buffer.readUnsignedByte() shl 2)
            29 -> definition.ambient = buffer.get().toInt()
            39 -> definition.contrast = buffer.get() * 25
            in 30..34 -> buffer.readString().let { definition.actions[opcode - 30] = it }
            40 -> {
                val size = buffer.readUnsignedByte()
                val colorsToFind = ShortArray(size)
                val colorsToReplace = ShortArray(size)
                (0 until size).forEach {
                    colorsToFind[it] = buffer.readUnsignedShort().toShort()
                    colorsToReplace[it] = buffer.readUnsignedShort().toShort()
                }
                definition.recolorToFind = colorsToFind
                definition.recolorToReplace = colorsToReplace
            }
            41 -> {
                val size = buffer.readUnsignedByte()
                val texturesToFind = ShortArray(size)
                val texturesToReplace = ShortArray(size)
                (0 until size).forEach {
                    texturesToFind[it] = buffer.readUnsignedShort().toShort()
                    texturesToReplace[it] = buffer.readUnsignedShort().toShort()
                }

                definition.texturesToFind = texturesToFind
                definition.texturesToReplace = texturesToReplace
            }
            42 -> {
                val length = buffer.readUnsignedByte()
                val aByteArray1118 = ByteArray(length)

                (0 until length).forEach {
                    aByteArray1118[it] = buffer.get()
                }
                definition.aByteArray1118 = aByteArray1118
            }
            62 -> definition.isRotated = true
            64 -> definition.shadowed = false
            65 -> definition.modelSizeX = buffer.readUnsignedShort()
            66 -> definition.modelSizeHeight = buffer.readUnsignedShort()
            67 -> definition.modelSizeY = buffer.readUnsignedShort()
            69 -> definition.blockingMask = buffer.readUnsignedByte()
            70 -> definition.offsetX = (buffer.readUnsignedShort() shl 2)
            71 -> definition.offsetHeight = (buffer.readUnsignedShort() shl 2)
            72 -> definition.offsetZ = (buffer.readUnsignedShort() shl 2)
            73 -> definition.obstructsGround = true
            74 -> definition.isHollow = true
            75 -> definition.support = buffer.readUnsignedByte()
            77, 92 -> {
                buffer.readUnsignedShort().let {
                    definition.varbitId = if (it == 65535) -1 else it
                }

                buffer.readUnsignedShort().let {
                    definition.varpId = if (it == 65535) -1 else it
                }

                var value = -1

                if (opcode == 92) {
                    buffer.readBigSmart().let {
                        value = if (it == 65535) -1 else it
                    }
                }

                val size = buffer.readUnsignedByte()
                val configChangeDest = IntArray(size + 2)

                (0..size).forEach { index ->
                    buffer.readUnsignedShort().let {
                        if (it == 65535) configChangeDest[index] = -1
                        else configChangeDest[index] = it
                    }
                }

                configChangeDest[size + 1] = value
                definition.configChangeDest = configChangeDest
            }
            78 -> {
                definition.ambientSoundId = buffer.readUnsignedShort()
                definition.ambientSoundHearDistance = buffer.readUnsignedByte()
            }
            79 -> {
                definition.anInt1145 = buffer.readUnsignedShort()
                definition.anInt1139 = buffer.readUnsignedShort()
                definition.ambientSoundHearDistance = buffer.readUnsignedByte()
                val length = buffer.readUnsignedByte()
                val anIntArray1127 = IntArray(length)

                (0 until length).forEach { index ->
                    anIntArray1127[index] = buffer.readUnsignedShort()
                }

                definition.audioTracks = anIntArray1127
            }
            81 -> {
                definition.contouredGround = 2.toByte()
                definition.anInt1142 = buffer.readUnsignedByte() * 256
            }
            82 -> definition.hidden = true
            88 -> definition.aBoolean1151 = true
            89 -> definition.randomizeAnimStart = true
            91 -> definition.members = true
            93 -> {
                definition.contouredGround = 5.toByte()
                definition.anInt1142 = buffer.readUnsignedShort()
            }
            94 -> {
                definition.contouredGround = 4.toByte()
            }
            95 -> {
                definition.contouredGround = 5.toByte()
                definition.anInt1142 = buffer.short.toInt()
            }
            97 -> definition.adjustMapSceneRotation = true
            98 -> definition.hasAnimation = true
            99 -> {
                definition.anInt1184 = buffer.readUnsignedByte()
                definition.anInt1173 = buffer.readUnsignedShort()
            }
            100 -> {
                definition.anInt1183 = buffer.readUnsignedByte()
                definition.anInt1121 = buffer.readUnsignedShort()
            }
            101 -> definition.mapSpriteRotation = buffer.readUnsignedByte()
            102 -> definition.mapSpriteId = buffer.readUnsignedShort()
            103 -> definition.occludes = 0
            104 -> definition.ambientSoundVolume = buffer.readUnsignedByte()
            105 -> definition.flipMapSprite = true
            106 -> {
                val length = buffer.readUnsignedByte()
                val anIntArray1170 = IntArray(length)
                val anIntArray1154 = IntArray(length)
                (0 until length).forEach { index ->
                    anIntArray1170[index] = buffer.readBigSmart()
                    val size = buffer.readUnsignedByte()
                    anIntArray1154[index] = size
                }

                definition.animations = anIntArray1170
                definition.animations1 = anIntArray1154
            }
            107 -> definition.mapAreaId = buffer.readUnsignedShort()
            in 150..154 -> buffer.readString().let { definition.actions[opcode -150] = it }
            160 -> {
                val length = buffer.readUnsignedByte()
                val anIntArray1153 = IntArray(length)
                (0 until length).forEach { index ->
                    anIntArray1153[index] = buffer.readUnsignedShort()
                }
                definition.anIntArray1153 = anIntArray1153
            }
            162 -> {
                definition.contouredGround = 3.toByte()
                definition.anInt1142 = buffer.int
            }
            163 -> {
                definition.aByte1123 = buffer.get()
                definition.aByte1110 = buffer.get()
                definition.aByte1169 = buffer.get()
                definition.aByte1109 = buffer.get()
            }
            164 -> definition.anInt1112 = buffer.short.toInt()
            165 -> definition.anInt1115 = buffer.short.toInt()
            166 -> definition.anInt1125 = buffer.short.toInt()
            167 -> definition.anInt1107 = buffer.readUnsignedShort()
            168 -> definition.aBoolean1163 = true
            169 -> definition.aBoolean1175 = true
            170 -> definition.anInt1156 = buffer.readUnsignedSmart()
            171 -> definition.anInt1111 = buffer.readUnsignedSmart()
            173 -> {
                definition.anInt1128 = buffer.readUnsignedShort()
                definition.anInt1159 = buffer.readUnsignedShort()
            }
            177 -> definition.aBoolean1167 = true
            178 -> definition.anInt1113 = buffer.readUnsignedByte()
            189 -> {}
            249 -> {
                val length = buffer.readUnsignedByte()
                (0 until length).forEach { _ ->
                    val string = buffer.readUnsignedByte() == 1
                    definition.params[buffer.readUnsignedMedium()] = if (string) buffer.readString() else buffer.int
                }
            }
            else -> logger.warn { "Unhandled object definition opcode with id: ${opcode}." }
        }
        while (true)
        return definition
    }


}