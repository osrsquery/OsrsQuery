package com.query.dump.impl

import com.query.Application
import com.query.Application.overlays
import com.query.cache.definitions.impl.OverlayDefinition
import com.query.dump.DefinitionsTypes
import com.query.dump.TypeManager
import com.query.utils.FileUtils.getFile
import com.query.utils.progress
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO


class Overlay : TypeManager {

    override val requiredDefs = listOf(DefinitionsTypes.OVERLAYS)

    override fun load() {
        writeOverlays()
    }

    override fun onTest() {
        writeOverlays()
    }

    private fun writeOverlays() {
        val progress = progress("Writing Overlays Images", overlays().size.toLong())
        overlays().forEach {
            ImageIO.write(getOverlayPNG(it), "png", getFile("overlays","${it.id}.png"))
            progress.step()
        }
        progress.close()
    }

    private fun getOverlayPNG(overlay : OverlayDefinition, width: Int = 50, height: Int = 50): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                var rgb: Int = overlay.rgbColor
                if (rgb > 0) {
                    val red = rgb shr 16 and 0xFF
                    val green = rgb shr 8 and 0xFF
                    val blue = rgb and 0xFF
                    image.setRGB(x, y, Color(red, green, blue).rgb)
                }

                val secondaryRgb = overlay.secondaryRgbColor

                if (secondaryRgb > 0) {
                    val red = secondaryRgb shr 16 and 0xFF
                    val green = secondaryRgb shr 8 and 0xFF
                    val blue = secondaryRgb and 0xFF
                    image.setRGB(x, y, Color(red, green, blue).rgb)
                }
            }
        }
        return image
    }

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            val parser = ArgParser("app")
            val rev by parser.option(ArgType.Int, description = "The revision you wish to dump").default(0)
            parser.parse(args)
            Application.revision = rev


            Overlay().test()
        }

    }

}