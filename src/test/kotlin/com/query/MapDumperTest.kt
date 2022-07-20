package com.query

import com.query.Application.areas
import com.query.Application.mapScene
import com.query.Application.objects
import com.query.Application.overlays
import com.query.Application.sprites
import com.query.Application.underlays
import com.query.cache.definitions.impl.*
import com.query.cache.download.CacheLoader
import com.query.cache.map.MapImageGenerator
import com.query.cache.map.builders.MapImageBuilder
import com.query.utils.Colors
import com.query.utils.Colors.getAverageColor
import com.query.utils.FileUtils.getDir
import com.query.utils.TimeUtils
import mu.KotlinLogging
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


private val logger = KotlinLogging.logger {}
object MapDumperTest {

    fun extract() {
        CacheLoader.initialize()

        ObjectProvider(null,false).run()
        OverlayProvider(null,false).run()
        UnderlayProvider(null,false).run()
        AreaProvider(null,false).run()
        SpriteProvider(null,false).run()
        MapSceneProvider(null,false).run()

        val map = MapImageBuilder().
            outline(false).
            label(false).
            functions(true).
            mapScenes(false).
            objects(true).
            fill(false).
            scale(4)
        .build()

        val dumper = MapImageGenerator(map)
        dumper.objects = objects().associateBy { it.id }
        dumper.overlays = overlays().associateBy { it.id }
        dumper.underlays = underlays().associateBy { it.id }
        dumper.areas = areas().associateBy { it.id }
        dumper.textures = emptyMap<Int, Int>().toMutableMap()
        getDir("/textures/").listFiles().forEach {
            if(it.extension == "png")  {
                val color = Colors.hslToRgb(Colors.multiplyLightness(getAverageColor(ImageIO.read(it)), 96))
                println(it.nameWithoutExtension.replace("texture_",""))
                dumper.textures[it.nameWithoutExtension.replace("texture_","").toInt()] = color
            }
        }
        dumper.sprites = sprites().associateBy { it.id }
        dumper.mapScene = mapScene().associateBy { it.id }

        val timer = measureTimeMillis {
            dumper.draw()
        }
        logger.info { "Map Images Written in ${TimeUtils.millsToFormat(timer)}" }


    }

}

fun main() {
    Application.revision = 667
    MapDumperTest.extract()
}