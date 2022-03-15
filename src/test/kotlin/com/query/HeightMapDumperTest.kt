package com.query

import com.query.cache.download.CacheLoader
import com.query.cache.map.HeightMapGenerator
import com.query.cache.map.builders.HeightMapImageBuilder
import com.query.utils.FileUtils.getFile
import mu.KotlinLogging
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

object HeightMapDumperTest {

    fun extract() {
        CacheLoader.initialize()

        var dumper = HeightMapGenerator(HeightMapImageBuilder().scale(4).viewable(false).build())
        var image = dumper.drawHeightMap(0)
        val imageFile = getFile("mapImages/","heightmap.png")
        ImageIO.write(image, "png", imageFile)
        logger.info("Heightmap Image Dumped {}", imageFile)
        image = BufferedImage(1,1,1)
        dumper = HeightMapGenerator(HeightMapImageBuilder().scale(4).viewable(true).build())

        image = dumper.drawHeightMap(0)
        val imageFileViewable = getFile("mapImages/","heightmap-viewable.png")
        ImageIO.write(image, "png", imageFileViewable)
        logger.info("Heightmap Viewable Image Dumped {}", imageFileViewable)

    }

}

fun main() {
    HeightMapDumperTest.extract()
}