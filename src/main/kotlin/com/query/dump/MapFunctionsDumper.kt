package com.query.dump

import com.query.cache.SpriteDecoder
import com.query.Application.areas
import com.query.Application.objects
import com.query.Constants
import com.query.cache.definitions.impl.AreaProvider
import com.query.cache.definitions.impl.ObjectProvider
import com.query.utils.FileUtil.getFile
import com.query.utils.IndexType
import com.query.utils.progress
import com.query.utils.revisionIsOrBefore
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.ByteBuffer
import javax.imageio.ImageIO


object MapFunctionsDumper {

    fun init() {
        if (areas().isEmpty()) {
            SpriteDumper().init()
            AreaProvider(null).run()
            ObjectProvider(null).run()
        }
        writeImage()
    }

    private fun writeImage() {
        val functions = getMapFunctions()
        val progress = progress("Writing Area Sprites", functions.size.toLong())
        var index = 0
        functions.forEach {
            ImageIO.write(it.value, "png", getFile("mapFunctions/","${if(revisionIsOrBefore(142)) it.key else index}.png"))
            progress.step()
            index++
        }
        progress.close()
    }

    fun getMapFunctions() : Map<Int,BufferedImage> {
        val data : MutableMap<Int,BufferedImage> = emptyMap<Int, BufferedImage>().toMutableMap()
        if (revisionIsOrBefore(142)) {
            val functions = objects().filter { it.mapAreaId != -1 }
            functions.filter { it.mapAreaId != -1 }.forEachIndexed { index, area ->
                val container: ByteArray = Constants.library.data(IndexType.SPRITES.number, 318)!!
                val sprite = SpriteDecoder.decode(ByteBuffer.wrap(container))
                val img = sprite.getFrame(area.mapAreaId)

                val background = BufferedImage(15, 15, BufferedImage.TYPE_INT_RGB)
                val g2d = background.createGraphics()
                g2d.color = Color.decode("#FF00FF")
                g2d.fillRect(0, 0, background.width, background.height)
                g2d.drawImage(img, 0, 0, null)
                g2d.dispose()

                data[area.mapAreaId] = background
            }
        } else {
            val functions = areas().filter { it.spriteId != -1 }
            functions.filter { it.spriteId != -1 }.forEachIndexed { index, area ->
                val pink = getFile("sprites/pink/", "${area.spriteId}.png")
                var buffImg = BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB)
                try {
                    buffImg = ImageIO.read(pink)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                data[index] = buffImg
            }
        }
        return data
    }

}