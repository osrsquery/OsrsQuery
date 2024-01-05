package com.query.utils

import com.query.Application
import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO

object ImageUtils {

    fun loadIconImage(filePath: String): BufferedImage? {
        try {
            val imageUrl: URL? = Application::class.java.getResource(filePath)
            return ImageIO.read(imageUrl)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}