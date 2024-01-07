package com.query.cache.definition.data

import com.query.cache.Definition

class MapDefinition(
    override var id: Int = -1,
    val tiles: LongArray = LongArray(64 * 64 * 4),
    val objects: MutableList<MapObject> = mutableListOf()
) : Definition {

    fun getTile(localX: Int, localY: Int, level: Int) = MapTile(tiles[index(localX, localY, level)])

    fun setTile(localX: Int, localY: Int, level: Int, tile: MapTile) {
        tiles[index(localX, localY, level)] = tile.packed
    }

    companion object {
        internal fun index(localX: Int, localY: Int, level: Int): Int {
            return (level shl 12) + (localX shl 6) + localY
        }
        internal fun localX(tile: Int) = tile shr 6 and 0x3f
        internal fun localY(tile: Int) = tile and 0x3f
        internal fun level(level: Int) = level shr 12 and 0x3
    }
}