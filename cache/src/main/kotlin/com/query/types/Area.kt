package com.query.types

import com.query.types.Zone
import com.query.types.area.Cuboid
import com.query.types.area.Polygon
import com.query.types.area.Rectangle

/**
 * Represents a tiled area of any size or shape
 */
interface Area : Iterable<Tile> {
    val area: Double

    operator fun contains(tile: Tile): Boolean = contains(tile.x, tile.y, tile.level)

    fun contains(x: Int, y: Int, level: Int = 0): Boolean

    fun random(): Tile

    fun toRegions(): List<Region>

    fun toZones(level: Int = 0): List<Zone>

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>, maxLevel: Int): Area {
            val x = map["x"] as List<Int>
            val y = map["y"] as List<Int>
            val level = map["level"] as? Int
            return if (x.size <= 2) {
                if (level == null) {
                    Rectangle(x.first(), y.first(), x.last(), y.last())
                } else {
                    Cuboid(x.first(), y.first(), x.last(), y.last(), level, level)
                }
            } else {
                Polygon(x.toIntArray(), y.toIntArray(), level ?: 0, level ?: maxLevel)
            }
        }
    }
}