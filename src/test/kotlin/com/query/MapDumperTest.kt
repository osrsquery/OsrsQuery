package com.query

import com.query.Application.areas
import com.query.Application.objects
import com.query.Application.overlays
import com.query.Application.sprites
import com.query.Application.textures
import com.query.Application.underlays
import com.query.cache.definitions.impl.*
import com.query.cache.download.CacheLoader
import com.query.cache.map.MapImageGenerator
import com.query.cache.map.builders.MapImageBuilder
import com.query.utils.TimeUtils
import com.query.utils.revisionBefore
import mu.KotlinLogging
import kotlin.system.measureTimeMillis


private val logger = KotlinLogging.logger {}
object MapDumperTest {

    fun extract() {
        CacheLoader.initialize()

        ObjectProvider(null,false).run()
        OverlayProvider(null,false).run()
        UnderlayProvider(null,false).run()
        if(!revisionBefore(142)) {
            AreaProvider(null,false).run()
        }
        SpriteProvider(null,false).run()
        TextureProvider(null,false).run()

        val map = MapImageBuilder().
            outline(true).
            label(true).
            functions(true).
            mapScenes(true).
            objects(true).
            fill(true).
            scale(4)
        .build()

        val dumper = MapImageGenerator(map)
        dumper.objects = objects().associateBy { it.id }
        dumper.overlays = overlays().associateBy { it.id }
        dumper.underlays = underlays().associateBy { it.id }
        if(!revisionBefore(142)) {
            dumper.areas = areas().associateBy { it.id }
        }

        dumper.textures = textures().associateBy { it.id }
        dumper.sprites = sprites().associateBy { it.id }

        val timer = measureTimeMillis {
            dumper.draw()
        }
        logger.info { "Map Images Written in ${TimeUtils.millsToFormat(timer)}" }


    }

}

fun main() {
    MapDumperTest.extract()
}