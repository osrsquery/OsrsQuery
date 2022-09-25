
package com.query.game.map

import com.query.Application
import com.query.cache.SpriteDecoder
import com.query.Constants
import com.query.cache.definitions.impl.*
import com.query.game.map.builders.MapImageBuilder
import com.query.game.map.region.Region
import com.query.game.map.region.RegionLoader
import com.query.dump.MapSceneDumper
import com.query.utils.*
import mu.KotlinLogging
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import com.query.game.map.region.data.Location
import com.query.game.map.region.regionSizeX
import com.query.game.map.region.regionSizeY
import com.query.utils.image.BigBufferedImage
import java.awt.RenderingHints
import java.io.File
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

class MapImageGenerator(
    private val builder : MapImageBuilder,
    val saveLocation : File = FileUtil.getDir("mapImages/")
) {

    private val flags: MutableList<Int> = ArrayList()

    var regionLoader: RegionLoader = RegionLoader()
    var objects: Map<Int, ObjectDefinition> = Application.objects().associateBy { it.id }
    var overlays: Map<Int, OverlayDefinition> = Application.overlays().associateBy { it.id }
    var underlays: Map<Int, UnderlayDefinition> = Application.underlays().associateBy { it.id }
    var areas: Map<Int, AreaDefinition> = Application.areas().associateBy { it.id }
    var textures: Map<Int, TextureDefinition> = Application.textures().associateBy { it.id }
    var sprites: Map<Int, SpriteDefinition> = Application.sprites().associateBy { it.id }
    private val scaledMapIcons: MutableMap<Int, Image> = HashMap()

    init {
        resizeMapScene()
    }

    /**
     * Main Method to start the Map drawing
     * @return The Full map Image
     */
    fun draw() {
        val minX = regionLoader.lowestX.baseX
        val minY = regionLoader.lowestY.baseY
        val maxX: Int = regionLoader.highestX.baseX + regionSizeX
        val maxY: Int = regionLoader.highestY.baseY + regionSizeY
        var dimX = maxX - minX
        var dimY = maxY - minY
        val boundX = dimX - 1
        val boundY = dimY - 1
        dimX *= builder.scale
        dimY *= builder.scale

        logger.info {
            "====== Setting Drawing Map Image  =====\n" +
                    "Options: ${builder}\n" +
                    "Image Size: $dimX px x $dimY px\n" +
                    "Size: ${dimX * dimY * 3 / 1024 / 1024} MB\n" +
                    "Memory: ${Runtime.getRuntime().maxMemory() / 1024L / 1024L}mb\n" +
                    "North most region: ${regionLoader.lowestX.baseX}\n" +
                    "South most region: ${regionLoader.highestY.baseY}\n" +
                    "West most region: ${regionLoader.lowestX.baseX}\n" +
                    "East most region: ${regionLoader.highestY.baseY}\n" +
            "====== Starting Drawing Map Image =====\n"
        }


        for (plane in PLANE_MIN..PLANE_MAX) {
            logger.info { "Generating map images for plane = $plane" }
            val baseImage: BufferedImage = BigBufferedImage.create(dimX, dimY, BufferedImage.TYPE_INT_RGB)
            val fullImage: BufferedImage = BigBufferedImage.create(dimX, dimY, BufferedImage.TYPE_INT_RGB)
            val graphics = fullImage.createGraphics()
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            logger.info { "Adding Underlay" }
            drawUnderlay(plane, baseImage)
            logger.info { "Blending Underlay" }
            blendUnderlay(baseImage, fullImage, boundX, boundY)
            logger.info { "Drawing Overlay" }
            drawOverlay(plane, fullImage)
            logger.info { "Drawing Objects" }
            drawLocations(plane, graphics)
            if (builder.walls) {
                logger.info { "Drawing Walls" }
                drawWalls(plane, graphics)
            }
            if (builder.drawFunctions) {
                logger.info {"Adding Map Icons / Labels" }
                drawIcons(plane, graphics)
            }

            drawRegions(graphics)
            graphics.dispose()

            ImageIO.write(fullImage, "png", File(saveLocation, "map-${plane}.png"))
        }
    }

    /**
     * Draws the map Underlay
     * @param plane Plane of the Overlay to be drawn
     */
    private fun drawUnderlay(plane: Int, image: BufferedImage) {
        regionLoader.getRegions().forEach {
            val drawBaseX = it.baseX - regionLoader.lowestX.baseX
            val drawBaseY = regionLoader.highestY.baseY - it.baseY
            for (x in 0 until regionSizeX) {
                val drawX = drawBaseX + x
                for (y in 0 until regionSizeY) {
                    val drawY: Int = drawBaseY + (regionSizeY - 1 - y)
                    val underlayId = it.getUnderlayId(plane, x, y) - 1
                    var rgb = Color.CYAN.rgb
                    if (underlayId > -1) {
                        val underlay = findUnderlay(underlayId)
                        rgb = underlay.color
                    }
                    drawMapSquare(image, drawX, drawY, rgb, -1, -1)
                }
            }
        }

    }

    /**
     * Blends the Underlay
     * @param baseImage Base Image Containing the Underlay
     * @param fullImage Plane of the Overlay to be drawn
     * @param boundX Top Left of the Image
     * @param boundY Bottom Right of the Image
     */
    private fun blendUnderlay(baseImage: BufferedImage, fullImage: BufferedImage, boundX: Int, boundY: Int) {
        regionLoader.getRegions().forEach {
            val drawBaseX = it.baseX - regionLoader.lowestX.baseX
            val drawBaseY = regionLoader.highestY.baseY - it.baseY
            for (x in 0 until regionSizeX) {
                val drawX = drawBaseX + x
                for (y in 0 until regionSizeY) {
                    val drawY: Int = drawBaseY + (regionSizeY - 1 - y)
                    var c = getMapSquare(baseImage, drawX, drawY)
                    if (c == Color.CYAN) {
                        continue
                    }
                    var tRed = 0
                    var tGreen = 0
                    var tBlue = 0
                    var count = 0
                    val maxDY = boundY.coerceAtMost(drawY + 3)
                    val maxDX = boundX.coerceAtMost(drawX + 3)
                    val minDY = 0.coerceAtLeast(drawY - 3)
                    val minDX = 0.coerceAtLeast(drawX - 3)
                    for (dy in minDY until maxDY) {
                        for (dx in minDX until maxDX) {
                            c = getMapSquare(baseImage, dx, dy)
                            if (c == Color.CYAN) {
                                continue
                            }
                            tRed += c.red
                            tGreen += c.green
                            tBlue += c.blue
                            count++
                        }
                    }
                    if (count > 0) {
                        c = Color(tRed / count, tGreen / count, tBlue / count)
                        drawMapSquare(fullImage, drawX, drawY, c.rgb, -1, -1)
                    }
                }
            }
        }
    }

    /**
     * Draws the map Overlay
     * @param plane Plane of the Overlay to be drawn
     * @param image Draws the Map Overlay onto the image
     */
    private fun drawOverlay(plane: Int, image: BufferedImage) {
        regionLoader.getRegions().forEach {
            val drawBaseX = it.baseX - regionLoader.lowestX.baseX
            val drawBaseY = regionLoader.highestY.baseY - it.baseY
            for (x in 0 until regionSizeX) {
                val drawX = drawBaseX + x
                for (y in 0 until regionSizeY) {
                    val drawY: Int = drawBaseY + (regionSizeY - 1 - y)
                    if (plane == 0 || !it.isLinkedBelow(plane, x, y) && !it.isVisibleBelow(plane, x, y)) {
                        val overlayId = it.getOverlayId(plane, x, y) - 1
                        if (overlayId > -1) {
                            val rgb = getOverlayColor(overlayId)
                            drawMapSquare(
                                image,
                                drawX,
                                drawY,
                                rgb,
                                it.getOverlayPath(plane, x, y).toInt(),
                                it.getOverlayRotation(plane, x, y).toInt()
                            )
                        }
                    }
                    if (plane < 3 && (it.isLinkedBelow(plane + 1, x, y) || it.isVisibleBelow(plane + 1, x, y))) {
                        val overlayAboveId = it.getOverlayId(plane + 1, x, y) - 1
                        if (overlayAboveId > -1) {
                            val rgb = getOverlayColor(overlayAboveId)
                            drawMapSquare(
                                image,
                                drawX,
                                drawY,
                                rgb,
                                it.getOverlayPath(plane + 1, x, y).toInt(),
                                it.getOverlayRotation(plane + 1, x, y).toInt()
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the Overlay tile c
     * @param overlayID OverlayID of the tile
     * @return rgb Tile Overlay Color
     */
    private fun getOverlayColor(overlayID: Int): Int {
        val overlay = findOverlay(overlayID)
        var rgb = if (overlay.textureId >= 0) {
            textures[overlay.textureId]?.averageRGB ?: error("Error getting Texure Color")
        } else if (overlay.rgbColor == 0xFF00FF) {
            -2
        } else {
            val overlayHsl = hsl24to16(
                overlay.hue,
                overlay.saturation,
                overlay.lightness
            )
            overlayHsl
        }

        var overlayRgb = 0
        if (rgb != -2) {
            val var0 = adjustHSLListness0(rgb, 96)
            overlayRgb = colorPalette[var0]
        }
        if (overlay.secondaryRgbColor != -1) {
            val hue = overlay.otherHue
            val sat = overlay.otherSaturation
            val olight = overlay.otherLightness
            rgb = hsl24to16(hue, sat, olight)
            val var0 = adjustHSLListness0(rgb, 96)
            overlayRgb = colorPalette[var0]
        }

        return overlayRgb
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

    /**
     * Draw map Scene
     * @param plane Plane of the Object Icon to be drawn
     * @param graphics Graphics Object to draw to
     */
    private fun drawLocations(plane: Int, graphics: Graphics2D) {
        regionLoader.getRegions().forEach {
            val drawBaseX = it.baseX - regionLoader.lowestX.baseX
            val drawBaseY = regionLoader.highestY.baseY - it.baseY
            for (location in it.getLocations()) {
                val localX = location.position.x - it.baseX
                val localY = location.position.y - it.baseY
                if (!canDrawLocation(it, location, plane, localX, localY)) {
                    continue
                }
                val objType = findObject(location.id)
                val drawX = drawBaseX + localX
                val drawY: Int = drawBaseY + (regionSizeY - 1 - localY)
                if (objType.mapSceneID != -1) {
                    val spriteImage = scaledMapIcons[objType.mapSceneID]
                    graphics.drawImage(spriteImage, drawX * builder.scale, drawY * builder.scale, null)
                }
            }
        }

    }

    /**
     * Draw Walls on the map image
     * @param plane Plane of the Object Icon to be drawn
     * @param graphics Graphics Object to draw to
     */
    private fun drawWalls(plane: Int, graphics: Graphics2D) {
        regionLoader.getRegions().forEach {
            val drawBaseX = it.baseX - regionLoader.lowestX.baseX
            val drawBaseY = regionLoader.highestY.baseY - it.baseY
            for (location in it.getLocations()) {
                graphics.color = Color.WHITE
                val localX = location.position.x - it.baseX
                val localY = location.position.y - it.baseY
                if (!canDrawLocation(it, location, plane, localX, localY)) {
                    continue
                }
                val objType = findObject(location.id)

                // Don't draw walls on water
                if (objType.mapSceneID == 22) {
                    continue
                }
                val objName: String = objType.name.lowercase()
                if (objName.contains("door") || objName.contains("gate")) {
                    graphics.color = Color.RED
                }
                var drawX = drawBaseX + localX
                var drawY: Int = drawBaseY + (regionSizeY - 1 - localY)
                drawX *= builder.scale
                drawY *= builder.scale
                if (location.type == 0) { // Straight walls
                    when (location.orientation) {
                        0 -> graphics.drawLine(drawX, drawY, drawX, drawY + builder.scale)
                        1 -> graphics.drawLine(drawX, drawY, drawX + builder.scale, drawY)
                        2 -> graphics.drawLine(drawX + builder.scale, drawY, drawX + builder.scale, drawY + builder.scale)
                        3 -> graphics.drawLine(drawX, drawY + builder.scale, drawX + builder.scale, drawY + builder.scale)
                    }
                } else if (location.type == 2) { // Corner walls
                    when (location.orientation) {
                        0 -> { // West & South
                            graphics.drawLine(drawX, drawY, drawX, drawY + builder.scale)
                            graphics.drawLine(drawX, drawY, drawX + builder.scale, drawY)
                        }
                        1 -> { // South & East
                            graphics.drawLine(drawX, drawY, drawX + builder.scale, drawY)
                            graphics.drawLine(drawX + builder.scale, drawY, drawX + builder.scale, drawY + builder.scale)
                        }
                        2 -> { // East & North
                            graphics.drawLine(drawX + builder.scale, drawY, drawX + builder.scale, drawY + builder.scale)
                            graphics.drawLine(drawX, drawY + builder.scale, drawX + builder.scale, drawY + builder.scale)
                        }
                        3 -> { // North & West
                            graphics.drawLine(drawX, drawY + builder.scale, drawX + builder.scale, drawY + builder.scale)
                            graphics.drawLine(drawX, drawY, drawX, drawY + builder.scale)
                        }
                    }
                } else if (location.type == 3) { // Single points
                    when (location.orientation) {
                        0 -> graphics.drawLine(drawX, drawY + 1, drawX, drawY + 1)
                        1 -> graphics.drawLine(drawX + 3, drawY + 1, drawX + 3, drawY + 1)
                        2 -> graphics.drawLine(drawX + 3, drawY + 4, drawX + 3, drawY + 4)
                        3 -> graphics.drawLine(drawX, drawY + 3, drawX, drawY + 3)
                    }
                } else if (location.type == 9) { // Diagonal walls
                    if (location.orientation == 0 || location.orientation == 2) { // West or East
                        graphics.drawLine(drawX, drawY + builder.scale, drawX + builder.scale, drawY)
                    } else if (location.orientation == 1 || location.orientation == 3) { // South or South
                        graphics.drawLine(drawX, drawY, drawX + builder.scale, drawY + builder.scale)
                    }
                }
            }
        }
    }



    /**
     * Draw Map Functions on the map image
     * @param plane Plane of the Object Icon to be drawn
     * @param graphics Graphics Object to draw to
     */
    private fun drawIcons(plane: Int, graphics: Graphics2D) {
        regionLoader.getRegions().forEach {
            val drawBaseX = it.baseX - regionLoader.lowestX.baseX
            val drawBaseY = regionLoader.highestY.baseY - it.baseY
            for (location in it.getLocations()) {
                val localX = location.position.x - it.baseX
                val localY = location.position.y - it.baseY
                if (!canDrawLocation(it, location, plane, localX, localY)) {
                    continue
                }
                val objType = findObject(location.id)
                val drawX = drawBaseX + localX
                val drawY = drawBaseY + (63 - localY)
                if (objType.mapAreaId != -1) {
                    graphics.drawImage(findMapIcon(objType.mapAreaId), (drawX - 1) * builder.scale, (drawY - 1) * builder.scale, null)
                }
            }
        }
    }

    /**
     * Check if the map can draw this location on the map
     * @param region Region to look for Objects in
     * @param location Locations to Check
     * @param plane Planes that the check is valid on
     * @param x X that the check is valid on
     * @param y Y that the check is valid on
     * @return Can Draw
     */
    private fun canDrawLocation(region: Region, location: Location, plane: Int, x: Int, y: Int): Boolean {
        if (region.isLinkedBelow(plane, x, y) || region.isVisibleBelow(plane, x, y)) {
            return false
        }
        return if (location.position.z == plane + 1
            && (region.isLinkedBelow(plane + 1, x, y)
                    || region.isVisibleBelow(plane + 1, x, y))
        ) {
            true
        } else plane == location.position.z
    }

    /**
     * Draw Region onto the map
     * @param graphics Draw Region onto the Graphics Object
     */
    private fun drawRegions(graphics: Graphics2D) {
        regionLoader.getRegions().forEach {
            val baseX = it.baseX
            val baseY = it.baseY
            val drawBaseX = baseX - regionLoader.lowestX.baseX
            val drawBaseY = regionLoader.highestY.baseY - baseY

            if (builder.labelRegions) {
                graphics.color = Color.WHITE
                val str = baseX.toString() + "," + baseY + " (" + it.regionX + "," + it.regionY + ")"
                graphics.drawString(str, drawBaseX * builder.scale + 1, drawBaseY * builder.scale + graphics.fontMetrics.height)
            }
            if (builder.outlineRegions) {
                graphics.color = Color.WHITE
                graphics.drawRect(drawBaseX * builder.scale, drawBaseY * builder.scale, regionSizeX * builder.scale, regionSizeY * builder.scale)
            }

            if (builder.fill) {
                if (flags.contains(it.regionID)) {
                    graphics.color = Color(255, 0, 0, 80)
                    graphics.fillRect(
                        drawBaseX * builder.scale,
                        drawBaseY * builder.scale,
                        64 * builder.scale,
                        64 * builder.scale
                    )
                }
            }
        }

    }

    /**
     * Draw Map Square onto the base Image
     * @param image Base Image
     * @param x X Location
     * @param y Y Color
     * @param overlayRGB Overlay Color for the map
     * @param shape Shape of the Tile
     * @param rotation Rotation of the Tile
     */
    private fun drawMapSquare(image: BufferedImage, x: Int, y: Int, overlayRGB: Int, shape: Int, rotation: Int) {
        if (shape > -1) {
            val shapeMatrix = TILE_SHAPES[shape]
            val rotationMatrix = TILE_ROTATIONS[rotation and 0x3]
            var shapeIndex = 0
            for (tilePixelY in 0 until builder.scale) {
                for (tilePixelX in 0 until builder.scale) {
                    val drawx = x * builder.scale + tilePixelX
                    val drawy = y * builder.scale + tilePixelY
                    if (shapeMatrix[rotationMatrix[shapeIndex++]] != 0) {
                        image.setRGB(drawx, drawy, overlayRGB)
                    }
                }
            }
        } else {
            for (tilePixelY in 0 until builder.scale) {
                for (tilePixelX in 0 until builder.scale) {
                    val drawx = x * builder.scale + tilePixelX
                    val drawy = y * builder.scale + tilePixelY
                    image.setRGB(drawx, drawy, overlayRGB)
                }
            }
        }
    }

    /**
     * Get Map Square Color
     * @param image Base Image
     * @param x X Location
     * @param y Y Location
     * @return Returns Color of the Map Square
     */
    private fun getMapSquare(image: BufferedImage, x: Int, y: Int): Color {
        var baseX = x
        var baseY = y
        baseX *= builder.scale
        baseY *= builder.scale
        return Color(image.getRGB(baseX, baseY))
    }

    private fun findObject(id: Int) = objects[id]?: error("Could not find Object")

    private fun findUnderlay(id: Int) = underlays[id]?: error("Could not find Underlay")

    private fun findOverlay(id: Int) = overlays[id]?: error("Could not find Overlay")

    private fun findArea(id: Int) = areas[id]?: error("Could not find Area")

    private fun findSprite(id: Int) = sprites[id]?: error("Could not find Sprite")

    private fun findMapIcon(id: Int) = if(revisionBefore(142)) pre142MapFunction(id) else findSprite(findArea(id).spriteId).sprite

    private fun pre142MapFunction(id : Int) : BufferedImage {
        val container: ByteArray = Constants.library.data(IndexType.SPRITES.number, 318)!!
        val sprite = SpriteDecoder.decode(ByteBuffer.wrap(container))
        return sprite.getFrame(id)!!
    }

    private fun resizeMapScene() {
        if (!builder.drawMapScene) return
        MapSceneDumper.collectSprites().forEach {
            scaledMapIcons[it.key] = it.value
        }
    }

    companion object {
        private val colorPalette = ColorPalette(0.9, 0, 512).colorPalette
        private val TILE_SHAPES = arrayOf(
            intArrayOf(
                1, 1, 1, 1,
                1, 1, 1, 1,
                1, 1, 1, 1,
                1, 1, 1, 1
            ), intArrayOf(
                1, 0, 0, 0,
                1, 1, 0, 0,
                1, 1, 1, 0,
                1, 1, 1, 1
            ), intArrayOf(
                1, 1, 0, 0,
                1, 1, 0, 0,
                1, 0, 0, 0,
                1, 0, 0, 0
            ), intArrayOf(
                0, 0, 1, 1,
                0, 0, 1, 1,
                0, 0, 0, 1,
                0, 0, 0, 1
            ), intArrayOf(
                0, 1, 1, 1,
                0, 1, 1, 1,
                1, 1, 1, 1,
                1, 1, 1, 1
            ), intArrayOf(
                1, 1, 1, 0,
                1, 1, 1, 0,
                1, 1, 1, 1,
                1, 1, 1, 1
            ), intArrayOf(
                1, 1, 0, 0,
                1, 1, 0, 0,
                1, 1, 0, 0,
                1, 1, 0, 0
            ), intArrayOf(
                0, 0, 0, 0,
                0, 0, 0, 0,
                1, 0, 0, 0,
                1, 1, 0, 0
            ), intArrayOf(
                1, 1, 1, 1,
                1, 1, 1, 1,
                0, 1, 1, 1,
                0, 0, 1, 1
            ), intArrayOf(
                1, 1, 1, 1,
                1, 1, 0, 0,
                1, 0, 0, 0,
                1, 0, 0, 0
            ), intArrayOf(
                0, 0, 0, 0,
                0, 0, 1, 1,
                0, 1, 1, 1,
                0, 1, 1, 1
            ), intArrayOf(
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 1, 1, 0,
                1, 1, 1, 1
            )
        )
        private val TILE_ROTATIONS = arrayOf(
            intArrayOf(
                0, 1, 2, 3,
                4, 5, 6, 7,
                8, 9, 10, 11,
                12, 13, 14, 15
            ), intArrayOf(
                12, 8, 4, 0,
                13, 9, 5, 1,
                14, 10, 6, 2,
                15, 11, 7, 3
            ), intArrayOf(
                15, 14, 13, 12,
                11, 10, 9, 8,
                7, 6, 5, 4,
                3, 2, 1, 0
            ), intArrayOf(
                3, 7, 11, 15,
                2, 6, 10, 14,
                1, 5, 9, 13,
                0, 4, 8, 12
            )
        )

        private const val PLANE_MIN = 0
        private const val PLANE_MAX = 3

    }

}