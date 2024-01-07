package com.query.cache.definition.decoder

import com.query.buffer.read.BufferReader
import com.query.buffer.read.Reader
import com.query.cache.Cache
import com.query.cache.DefinitionDecoder
import com.query.cache.Index.WORLD_MAP
import com.query.cache.definition.data.WorldMapDefinition
import com.query.cache.definition.data.WorldMapSection
import java.util.*

class WorldMapDetailsDecoder : DefinitionDecoder<WorldMapDefinition>(WORLD_MAP) {

    override fun size(cache: Cache): Int {
        return cache.lastFileId(index, cache.getArchiveId(index, "details"))
    }

    override fun load(definitions: Array<WorldMapDefinition>, cache: Cache, id: Int) {
        val archive = cache.getArchiveId(index, "details")
        val file = getFile(id)
        val data = cache.getFile(index, archive, file) ?: return
        read(definitions, id, BufferReader(data))
    }

    override fun create(size: Int) = Array(size) { WorldMapDefinition(it) }

    override fun readLoop(definition: WorldMapDefinition, buffer: Reader) {
        definition.read(-1, buffer)
    }

    override fun WorldMapDefinition.read(opcode: Int, buffer: Reader) {
        map = buffer.readString()
        name = buffer.readString()
        position = buffer.readInt()
        anInt9542 = buffer.readInt()// Size?
        static = buffer.readUnsignedBoolean()
        anInt9547 = buffer.readUnsignedByte()// Always zero except "Braindeath Island" which is -1
        buffer.readUnsignedByte()

        if (anInt9547 == 255) {
            anInt9547 = 0
        }
        sections = LinkedList()
        val length = buffer.readUnsignedByte()
        for (i in 0 until length) {
            sections!!.addLast(
                WorldMapSection(
                    buffer.readUnsignedByte(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort()
                )
            )
        }
    }

    override fun changeValues(definitions: Array<WorldMapDefinition>, definition: WorldMapDefinition) {
        definition.minX = 12800
        definition.minY = 12800
        definition.maxX = 0
        definition.maxY = 0

        definition.sections?.forEach { section ->
            if (definition.minX > section.startX) {
                definition.minX = section.startX
            }
            if (definition.minY > section.startY) {
                definition.minY = section.startY
            }
            if (definition.maxX < section.endX) {
                definition.maxX = section.endX
            }
            if (definition.maxY < section.endY) {
                definition.maxY = section.endY
            }
        }
    }
}