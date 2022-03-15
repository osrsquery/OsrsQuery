package com.query.dump

import com.query.cache.map.HeightMapGenerator
import com.query.cache.map.builders.HeightMapImageBuilder
import com.query.utils.FileUtils
import mu.KotlinLogging
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}
object HeightMapDumper {

    fun init() {
        var dumper = HeightMapGenerator(HeightMapImageBuilder().scale(4).viewable(false).build())
        var image = dumper.drawHeightMap(0)
        val imageFile = FileUtils.getFile("mapImages/", "heightmap.png")
        ImageIO.write(image, "png", imageFile)
        logger.info("Heightmap Image Dumped {}", imageFile)
        dumper = HeightMapGenerator(HeightMapImageBuilder().scale(4).viewable(false).build())
        logger.info("Heightmap Image Dumped {}", imageFile)

        image = dumper.drawHeightMap(0)
        val imageFileViewable = FileUtils.getFile("mapImages/", "heightmap-viewable.png")
        ImageIO.write(image, "png", imageFileViewable)
        logger.info("Heightmap Viewable Image Dumped {}", imageFileViewable)

    }

}