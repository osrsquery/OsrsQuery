package com.osrsquery.map

import com.query.Application
import com.query.cache.CacheManager
import com.query.cache.definitions.impl.*
import com.query.dump.MapDumper
import com.query.game.map.MapImageGenerator
import com.query.game.map.builders.MapImageBuilder
import com.query.game.map.region.Region
import com.query.game.map.region.data.Tile
import com.query.utils.progress
import com.query.utils.revisionIsOrBefore
import kotlinx.coroutines.*
import me.tongfei.progressbar.ProgressBar
import java.io.File

object WorldMapDumper {

    fun dumpMaps(build : Int, regions : List<TileInfo>) {
        CacheManager.initialize(build)
        val map = MapImageBuilder().setRegion(12853).label(false).outline(false)

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

        val progressBar = progress("Dumping Map Tiles", (regions.size) * 4)

        runBlocking {
            processAllRegionsAsync(dumper, build, progressBar, regions)
        }

        progressBar.close()
    }


    private suspend fun processRegionAsync(dumper: MapImageGenerator, build: Int, progressBar: ProgressBar, it: TileInfo): Unit = coroutineScope {
        (0 until 4).map { level ->
            async(Dispatchers.Default) {
                val file = File("tiles/rev-${build}/${level}/10/${it.x}/${it.y}.png")
                dumper.draw(file, level)
                progressBar.step()
            }
        }.awaitAll()
    }

    suspend fun processAllRegionsAsync(dumper: MapImageGenerator, build: Int, progressBar: ProgressBar, regions: List<TileInfo>) {
        regions.forEach { region ->
            dumper.setRegion(region.region)
            processRegionAsync(dumper, build, progressBar, region)
        }
    }

}

fun main() {
    WorldMapDumper.dumpMaps(220, listOf(TileInfo(50,53,12853)))
}