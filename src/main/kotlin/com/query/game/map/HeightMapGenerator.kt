package com.query.game.map

import com.query.Application
import com.query.game.map.builders.HeightMapImageBuilder
import com.query.game.map.region.RegionLoader
import com.query.game.map.region.regionSizeX
import com.query.game.map.region.regionSizeY
import com.query.utils.image.BigBufferedImage
import com.query.utils.FileUtil
import com.query.utils.revisionBefore
import mu.KotlinLogging
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

class HeightMapGenerator(private val builder : HeightMapImageBuilder) {

    private var regionLoader: RegionLoader = RegionLoader()

    /**
     * Main Method to set up Height Map Image
     * @param  z The plane of the region you wish to draw
     * @return The Full map Image
     */
    fun drawHeightMap() {
        val minX = regionLoader.lowestX.baseX
        val minY = regionLoader.lowestY.baseY
        val maxX: Int = regionLoader.highestX.baseX + regionSizeX
        val maxY: Int = regionLoader.highestY.baseY + regionSizeY
        var dimX = maxX - minX
        var dimY = maxY - minY
        dimX *= builder.scale
        dimY *= builder.scale
        logger.info {
            "Map image dimensions: $dimX px x$dimY px, ${builder.scale}.scale px per map square (${dimX * dimY / 1024 / 1024} MB)"
        }

        val type = when(builder.viewable) {
            false -> BufferedImage.TYPE_USHORT_GRAY
            true -> BufferedImage.TYPE_INT_RGB
        }

        logger.info {
            "====== Setting Height Map Drawing Map Image  =====\n" +
                    "Options: ${builder}\n" +
                    "Image Size: $dimX px x $dimY px\n" +
                    "Size: ${dimX * dimY * 3 / 1024 / 1024} MB\n" +
                    "Memory: ${Runtime.getRuntime().maxMemory() / 1024L / 1024L}mb\n" +
                    "North most region: ${regionLoader.lowestX.baseX}\n" +
                    "South most region: ${regionLoader.highestY.baseY}\n" +
                    "West most region: ${regionLoader.lowestX.baseX}\n" +
                    "East most region: ${regionLoader.highestY.baseY}\n" +
                    "====== Starting Height Map Drawing Map Image =====\n"
        }

        for (plane in 0..3) {
            logger.info { "Generating map images for plane = $plane" }
            val image: BufferedImage = BigBufferedImage.create(dimX, dimY, type)
            val graphics = image.createGraphics()
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            draw(image,plane)

            ImageIO.write(image, "png", FileUtil.getFile("mapImages/", "height-map-$plane.png"))
        }

    }

    /**
     * Draw the tiles to the Buffer Image
     * @param  image Image
     * @param  z The plane of the region you wish to draw
     */
    private fun draw(image: BufferedImage, z: Int) {
        var max = Int.MIN_VALUE
        var min = Int.MAX_VALUE
        for (region in regionLoader.getRegions()) {
            val baseX: Int = region.baseX
            val baseY: Int = region.baseY

            val drawBaseX = baseX - regionLoader.lowestX.baseX

            val drawBaseY = regionLoader.highestY.baseY - baseY
            for (x in 0 until regionSizeX) {
                val drawX = drawBaseX + x
                for (y in 0 until regionSizeY) {
                    val drawY: Int = drawBaseY + (regionSizeY - 1 - y)
                    val height = region.getTileHeight(z, x, y)
                    if (height > max) {
                        max = height
                    }
                    if (height < min) {
                        min = height
                    }
                    val rgb = toColor(height)
                    drawMapSquare(image, drawX, drawY, rgb)
                }
            }
        }
    }

    /**
     * Draws the color to the tile based on the scale
     * @param  image Image
     * @param  x  Top left
     * @param  y Bottom Right
     * @param  rgb Color to  render
     */
    private fun drawMapSquare(image: BufferedImage, x: Int, y: Int, rgb: Int) {
        var mapX = x
        var mapY = y
        mapX *= builder.scale
        mapY *= builder.scale
        for (i in 0 until builder.scale) {
            for (j in 0 until builder.scale) {
                image.setRGB(mapX + i, mapY + j, rgb)
            }
        }
    }

    /**
     * Gets the Color to draw onto the Tile
     * @param  height Height of the tile
     * @return returns the Color based on if the map is viewable or is for data
     */
    private fun toColor(height: Int): Int {

        val red = ((-height) and 0xFF).toUByte().toInt()
        val green = ((-height) shr 8 and 0xFF).toUByte().toInt()
        val blue = ((-height) shr 16 and 0xFF).toUByte().toInt()

        if (builder.viewable) {
            val red1 = (red * 0.299).toInt()
            val green1 = (green * 0.587).toInt()
            val blue1 = (blue * 0.114).toInt()
            val newColor = Color(
                red1 + green1 + blue1,
                red1 + green1 + blue1,
                red1 + green1 + blue1
            )
            return newColor.rgb
        }

        return Color(red,green,blue).rgb
    }


}