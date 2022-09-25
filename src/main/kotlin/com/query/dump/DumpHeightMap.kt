package com.query.dump

import com.query.game.map.HeightMapGenerator
import com.query.game.map.builders.HeightMapImageBuilder
import com.query.utils.TimeUtils
import mu.KotlinLogging
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

object HeightMapDumper {

    fun init() {
        val dumper = HeightMapGenerator(HeightMapImageBuilder().scale(4).viewable(true).build())
        val timer = measureTimeMillis {
            dumper.drawHeightMap()
        }
        logger.info { "Map Images Written in ${TimeUtils.millsToFormat(timer)}" }

    }

}