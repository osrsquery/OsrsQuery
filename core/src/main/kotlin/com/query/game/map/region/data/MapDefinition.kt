package com.query.game.map.region.data

import com.query.game.map.region.regionSizeX
import com.query.game.map.region.regionSizeY
import com.query.game.map.region.regionSizeZ

data class MapDefinition(
    val regionX : Int,
    val regionY: Int,
    val tiles: Array<Array<Array<Tile>>> = Array(regionSizeZ) { Array(regionSizeX) { Array(regionSizeY) { Tile() } } }
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapDefinition

        if (regionX != other.regionX) return false
        if (regionY != other.regionY) return false
        if (!tiles.contentDeepEquals(other.tiles)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = regionX
        result = 31 * result + regionY
        result = 31 * result + tiles.contentDeepHashCode()
        return result
    }
}