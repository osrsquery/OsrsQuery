package com.osrsquery.map

import com.google.gson.Gson
import com.query.Application
import com.query.cache.CacheManager
import com.query.cache.Xtea
import com.query.cache.XteaLoader
import com.query.cache.definitions.impl.*
import com.query.game.map.MapImageGenerator
import com.query.game.map.builders.MapImageBuilder
import com.query.utils.TimeUtils
import com.query.utils.revisionIsOrBefore
import java.io.File
import kotlin.system.measureTimeMillis

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
            .sortedBy { it.builds.first().major }

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

        val map = MapImageBuilder().
            outline(false).
            label(false).
            functions(true).
            mapScenes(true).
            objects(true).
            fill(false).
            scale(4)
        .build()

        updatedTilesPerRev.forEach {
            CacheManager.initialize(it.key)

            ObjectProvider(null).run()
            OverlayProvider(null).run()
            UnderlayProvider(null).run()
            AreaProvider(null).run()
            TextureProvider(null).run()
            SpriteProvider(null).run()


            val dumper = MapImageGenerator(map)

            dumper.objects = Application.objects().associateBy { it.id }
            dumper.overlays = Application.overlays().associateBy { it.id }
            dumper.underlays = Application.underlays().associateBy { it.id }
            if(!revisionIsOrBefore(142)) {
                dumper.areas = Application.areas().associateBy { it.id }
            }

            dumper.textures = Application.textures().associateBy { it.id }
            dumper.sprites = Application.sprites().associateBy { it.id }

            val timer = measureTimeMillis {
                for (level in 0 until 4) {
                    dumper.draw(File("E:\\RSPS\\OsrsQuery\\OsrsQuery\\tiles\\rev-${it.key}//","map-${level}.png"),level)
                }
            }
            println("Map Images Written in ${TimeUtils.millsToFormat(timer)}")
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