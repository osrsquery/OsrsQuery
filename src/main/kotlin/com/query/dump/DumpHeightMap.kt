package com.query.dump

import com.query.cache.map.HeightMapGenerator
import com.query.cache.map.builders.HeightMapImageBuilder
import com.query.utils.FileUtils
import com.query.utils.TimeUtils
import mu.KotlinLogging
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

object HeightMapDumper {

    fun init() {
        var dumper = HeightMapGenerator(HeightMapImageBuilder().scale(4).viewable(true).build())
        val timer = measureTimeMillis {
            dumper.drawHeightMap()
        }
        logger.info { "Map Images Written in ${TimeUtils.millsToFormat(timer)}" }

    }

}