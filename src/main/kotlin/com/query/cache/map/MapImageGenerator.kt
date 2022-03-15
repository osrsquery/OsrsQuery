
package com.query.cache.map

import SpriteData
import com.query.Constants
import com.query.cache.definitions.impl.*
import com.query.cache.map.builders.MapImageBuilder
import com.query.dump.impl.MapScene
import com.query.utils.*
import mu.KotlinLogging
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import kotlin.experimental.and
import com.query.cache.map.region.*

private val logger = KotlinLogging.logger {}

class MapImageGenerator(private val builder : MapImageBuilder) {

    private val wallColor = Color.WHITE.rgb
    private val doorColor = Color.RED.rgb

    private val scaledMapIcons: MutableMap<Int, Image> = HashMap()
    var regionLoader: RegionLoader = RegionLoader()

    var objects: Map<Int, ObjectDefinition> = HashMap()
    var overlays: Map<Int, OverlayDefinition> = HashMap()
    var underlays: Map<Int, UnderlayDefinition> = HashMap()
    var areas: Map<Int, AreaDefinition> = HashMap()
    var textures: Map<Int, TextureDefinition> = HashMap()
    var sprites: Map<Int, SpriteDefinition> = HashMap()

    init {
        resizeMapScene()
    }

    /**
     * Main Method to setup the Map drawing
     * @param  z The plane of the region you wish to draw
     * @return The Full map Image
     */
    fun drawMap(z: Int): BufferedImage {
        val minX = regionLoader.lowestX.baseX
        val minY = regionLoader.lowestY.baseY
        val maxX = regionLoader.highestX.baseX + regionSizeX
        val maxY = regionLoader.highestY.baseY + regionSizeY
        val dimX = maxX - minX
        val dimY = maxY - minY
        val pixelsX = dimX * builder.scale
        val pixelsY = dimY * builder.scale

        logger.info {
            "====== Setting Drawing Map Image  =====\n" +
            "Options: ${builder}\n" +
            "Image Size: $pixelsX px x $pixelsY px\n" +
            "Size: ${pixelsX * pixelsY * 3 / 1024 / 1024} MB\n" +
            "Memory: ${Runtime.getRuntime().maxMemory() / 1024L / 1024L}mb\n" +
            "North most region: ${regionLoader.lowestX.baseX}\n" +
            "South most region: ${regionLoader.highestY.baseY}\n" +
            "West most region: ${regionLoader.lowestX.baseX}\n" +
            "East most region: ${regionLoader.highestY.baseY}\n" +
            "====== Starting Drawing Map Image =====\n"
        }

        val image = BufferedImage(pixelsX, pixelsY, BufferedImage.TYPE_INT_RGB)
        drawMap(image, z)
        logger.info { "Adding Objects" }
        drawObjects(image, z)
        logger.info {"Adding Map Icons / Labels" }
        drawMapIcons(image, z)
        return image
    }

    /**
     * Renders and draws a Bufferedimage of the region asked for
     * @param  region  com.query.cache.map.region.Region of the world to draw
     * @param  z The plane of the region you wish to draw
     * @return      the image at the specified com.query.cache.map.region.Region
     */
    fun drawRegion(region: Region, z: Int): BufferedImage {
        val pixelsX = regionSizeX * builder.scale
        val pixelsY = regionSizeY * builder.scale
        val image = BufferedImage(pixelsX, pixelsY, BufferedImage.TYPE_INT_RGB)
        drawMap(image, 0, 0, z, region)
        drawObjects(image, 0, 0, region, z)
        renderMapIcons(image, 0, 0, region, z)
        return image
    }

