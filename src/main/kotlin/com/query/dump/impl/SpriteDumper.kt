package com.query.dump.impl

import com.query.Application
import com.query.Application.sprites
import com.query.Constants
import com.query.dump.DefinitionsTypes
import com.query.dump.TypeManager
import com.query.utils.FileUtils.getFile
import com.query.utils.progress
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO


class SpriteDumper : TypeManager {

    override fun load() {
        writeSprites()
    }

    override val requiredDefs = listOf(DefinitionsTypes.SPRITES)

    override fun onTest() {
        writeSprites()
    }

    private fun writeSprites() {
        val progress = progress("Writing Sprites", sprites().size.toLong())
        sprites().forEach {
            it.sprite.writeTransparent(getFile("sprites/transparent/", "${it.id}.png"))
            it.sprite.writePink(getFile("sprites/pink/", "${it.id}.png"))
            progress.step()
        }
        Constants.library.data(10,"title.jpg").apply {
            Files.write(getFile("sprites/pink/", "title.jpg").toPath(), this)
            Files.write(getFile("sprites/transparent/", "title.jpg").toPath(),this)
        }
        progress.close()
    }

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            val parser = ArgParser("app")
            val rev by parser.option(ArgType.Int, description = "The revision you wish to dump").default(0)
            parser.parse(args)
            Application.revision = rev

            SpriteDumper().test()
        }

        fun BufferedImage.writeTransparent(file : File) {
            val imageTransparent = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            imageTransparent.setRGB(0, 0, width, height,
                (raster.dataBuffer as DataBufferInt).data, 0, width)
            ImageIO.write(imageTransparent, "png", file)
        }

        fun BufferedImage.writePink(file : File) {
            val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            img.setRGB(0, 0, width, height, (raster.dataBuffer as DataBufferInt).data, 0, width)

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

    }

}