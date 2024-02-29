package com.osrsquery.map

import com.google.gson.Gson
import com.query.cache.CacheManager
import com.query.cache.Xtea
import com.query.cache.XteaLoader
import com.query.cache.Xteas
import com.query.game.map.region.Region
import java.io.File

data class TileInfo(val x: Int, val y : Int, val region : Int)

object DumpMapImages {

    val updatedTilesPerRev: MutableMap<Int, MutableList<TileInfo>> = mutableMapOf()

    fun init() {
        // Retrieve old school caches with builds and timestamps
        val oldschoolCaches = CacheManager.findAllCaches().filter { it.game == "oldschool" && it.builds.isNotEmpty() && it.timestamp != null }

        // Group caches by major version and get the latest entry for each major version
        val latestMajorEntries = oldschoolCaches.groupBy { it.builds.first().major }
            .map { (_, entries) -> entries.maxByOrNull { it.timestamp } }
            .filterNotNull()
            .sortedBy { it.builds.first().major }.take(1)

        // Initialize a mutable map to store Xtea keys for comparison
        var compare: MutableMap<Int, IntArray> = emptyMap<Int, IntArray>().toMutableMap()

        println("Setting rev 1")
        CacheManager.initialize(1)

        // Load Xtea keys into the comparison map
        XteaLoader.xteas.forEach {
            val regionID = it.value.mapsquare
            compare[regionID] = it.value.key
            val pos = it.value.name.replace("l","").split("_")
            updatedTilesPerRev.computeIfAbsent(1) { mutableListOf() }.add(TileInfo(pos[0].toInt(),pos[1].toInt(),regionID))
        }

        // Check Xtea keys for each major version
        latestMajorEntries.forEach { entry ->
            val build = entry.builds.first().major
            println("Checking: ${build}")
            CacheManager.initialize(build)
            val regionsToUpdate = checkXteaKeys(compare, XteaLoader.xteas)
            regionsToUpdate.forEach { region ->
                val pos = region.name.replace("l","").split("_")
                updatedTilesPerRev.computeIfAbsent(build) { mutableListOf() }.add(TileInfo(pos[0].toInt(),pos[1].toInt(),region.mapsquare))
            }

            // Update the comparison map with the current Xtea keys
            compare = XteaLoader.xteasList.toMutableMap()
        }

        updatedTilesPerRev.forEach {
            WorldMapDumper.dumpMaps(it.key, it.value)
        }
        File("./key.json",Gson().toJson(updatedTilesPerRev))


    }

    private fun checkXteaKeys(compare: Map<Int, IntArray>, xteasList: Map<Int,Xtea>): List<Xtea> {
        val regionsToUpdate = mutableListOf<Xtea>()

        xteasList.forEach { xteaEntry ->
            val regionID = xteaEntry.value.mapsquare
            val compareAgainst = compare[regionID]

            if (compareAgainst == null || !compareAgainst.contentEquals(xteaEntry.value.key)) {
                regionsToUpdate.add(xteaEntry.value)
            }
        }

        return regionsToUpdate
    }
}

fun main() {
    DumpMapImages.init()
}