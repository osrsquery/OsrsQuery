package com.query.dump.dumper317

import com.query.Application
import com.query.Constants
import com.query.cache.download.CacheLoader
import com.query.cache.map.MapImageGenerator
import com.query.cache.map.builders.MapImageBuilder
import com.query.utils.FileUtils
import com.query.utils.TimeUtils
import com.query.utils.progress
import com.query.utils.revisionBefore
import mu.KotlinLogging
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

object Dumper  {
    fun init() {
        TextureDumper.init()
        ModelDumper.init()
        val progress = progress("Copying Existing Stuff", 3)
        FileUtils.getDir("mapsScenes/").copyTo(FileUtils.getDir("dump317/mapsScenes/"),true)
        progress.step()
        FileUtils.getDir("mapsFunctions/").copyTo(FileUtils.getDir("dump317/mapsFunctions/"),true)
        progress.step()
        FileUtils.getDir("sprites/").copyTo(FileUtils.getDir("dump317/sprites/"),true)
        progress.step()
        
        progress.close()
        VarbitDumper.init()
        FloorDumper.init()
        AreaDumper.init()
        KitDumper.init()
        SequenceDumper.init()
        SpotAnimDumper.init()
        NpcDumper.init()
        ObjectDumper.init()
        ItemDumper.init()


        val map = MapImageBuilder().
        outline(true).
        label(true).
        functions(true).
        mapScenes(true).
        objects(true).
        fill(false).
        scale(4)
            .build()

        val dumper = MapImageGenerator(map, FileUtils.getDir("dump317/mapImages/"))
        dumper.objects = Application.objects().associateBy { it.id }
        dumper.overlays = Application.overlays().associateBy { it.id }
        dumper.underlays = Application.underlays().associateBy { it.id }
        if(!revisionBefore(142)) {
            dumper.areas = Application.areas().associateBy { it.id }
        }

        dumper.textures = Application.textures().associateBy { it.id }
        dumper.sprites = Application.sprites().associateBy { it.id }

        val timer = measureTimeMillis {
            dumper.draw()
        }
        logger.info { "Map Images Written in ${TimeUtils.millsToFormat(timer)}" }
        MapDumper.init()

    }

}