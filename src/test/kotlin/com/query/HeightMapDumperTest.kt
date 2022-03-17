package com.query

import com.query.cache.download.CacheLoader
import com.query.cache.map.HeightMapGenerator
import com.query.cache.map.builders.HeightMapImageBuilder
import com.query.utils.FileUtils.getFile
import com.query.utils.TimeUtils
import mu.KotlinLogging
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

object HeightMapDumperTest {

    fun extract() {
        CacheLoader.initialize()

        var dumper = HeightMapGenerator(HeightMapImageBuilder().scale(4).viewable(true).build())
        val timer = measureTimeMillis {
            dumper.drawHeightMap()
        }
        logger.info { "Map Images Written in ${TimeUtils.millsToFormat(timer)}" }


    }

}

fun main() {
    HeightMapDumperTest.extract()
}