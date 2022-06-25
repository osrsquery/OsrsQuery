package com.query.utils.image

import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.awt.image.Raster
import java.io.File
import java.util.concurrent.Callable
import javax.imageio.ImageIO

class ImagePartLoader(private val y: Int, width: Int, height: Int, private val file: File, private val image: BufferedImage) :
    Callable<ImagePartLoader> {
    private val region: Rectangle

    init {
        region = Rectangle(0, y, width, height)
    }

    @Throws(Exception::class)
    override fun call(): ImagePartLoader {
        Thread.currentThread().priority = (Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2
        ImageIO.createImageInputStream(file).use { stream ->
            val readers = ImageIO.getImageReaders(stream)
            if (readers.hasNext()) {
                val reader = readers.next()
                reader.setInput(stream, true, true)
                val param = reader.defaultReadParam
                param.sourceRegion = region
                val part = reader.read(0, param)
                val source: Raster = part.raster
                val target = image.raster
                target.setRect(0, y, source)
            }
        }
        return this@ImagePartLoader
    }
}