    /**
     * Draw a Tile Object on the Image
     * @param  image  BufferImage to write to
     * @param  drawBaseX BaseX of the com.query.cache.map.region.Region
     * @param  drawBaseY BaseY of the com.query.cache.map.region.Region
     * @param  z Plane of the map
     * @param  region World com.query.cache.map.region.Region
     */
    private fun drawMap(image: BufferedImage, drawBaseX: Int, drawBaseY: Int, z: Int, region: Region) {
        val map = Array(regionSizeX * builder.scale) {
            IntArray(regionSizeY * builder.scale)
        }

        drawMap(map, region, z)
        var above: Array<IntArray>? = null
        if (z < 3) {
            above = Array(regionSizeX * builder.scale) {
                IntArray(regionSizeY * builder.scale)
            }
            drawMap(above, region, z + 1)
        }
        for (x in 0 until regionSizeX) {
            for (y in 0 until regionSizeY) {
                val isBridge = (region.getTileSetting(1, x, regionSizeY - y - 1) and 2) > 0
                val tileSetting = region.getTileSetting(z, x, regionSizeY - y - 1).toInt()
                if (!isBridge && (tileSetting and 24) < 1) {
                    drawTile(image, map, drawBaseX, drawBaseY, x, y)
                }
                if (z < 3 && isBridge) // client also has a check for &8 != 0 here
                {
                    drawTile(image, above?: error("Error Finding Above Pixels"), drawBaseX, drawBaseY, x, y)
                }
            }
        }
    }

    /**
     * Draw a Tile Object on the Image
     * @param  to  BufferImage to write to
     * @param  pixels Map Image Pixels
     * @param  drawBaseX BaseX of the com.query.cache.map.region.Region
     * @param  drawBaseY BaseY of the com.query.cache.map.region.Region
     * @param  x Tile X Position
     * @param  y Tile Y Position
     */
    private fun drawTile(to: BufferedImage, pixels: Array<IntArray>, drawBaseX: Int, drawBaseY: Int, x: Int, y: Int) {
        for (i in 0 until builder.scale) {
            for (j in 0 until builder.scale) {
                to.setRGB(
                    drawBaseX * builder.scale + x * builder.scale + i,
                    drawBaseY * builder.scale + y * builder.scale + j,
                    pixels[x * builder.scale + i][y * builder.scale + j]
                )
            }
        }
    }

