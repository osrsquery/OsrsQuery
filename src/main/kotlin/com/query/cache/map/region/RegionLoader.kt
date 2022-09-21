package com.query.cache.map.region

import com.query.Constants.library
import com.query.cache.XteaLoader.getKeys
import java.io.IOException
import java.nio.ByteBuffer


val DEFAULT_REGION = Region(-1)

class RegionLoader {


    private val regions: MutableMap<Int, Region> = HashMap()

    var lowestX: Region = DEFAULT_REGION
    var lowestY: Region = DEFAULT_REGION
    var highestX: Region = DEFAULT_REGION
    var highestY: Region = DEFAULT_REGION

    @Throws(IOException::class)
    fun loadRegions() {
        for (i in 0 until MAX_REGION) {
            val region = loadRegionFromArchive(i)
            if (region != null) {
                regions[i] = region
            }
        }
    }

    @Throws(IOException::class)
    fun loadRegionFromArchive(regionId: Int): Region? {
        val x = regionId shr 8
        val y = regionId and 0xFF
        val map: ByteArray = library.data(5, "m" + x + "_" + y) ?: return null
        var data = map
        val mapDef = MapLoader().load(x, y, data)
        val region = Region(regionId)
        region.loadTerrain(mapDef)
        val keys = getKeys(regionId)
        if (keys != null) {
            data = library.data(5, "l" + x + "_" + y, keys)!!
            val locDef = LocationsLoader().load(x, y, data)
            region.loadLocations(locDef)
        }
        return region
    }

    init {
        loadRegions()
        regions.values.forEach {
            if (lowestX == DEFAULT_REGION || it.baseX < lowestX.baseX) {
                lowestX = it
            }
            if (highestX == DEFAULT_REGION || it.baseX > highestX.baseX) {
                highestX = it
            }
            if (lowestY == DEFAULT_REGION || it.baseY < lowestY.baseY) {
                lowestY = it
            }
            if (highestY == DEFAULT_REGION || it.baseY > highestY.baseY) {
                highestY = it
            }
        }

    }
    fun getRegions() = regions.values

    fun findRegionForWorldCoordinates(baseX: Int, baseY: Int): Region? {
        val x = baseX ushr 6
        val y = baseY ushr 6
        return regions[x shl 8 or y]
    }

    companion object {
        const val MAX_REGION = 32768
    }

}