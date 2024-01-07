package com.query.cache.definition.decoder

import com.query.buffer.read.Reader
import com.query.cache.Config.MAP_SCENES
import com.query.cache.definition.ConfigDecoder
import com.query.cache.definition.data.MapSceneDefinition

class MapSceneDecoder : ConfigDecoder<MapSceneDefinition>(MAP_SCENES) {

    override fun create(size: Int) = Array(size) { MapSceneDefinition(it) }

    override fun MapSceneDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> sprite = buffer.readShort()
            2 -> colour = buffer.readUnsignedMedium()
            3 -> aBoolean1741 = true
            4 -> sprite = -1
        }
    }
}