    /**
     * Draw map Pixels on the Image
     * @param  pixels  Pixels of the Image
     * @param  region  com.query.cache.map.region.Region of the Map
     * @param  z Plane to look for objects in
     */
    private fun drawMap(pixels: Array<IntArray>, region: Region, z: Int) {
        val baseX = region.baseX
        val baseY = region.baseY
        val len = regionSizeX + BLEND * 2
        val hues = IntArray(len)
        val sats = IntArray(len)
        val light = IntArray(len)
        val mul = IntArray(len)
        val num = IntArray(len)
        val hasLeftRegion = regionLoader.findRegionForWorldCoordinates(baseX - 1, baseY) != null
        val hasRightRegion = regionLoader.findRegionForWorldCoordinates(baseX + regionSizeX, baseY) != null
        val hasUpRegion = regionLoader.findRegionForWorldCoordinates(baseX, baseY + regionSizeY) != null
        val hasDownRegion = regionLoader.findRegionForWorldCoordinates(baseX, baseY - 1) != null
        for (xi in (if (hasLeftRegion) -BLEND * 2 else -BLEND) until regionSizeX + if (hasRightRegion) BLEND * 2 else BLEND) {
            for (yi in (if (hasDownRegion) -BLEND else 0) until regionSizeY + if (hasUpRegion) BLEND else 0) {
                val xr = xi + BLEND
                if (xr >= (if (hasLeftRegion) -BLEND else 0) && xr < regionSizeX + (if (hasRightRegion) BLEND else 0)) {
                    val r = regionLoader.findRegionForWorldCoordinates(baseX + xr, baseY + yi)
                    if (r != null) {
                        val underlayId = r.getUnderlayId(z, convert(xr), convert(yi))
                        if (underlayId > 0) {
                            val underlay = findUnderlay(underlayId - 1)
                            hues[yi + BLEND] += underlay.hue
                            sats[yi + BLEND] += underlay.saturation
                            light[yi + BLEND] += underlay.lightness
                            mul[yi + BLEND] += underlay.hueMultiplier
                            num[yi + BLEND]++
                        }
                    }
                }
                val xl = xi - BLEND
                if (xl >= (if (hasLeftRegion) -BLEND else 0) && xl < regionSizeX + (if (hasRightRegion) BLEND else 0)) {
                    val r = regionLoader.findRegionForWorldCoordinates(baseX + xl, baseY + yi)
                    if (r != null) {
                        val underlayId = r.getUnderlayId(z, convert(xl), convert(yi))
                        if (underlayId > 0) {
                            val underlay = findUnderlay(underlayId - 1)
                            hues[yi + BLEND] -= underlay.hue
                            sats[yi + BLEND] -= underlay.saturation
                            light[yi + BLEND] -= underlay.lightness
                            mul[yi + BLEND] -= underlay.hueMultiplier
                            num[yi + BLEND]--
                        }
                    }
                }
            }
            if (xi in 0 until regionSizeX) {
                var runningHues = 0
                var runningSat = 0
                var runningLight = 0
                var runningMultiplier = 0
                var runningNumber = 0
                for (yi in (if (hasDownRegion) -BLEND * 2 else -BLEND) until regionSizeY + if (hasUpRegion) BLEND * 2 else BLEND) {
                    val yu = yi + BLEND
                    if (yu >= (if (hasDownRegion) -BLEND else 0) && yu < regionSizeY + (if (hasUpRegion) BLEND else 0)) {
                        runningHues += hues[yu + BLEND]
                        runningSat += sats[yu + BLEND]
                        runningLight += light[yu + BLEND]
                        runningMultiplier += mul[yu + BLEND]
                        runningNumber += num[yu + BLEND]
                    }
                    val yd = yi - BLEND
                    if (yd >= (if (hasDownRegion) -BLEND else 0) && yd < regionSizeY + (if (hasUpRegion) BLEND else 0)) {
                        runningHues -= hues[yd + BLEND]
                        runningSat -= sats[yd + BLEND]
                        runningLight -= light[yd + BLEND]
                        runningMultiplier -= mul[yd + BLEND]
                        runningNumber -= num[yd + BLEND]
                    }
                    if (yi in 0 until regionSizeY) {
                        val r = regionLoader.findRegionForWorldCoordinates(baseX + xi, baseY + yi)
                        if (r != null) {
                            val underlayId = r.getUnderlayId(z, convert(xi), convert(yi))
                            val overlayId = r.getOverlayId(z, convert(xi), convert(yi))
                            if (underlayId > 0 || overlayId > 0) {
                                var underlayHsl = -1
                                if (underlayId > 0) {
                                    val avgHue = runningHues * 256 / runningMultiplier
                                    val avgSat = runningSat / runningNumber
                                    var avgLight = runningLight / runningNumber
                                    // randomness is added to avgHue here
                                    if (avgLight < 0) {
                                        avgLight = 0
                                    } else if (avgLight > 255) {
                                        avgLight = 255
                                    }
                                    underlayHsl = hsl24to16(avgHue, avgSat, avgLight)
                                }
                                var underlayRgb = 0
                                if (underlayHsl != -1) {
                                    val var0 = method1792(underlayHsl, 96)
                                    underlayRgb = colorPalette[var0]
                                }
                                var shape: Int
                                var rotation: Int
                                var overlayRgb: Int = -1
                                if (overlayId == 0) {
                                    rotation = 0
                                    shape = 0
                                } else {
                                    shape = r.getOverlayPath(z, convert(xi), convert(yi)) + 1
                                    rotation = r.getOverlayRotation(z, convert(xi), convert(yi)).toInt()
                                    val overlayDefinition = findOverlay(overlayId - 1)
                                    val overlayTexture = overlayDefinition.textureId
                                    var rgb: Int
                                    rgb = if (overlayTexture >= 0) {
                                        textures[overlayTexture]?.field1777 ?: error("Error getting Texure Color")
                                    } else if (overlayDefinition.rgbColor == 0xFF00FF) {
                                        -2
                                    } else {
                                        // randomness added here
                                        val overlayHsl = hsl24to16(
                                            overlayDefinition.hue,
                                            overlayDefinition.saturation,
                                            overlayDefinition.lightness
                                        )
                                        overlayHsl
                                    }
                                    overlayRgb = 0
                                    if (rgb != -2) {
                                        val var0 = adjustHSLListness0(rgb, 96)
                                        overlayRgb = colorPalette[var0]
                                    }
                                    if (overlayDefinition.secondaryRgbColor != -1) {
                                        val hue = overlayDefinition.otherHue
                                        val sat = overlayDefinition.otherSaturation
                                        val olight = overlayDefinition.otherLightness
                                        rgb = hsl24to16(hue, sat, olight)
                                        val var0 = adjustHSLListness0(rgb, 96)
                                        overlayRgb = colorPalette[var0]
                                    }
                                }
                                if (shape == 0) {
                                    val drawY = regionSizeY - 1 - yi
                                    if (underlayRgb != 0) {
                                        drawMapSquare(pixels, xi, drawY, underlayRgb)
                                    }
                                } else if (shape == 1) {
                                    val drawY = regionSizeY - 1 - yi
                                    drawMapSquare(pixels, xi, drawY, overlayRgb)
                                } else {
                                    val drawX = xi * builder.scale
                                    val drawY = (regionSizeY - 1 - yi) * builder.scale
                                    val tileShapes = TILE_SHAPE_2D[shape]
                                    val tileRotations = TILE_ROTATION_2D[rotation]
                                    if (underlayRgb != 0) {
                                        var rotIdx = 0
                                        for (i in 0 until regionSizeZ) {
                                            val p1 =
                                                if (tileShapes[tileRotations[rotIdx++]] == 0) underlayRgb else overlayRgb
                                            val p2 =
                                                if (tileShapes[tileRotations[rotIdx++]] == 0) underlayRgb else overlayRgb
                                            val p3 =
                                                if (tileShapes[tileRotations[rotIdx++]] == 0) underlayRgb else overlayRgb
                                            val p4 =
                                                if (tileShapes[tileRotations[rotIdx++]] == 0) underlayRgb else overlayRgb
                                            pixels[drawX + 0][drawY + i] = p1
                                            pixels[drawX + 1][drawY + i] = p2
                                            pixels[drawX + 2][drawY + i] = p3
                                            pixels[drawX + 3][drawY + i] = p4
                                        }
                                    } else {
                                        var rotIdx = 0
                                        for (i in 0 until regionSizeZ) {
                                            val p1 = tileShapes[tileRotations[rotIdx++]]
                                            val p2 = tileShapes[tileRotations[rotIdx++]]
                                            val p3 = tileShapes[tileRotations[rotIdx++]]
                                            val p4 = tileShapes[tileRotations[rotIdx++]]
                                            if (p1 != 0) {
                                                pixels[drawX + 0][drawY + i] = overlayRgb
                                            }
                                            if (p2 != 0) {
                                                pixels[drawX + 1][drawY + i] = overlayRgb
                                            }
                                            if (p3 != 0) {
                                                pixels[drawX + 2][drawY + i] = overlayRgb
                                            }
                                            if (p4 != 0) {
                                                pixels[drawX + 3][drawY + i] = overlayRgb
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Draw a map on the Image
     * @param  image  BufferImage to write to
     * @param  z Plane to look for objects in
     */
    private fun drawMap(image: BufferedImage, z: Int) {
        regionLoader.getRegions().forEach {
            val baseX = it.baseX
            val baseY = it.baseY
            val drawBaseX = baseX - regionLoader.lowestX.baseX
            val drawBaseY = regionLoader.highestY.baseY - baseY
            drawMap(image, drawBaseX, drawBaseY, z, it)
        }
    }

    /**
     * Draw a game Object on the Image
     * @param  image  BufferImage to write to
     * @param  drawBaseX BaseX of the com.query.cache.map.region.Region
     * @param  drawBaseY BaseY of the com.query.cache.map.region.Region
     * @param  region region to look for objects in
     * @param  z Plane to look for objects in
     */
    private fun drawObjects(image: BufferedImage, drawBaseX: Int, drawBaseY: Int, region: Region, z: Int) {
        val graphics = image.createGraphics()
        for (location in region.getLocations()) {
            val rotation = location.orientation
            val type = location.type
            val localX = location.position.x - region.baseX
            val localY = location.position.y - region.baseY
            val isBridge = (region.getTileSetting(1, localX, localY) and 2) > 0
            if (location.position.z == z + 1) {
                if (!isBridge) {
                    continue
                }
            } else if (location.position.z == z) {
                if (isBridge) {
                    continue
                }
                if ((region.getTileSetting(z, localX, localY) and 24) > 0) {
                    continue
                }
            } else {
                continue
            }
            val obj = findObject(location.id)
            val drawX = (drawBaseX + localX) * builder.scale
            val drawY = (drawBaseY + (regionSizeY - 1 - localY)) * builder.scale
            if (type in 0..3) {
                // this is a wall
                var hash = (localY shl 7) + localX + (location.id shl 14) + 0x40000000
                if (obj.wallOrDoor == 0) {
                    hash -= Int.MIN_VALUE
                }
                var rgb = wallColor
                if (hash > 0) {
                    rgb = doorColor
                }
                if (obj.mapSceneID != -1) {
                    if(builder.drawMapScene) {
                        val spriteImage = scaledMapIcons[obj.mapSceneID]
                        graphics.drawImage(spriteImage, drawX * builder.scale, drawY * builder.scale, null)
                    }
                } else {
                    if (type == 0 || type == 2) {
                        when (rotation) {
                            0 -> {
                                image.setRGB(drawX + 0, drawY + 0, rgb)
                                image.setRGB(drawX + 0, drawY + 1, rgb)
                                image.setRGB(drawX + 0, drawY + 2, rgb)
                                image.setRGB(drawX + 0, drawY + 3, rgb)
                            }
                            1 -> {
                                image.setRGB(drawX + 0, drawY + 0, rgb)
                                image.setRGB(drawX + 1, drawY + 0, rgb)
                                image.setRGB(drawX + 2, drawY + 0, rgb)
                                image.setRGB(drawX + 3, drawY + 0, rgb)
                            }
                            2 -> {
                                image.setRGB(drawX + 3, drawY + 0, rgb)
                                image.setRGB(drawX + 3, drawY + 1, rgb)
                                image.setRGB(drawX + 3, drawY + 2, rgb)
                                image.setRGB(drawX + 3, drawY + 3, rgb)
                            }
                            3 -> {
                                image.setRGB(drawX + 0, drawY + 3, rgb)
                                image.setRGB(drawX + 1, drawY + 3, rgb)
                                image.setRGB(drawX + 2, drawY + 3, rgb)
                                image.setRGB(drawX + 3, drawY + 3, rgb)
                            }
                        }
                    }
                    if (type == 3) {
                        when (rotation) {
                            0 -> image.setRGB(drawX + 0, drawY + 0, rgb)
                            1 -> image.setRGB(drawX + 3, drawY + 0, rgb)
                            2 -> image.setRGB(drawX + 3, drawY + 3, rgb)
                            3 -> image.setRGB(drawX + 0, drawY + 3, rgb)
                        }
                    }
                    if (type == 2) {
                        when (rotation) {
                            3 -> {
                                image.setRGB(drawX + 0, drawY + 0, rgb)
                                image.setRGB(drawX + 0, drawY + 1, rgb)
                                image.setRGB(drawX + 0, drawY + 2, rgb)
                                image.setRGB(drawX + 0, drawY + 3, rgb)
                            }
                            0 -> {
                                image.setRGB(drawX + 0, drawY + 0, rgb)
                                image.setRGB(drawX + 1, drawY + 0, rgb)
                                image.setRGB(drawX + 2, drawY + 0, rgb)
                                image.setRGB(drawX + 3, drawY + 0, rgb)
                            }
                            1 -> {
                                image.setRGB(drawX + 3, drawY + 0, rgb)
                                image.setRGB(drawX + 3, drawY + 1, rgb)
                                image.setRGB(drawX + 3, drawY + 2, rgb)
                                image.setRGB(drawX + 3, drawY + 3, rgb)
                            }
                            2 -> {
                                image.setRGB(drawX + 0, drawY + 3, rgb)
                                image.setRGB(drawX + 1, drawY + 3, rgb)
                                image.setRGB(drawX + 2, drawY + 3, rgb)
                                image.setRGB(drawX + 3, drawY + 3, rgb)
                            }
                        }
                    }
                }
            } else if (type == 9) {
                if (obj.mapSceneID != -1) {
                    if(builder.drawMapScene) {
                        val spriteImage = scaledMapIcons[obj.mapSceneID]
                        graphics.drawImage(spriteImage, drawX, drawY, null)
                    }
                    continue
                }
                var hash = (localY shl 7) + localX + (location.id shl 14) + 0x40000000
                if (obj.wallOrDoor == 0) {
                    hash -= Int.MIN_VALUE
                }
                if (hash shr 29 and 3 != 2) {
                    continue
                }
                var rgb = 0xEEEEEE
                if (hash > 0) {
                    rgb = 0xEE0000
                }
                if (rotation != 0 && rotation != 2) {
                    image.setRGB(drawX + 0, drawY + 0, rgb)
                    image.setRGB(drawX + 1, drawY + 1, rgb)
                    image.setRGB(drawX + 2, drawY + 2, rgb)
                    image.setRGB(drawX + 3, drawY + 3, rgb)
                } else {
                    image.setRGB(drawX + 0, drawY + 3, rgb)
                    image.setRGB(drawX + 1, drawY + 2, rgb)
                    image.setRGB(drawX + 2, drawY + 1, rgb)
                    image.setRGB(drawX + 3, drawY + 0, rgb)
                }
            } else if (type == 22 || type in 9..11) {
                // ground object
                if (obj.mapSceneID != -1) {
                    if (builder.drawMapScene) {
                        val spriteImage = scaledMapIcons[obj.mapSceneID]
                        graphics.drawImage(spriteImage, drawX, drawY, null)
                    }
                }
            }
        }
        graphics.dispose()
    }

    /**
     * Draw a game Object on the Image
     * @param  image  BufferImage to write to
     * @param  z Plane to look for objects in
     */
    private fun drawObjects(image: BufferedImage, z: Int) {
        regionLoader.getRegions().forEach {
            val baseX = it.baseX
            val baseY = it.baseY

            val drawBaseX = baseX - regionLoader.lowestX.baseX

            val drawBaseY = regionLoader.highestY.baseY - baseY
            drawObjects(image, drawBaseX, drawBaseY, it, z)
        }
    }

    private fun renderMapIcons(image: BufferedImage, drawBaseX: Int, drawBaseY: Int, region: Region, z: Int) {
        val baseX = region.baseX
        val baseY = region.baseY
        val graphics = image.createGraphics()
        if (builder.drawFunctions) {
            drawMapFunctions(graphics, region, z, drawBaseX, drawBaseY)
        }
        if (builder.labelRegions) {
            graphics.color = Color.WHITE
            val str = baseX.toString() + "," + baseY + " (" + region.regionX + "," + region.regionY + ")"
            graphics.drawString(str, drawBaseX * builder.scale, drawBaseY * builder.scale + graphics.fontMetrics.height)
        }
        if (builder.outlineRegions) {
            graphics.color = Color.WHITE
            graphics.drawRect(drawBaseX * builder.scale, drawBaseY * builder.scale, regionSizeX * builder.scale, regionSizeY * builder.scale)
        }
        graphics.dispose()
    }

    private fun drawMapIcons(image: BufferedImage, z: Int) {
        // map icons
        regionLoader.getRegions().forEach {
            val baseX = it.baseX
            val baseY = it.baseY

            val drawBaseX = baseX - regionLoader.lowestX.baseX

            val drawBaseY = regionLoader.highestY.baseY - baseY
            renderMapIcons(image, drawBaseX, drawBaseY, it, z)
        }

    }

    private fun hsl24to16(hue: Int, saturation: Int, lightness: Int): Int {
        var sat = saturation
        if (lightness > 179) {
            sat /= 2
        }
        if (lightness > 192) {
            sat /= 2
        }
        if (lightness > 217) {
            sat /= 2
        }
        if (lightness > 243) {
            sat /= 2
        }
        return (sat / 32 shl 7) + (hue / 4 shl 10) + lightness / 2
    }

    private fun drawMapSquare(pixels: Array<IntArray>, x: Int, y: Int, rgb: Int) {
        var height = x
        var width = y
        height *= builder.scale
        width *= builder.scale
        for (i in 0 until builder.scale) {
            for (j in 0 until builder.scale) {
                pixels[height + i][width + j] = rgb
            }
        }
    }

    private fun drawMapFunctions(graphics: Graphics2D, region: Region, z: Int, drawBaseX: Int, drawBaseY: Int) {
        for (location in region.getLocations()) {
            val localZ = location.position.z
            if (localZ != z) {
                // draw all icons on z=0
                continue
            }
            val od = findObject(location.id)
            val localX = location.position.x - region.baseX
            val localY = location.position.y - region.baseY
            val drawX = drawBaseX + localX
            val drawY = drawBaseY + (regionSizeY - 1 - localY)
            if (od.mapAreaId != -1) {
                graphics.drawImage(findMapIcon(od.mapAreaId), drawX * builder.scale, drawY * builder.scale, null)
            }
        }
    }

    private fun findObject(id: Int) = objects[id]?: error("Could not find Object")

    private fun findUnderlay(id: Int) = underlays[id]?: error("Could not find Underlay")

    private fun findOverlay(id: Int) = overlays[id]?: error("Could not find Overlay")

    private fun findArea(id: Int) = areas[id]?: error("Could not find Area")

    private fun findSprite(id: Int) = sprites[id]?: error("Could not find Sprite")

    private fun findMapIcon(id: Int) = if(revisionBefore(142)) pre142MapFunction(id) else findSprite(findArea(id).spriteId).sprite.toBufferImage()

    private fun pre142MapFunction(id : Int) : BufferedImage {
        val container: ByteArray = Constants.library.data(IndexType.SPRITES.number, 318)!!
        val sprite = SpriteData.decode(ByteBuffer.wrap(container))
        return sprite.getFrame(id)
    }

    private fun resizeMapScene() {
        if (!builder.drawMapScene) return
        MapScene.collectSprites().forEach {
            scaledMapIcons[it.key] = Sprite.resize(it.value, MAPICON_MAX_WIDTH, MAPICON_MAX_HEIGHT)
        }
    }

    companion object {
        
        private const val MAPICON_MAX_WIDTH = 5 // scale minimap icons down to this size so they fit..
        private const val MAPICON_MAX_HEIGHT = 6
        private const val BLEND = 5 // number of surrounding tiles for ground blending
        private val colorPalette = ColorPalette(0.9, 0, 512).colorPalette
        private val TILE_SHAPE_2D = arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1),
            intArrayOf(1, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0),
            intArrayOf(0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1),
            intArrayOf(0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0),
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1),
            intArrayOf(1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1)
        )
        private val TILE_ROTATION_2D = arrayOf(
            intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
            intArrayOf(12, 8, 4, 0, 13, 9, 5, 1, 14, 10, 6, 2, 15, 11, 7, 3),
            intArrayOf(15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0),
            intArrayOf(3, 7, 11, 15, 2, 6, 10, 14, 1, 5, 9, 13, 0, 4, 8, 12)
        )

        private fun convert(d: Int): Int {
            return if (d >= 0) {
                d % 64
            } else {
                64 - -(d % 64) - 1
            }
        }

        fun method1792(var0: Int, var1: Int): Int {
            var var1 = var1
            return if (var0 == -1) {
                12345678
            } else {
                var1 = (var0 and 127) * var1 / 128
                if (var1 < 2) {
                    var1 = 2
                } else if (var1 > 126) {
                    var1 = 126
                }
                (var0 and 65408) + var1
            }
        }

        fun adjustHSLListness0(var0: Int, var1: Int): Int {
            var var1 = var1
            return if (var0 == -2) {
                12345678
            } else if (var0 == -1) {
                if (var1 < 2) {
                    var1 = 2
                } else if (var1 > 126) {
                    var1 = 126
                }
                var1
            } else {
                var1 = (var0 and 127) * var1 / 128
                if (var1 < 2) {
                    var1 = 2
                } else if (var1 > 126) {
                    var1 = 126
                }
                (var0 and 65408) + var1
            }
        }
    }
}