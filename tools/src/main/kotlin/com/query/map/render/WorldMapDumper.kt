package com.query.map.render

import com.query.ApplicationSettings
import com.query.Pipeline
import com.query.cache
import com.query.cache.Cache
import com.query.cache.definition.data.MapSceneDefinition
import com.query.cache.definition.data.ObjectDefinitionFull
import com.query.cache.definition.data.SpriteDefinition
import com.query.cache.definition.data.TextureDecoder
import com.query.cache.definition.decoder.*
import com.query.currentCache
import com.query.map.ProgressListener
import com.query.map.draw.MinimapIconPainter
import com.query.map.draw.RegionRenderer
import com.query.map.load.MapTileSettings
import com.query.map.load.RegionManager
import com.query.map.region.Xteas
import com.query.types.Region
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.io.File

class WorldMapDumper(val onProgress: (Pair<String,String>) -> Unit) : ProgressListener {


    var manager : RegionManager? = null
    var settings : MapTileSettings? = null
    var objectDecoder : Array<ObjectDefinitionFull> = emptyArray()
    var spriteDecoder : Array<SpriteDefinition> = emptyArray()
    var mapSceneDecoder : Array<MapSceneDefinition> = emptyArray()
    var loader : MinimapIconPainter? = null

    companion object {
        val FILTER_VIEWPORT = true
        val DISPLAY_ZONES = false
        var minimapIcons = false
    }

    fun init() {

    }

    fun dumpMaps(onComplete: () -> Unit) {
        stopKoin()
        val koin = startKoin {
            modules(
                module {
                    single { cache as Cache }
                    single { MapDecoder(get<Xteas>()) }
                    single(createdAtStart = true) {
                        Xteas().load(ApplicationSettings.findXteas(currentCache!!).absolutePath)
                    }
                })
        }.koin

        val cache: Cache = koin.get()
        val mapDefinitions = MapDecoder(koin.get<Xteas>()).loadCache(cache).associateBy { it.id }
        onProgress.invoke("Loading Maps" to "10")
        objectDecoder = ObjectDecoderFull(members = true, lowDetail = false).loadCache(cache)
        onProgress.invoke("Loading Objects" to "20")
        val overlayDefinitions = OverlayDecoder().loadCache(cache)
        onProgress.invoke("Loading Overlays" to "30")
        val underlayDefinitions = UnderlayDecoder().loadCache(cache)
        onProgress.invoke("Loading Undelrays" to "40")
        val textureDefinitions = TextureDecoder().loadCache(cache)
        onProgress.invoke("Loading Textures" to "50")
        val worldMapDecoder = WorldMapDetailsDecoder().loadCache(cache)
        onProgress.invoke("Loading World Map Details" to "60")
        val worldMapInfoDecoder = WorldMapInfoDecoder().loadCache(cache)
        onProgress.invoke("Loading Maps" to "70")
        spriteDecoder = SpriteDecoder().loadCache(cache)
        mapSceneDecoder = MapSceneDecoder().loadCache(cache)
        onProgress.invoke("Loading Maps" to "80")
        val loader = MinimapIconPainter(objectDecoder, worldMapDecoder, worldMapInfoDecoder, spriteDecoder)
        loader.startup(cache)
        manager = RegionManager(mapDefinitions, 3)
        settings = MapTileSettings(4, underlayDefinitions, overlayDefinitions, textureDefinitions, manager = manager!!)
        onProgress.invoke("Loading Maps" to "90")
        val pipeline = Pipeline<Region>()
        pipeline.add(RegionRenderer(manager!!, objectDecoder, spriteDecoder, mapSceneDecoder, loader, settings!!))

        val regions = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                cache.getFile(5, "m${regionX}_${regionY}") ?: continue
                regions.add(Region(regionX, regionY))
            }
        }
        onProgress.invoke("Gathering Maps" to "95")
        var regionsToRemove = regions.filter { region ->
            // Check if any file exists for this region at any level
            (0 until 4).any { level ->
                val filePath = "/map/images/$level/${region.id}.png"
                val file = File(ApplicationSettings.findCache(currentCache!!).absolutePath + filePath)
                file.exists()
            }
        }


        regions.removeAll(regionsToRemove)

        var progress = 0
        onProgress.invoke("Gathering Maps" to "100")

        regions.forEach {
            pipeline.process(it)
            onProgress.invoke("Creating Maps" to "$progress / ${regions.size}")
            progress++
        }
        onComplete.invoke()
    }

    override fun onProgressUpdate(progress: Int) {
        TODO("Not yet implemented")
    }

}