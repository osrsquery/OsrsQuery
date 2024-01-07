package com.query.map.draw

import com.query.ApplicationSettings
import com.query.Pipeline
import com.query.cache.definition.data.MapObject
import com.query.cache.definition.data.MapSceneDefinition
import com.query.cache.definition.data.ObjectDefinitionFull
import com.query.cache.definition.data.SpriteDefinition
import com.query.currentCache
import com.query.map.load.MapTileSettings
import com.query.map.load.RegionManager
import com.query.map.render.WorldMapDumper
import com.query.types.Region
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class RegionRenderer(
    private val manager: RegionManager,
    private val objectDecoder: Array<ObjectDefinitionFull>,
    private val spriteDecoder: Array<SpriteDefinition>,
    private val mapSceneDecoder: Array<MapSceneDefinition>,
    private val loader: MinimapIconPainter,
    private val settings: MapTileSettings
) : Pipeline.Modifier<Region> {

    override fun process(content: Region) {
        val start = System.currentTimeMillis()
        val regionX = content.x - 1
        val regionY = content.y - 1

        manager.loadTiles(regionX, regionY)
        settings.set(regionX, regionY)
        val objects = mutableMapOf<Int, List<MapObject>?>()
        for (rX in content.x - 1..content.x + 1) {
            for (rY in content.y - 1..content.y + 1) {
                val region = Region(rX, rY)
                objects[region.id] = manager.loadObjects(region)
            }
        }

        for (level in 0 until 4) {
            val img = manager.renderRegion(settings, level)

            val overlay = BufferedImage(manager.width * manager.scale, manager.height * manager.scale, BufferedImage.TYPE_INT_ARGB)
            val o = overlay.graphics as Graphics2D

            val painter = ObjectPainter(objectDecoder, spriteDecoder, mapSceneDecoder)
            painter.level = level
            painter.paint(o, Region(content.x, content.y), objects)

            if (WorldMapDumper.minimapIcons) {
                loader.paint(o, content, level, objects)
            }
            val g = img.graphics
            g.drawImage(overlay, 0, 1 + overlay.height, overlay.width, -overlay.height, null)

            try {
                val image = img.getSubimage(256, 257, 256, 256)

                if (isNotBlank(image)) {
                    ImageIO.write(image, "png", File(ApplicationSettings.findDirectory(currentCache!!,"map/images/$level/${content.id}.png").absolutePath))
                } else {
                    ImageIO.write(missingXtea(content.id), "png", File(ApplicationSettings.findDirectory(currentCache!!,"map/images/$level/${content.id}.png").absolutePath))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun missingXtea(region : Int) : BufferedImage {
        val width = 256 // Width of the image
        val height = 256 // Height of the image

        // Create a BufferedImage with the specified width and height
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        // Get the Graphics2D object to draw on the image
        val graphics = image.createGraphics()

        // Set background color (optional)
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, width, height)

        // Set text properties
        val font = Font("Arial", Font.BOLD, 24)
        graphics.font = font
        graphics.color = Color.BLACK

        // Text to be written on the image
        val text = "Missing: ${region}"

        // Calculate text position to center it
        val fm = graphics.fontMetrics
        val textWidth = fm.stringWidth(text)
        val textHeight = fm.height
        val x = (width - textWidth) / 2
        val y = (height + textHeight) / 2

        // Draw the text on the image
        graphics.drawString(text, x, y)

        // Dispose of the graphics object
        graphics.dispose()
        return image;
    }

    companion object {
        private fun isNotBlank(img: BufferedImage): Boolean {
            loop@ for (x in 0 until img.width) {
                for (y in 0 until img.height) {
                    if (img.getRGB(x, y) != 0) {
                        return true
                    }
                }
            }
            return false
        }
    }
}