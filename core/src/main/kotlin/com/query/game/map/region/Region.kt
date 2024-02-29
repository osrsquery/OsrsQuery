package com.query.game.map.region

import com.query.Application
import com.query.Constants.OVERLAY_SHORT_BREAKING_CHANGE_REV_NUMBER
import com.query.game.map.region.HeightCalculations.calculate
import com.query.game.map.region.data.Location
import com.query.game.map.region.data.LocationsDefinition
import com.query.game.map.region.data.MapDefinition
import com.query.utils.Position
import kotlin.experimental.and

const val regionSizeX = 64
const val regionSizeY = 64
const val regionSizeZ = 4

class Region(id : Int) {

    private val readOverlayAsByte = Application.revision >= OVERLAY_SHORT_BREAKING_CHANGE_REV_NUMBER

    val regionID: Int
    val baseX: Int
    val baseY: Int

    init {
        regionID = id
        baseX = id shr 8 and 0xFF shl 6 // local coords are in bottom 6 bits (64*64)
        baseY = id and 0xFF shl 6
    }

    private val tileHeights = Array(regionSizeZ) { Array(regionSizeX) { IntArray(regionSizeY) } }
    private val tileSettings = Array(regionSizeZ) { Array(regionSizeX) { ByteArray(regionSizeY) } }
    private val overlayIds = Array(regionSizeZ) { Array(regionSizeX) { ShortArray(regionSizeY) } }
    private val overlayPaths = Array(regionSizeZ) { Array(regionSizeX) { ByteArray(regionSizeY) } }
    private val overlayRotations = Array(regionSizeZ) { Array(regionSizeX) { ByteArray(regionSizeY) } }
    private val underlayIds = Array(regionSizeZ) { Array(regionSizeX) { ShortArray(regionSizeY) } }
    private val locations: MutableList<Location> = ArrayList()

    fun loadTerrain(map: MapDefinition) {
        val tiles = map.tiles
        for (z in 0 until regionSizeZ) {
            for (x in 0 until regionSizeX) {
                for (y in 0 until regionSizeY) {
                    val tile = tiles[z][x][y]
                    if (tile!!.height == null) {
                        if (z == 0) {
                            tileHeights[0][x][y] = -calculate(baseX + x + 0xe3b7b, baseY + y + 0x87cce,x,y) * 8
                        } else {
                            tileHeights[z][x][y] = tileHeights[z - 1][x][y] - 240
                        }
                    } else {
                        var height = tile.height
                        if (height == 1) {
                            height = 0
                        }
                        if (z == 0) {
                            tileHeights[0][x][y] = -height!! * 8
                        } else {
                            tileHeights[z][x][y] = tileHeights[z - 1][x][y] - height!! * 8
                        }
                    }
                    overlayIds[z][x][y] = tile.overlayId
                    overlayPaths[z][x][y] = tile.overlayPath
                    overlayRotations[z][x][y] = tile.overlayRotation
                    tileSettings[z][x][y] = tile.settings
                    underlayIds[z][x][y] = tile.underlayId
                }
            }
        }
    }

    fun loadLocations(location: LocationsDefinition) {
        for ((id, type, orientation, position) in location.locations) {
            val newLoc = Location(
                id, type, orientation,
                Position(baseX + position.x, baseY + position.y, position.z)
            )
            locations.add(newLoc)
        }
    }

    fun getTileHeight(z: Int, x: Int, y: Int) = tileHeights[z][x][y]
    fun getTileSetting(z: Int, x: Int, y: Int) = tileSettings[z][x][y]

    fun getOverlayId(z: Int, x: Int, y: Int) = (overlayIds[z][x][y] and if(readOverlayAsByte) 0xFF else 0x7FFF).toInt()

    fun getUnderlayId(z: Int, x: Int, y: Int) = (underlayIds[z][x][y] and if(readOverlayAsByte) 0xFF else 0x7FFF).toInt()

    fun getOverlayPath(z: Int, x: Int, y: Int) = overlayPaths[z][x][y]
    fun getOverlayRotation(z: Int, x: Int, y: Int) = overlayRotations[z][x][y]
    fun isLinkedBelow(z: Int, x: Int, y: Int) = (getTileSetting(z, x, y) and 0x2) > 0
    fun isVisibleBelow(z: Int, x: Int, y: Int): Boolean = getTileSetting(z, x, y) and 0x8 > 0

    fun getLocations() = locations

    val regionX = baseX shr 6
    val regionY = baseY shr 6

}