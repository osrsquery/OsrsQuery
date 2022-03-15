package com.query.cache.map

import com.query.cache.map.builders.HeightMapImageBuilder
import com.query.cache.map.region.RegionLoader
import com.query.cache.map.region.regionSizeX
import com.query.cache.map.region.regionSizeY
import mu.KotlinLogging
import java.awt.Color
import java.awt.image.BufferedImage

private val logger = KotlinLogging.logger {}

class HeightMapGenerator(private val builder : HeightMapImageBuilder) {

    private var regionLoader: RegionLoader = RegionLoader()

    /**
     * Main Method to set up Height Map Image
     * @param  z The plane of the region you wish to draw
     * @return The Full map Image
     */
    fun drawHeightMap(z: Int): BufferedImage {
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
            true -> BufferedImage.TYPE_INT_ARGB
        }

        val image = BufferedImage(dimX, dimY, type)
        draw(image, z)
        return image
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