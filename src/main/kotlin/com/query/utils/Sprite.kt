package com.query.utils

import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


data class Sprite(
    val id : Int,
    val frame : Int = 0,
    val maxWidth : Int = 0,
    val maxHeight : Int = 0,
    var width : Int = 0,
    var height : Int = 0,
    var offsetX : Int = 0,
    var offsetY : Int = 0,
    var pixelIdx : ByteArray = ByteArray(0),
    var palette : IntArray= IntArray(0),
    var pixels : IntArray = IntArray(0)
) {


    companion object {



        fun resize(bufferedImage: BufferedImage,width: Int, height: Int): BufferedImage {
            val tmp = bufferedImage.getScaledInstance(width, height, 0)
            val dimg = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val g2d = dimg.createGraphics()
            g2d.drawImage(tmp, 0, 0, null)
            g2d.dispose()
            return dimg
        }

    }

    fun toBufferImage() : BufferedImage {
        val imageTransparent = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        imageTransparent.setRGB(0, 0, width, height, pixels, 0, width)
        return imageTransparent
    }

    fun writeTransparent(file : File) {
        val imageTransparent = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        imageTransparent.setRGB(0, 0, width, height, pixels, 0, width)
        ImageIO.write(imageTransparent, "png", file)
    }

    fun writePink(file : File) {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        img.setRGB(0, 0, width, height, pixels, 0, width)

        val source = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_RGB)
        val g2d = source.createGraphics()
        g2d.color = Color(255, 0, 255, 255)
        g2d.fillRect(0, 0, source.width, source.height)
        g2d.dispose()

        val target = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB)
        val g = target.graphics as Graphics2D
        g.drawImage(source, 0, 0, null)
        g.drawImage(img, 0, 0, null)

        ImageIO.write(target, "png", file)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sprite

        if (id != other.id) return false
        if (frame != other.frame) return false
        if (maxWidth != other.maxWidth) return false
        if (maxHeight != other.maxHeight) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (offsetX != other.offsetX) return false
        if (offsetY != other.offsetY) return false
        if (!pixelIdx.contentEquals(other.pixelIdx)) return false
        if (!palette.contentEquals(other.palette)) return false
        if (!pixels.contentEquals(other.pixels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + frame
        result = 31 * result + maxWidth
        result = 31 * result + maxHeight
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + offsetX
        result = 31 * result + offsetY
        result = 31 * result + pixelIdx.contentHashCode()
        result = 31 * result + palette.contentHashCode()
        result = 31 * result + pixels.contentHashCode()
        return result
    }


}