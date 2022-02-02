package com.query.dump.impl

import com.query.Application
import com.query.Application.areas
import com.query.Application.objects
import com.query.Application.sprites
import com.query.Constants
import com.query.cache.definitions.Definition
import com.query.cache.definitions.impl.AreaProvider
import com.query.cache.definitions.impl.SpriteProvider
import com.query.dump.DefinitionsTypes
import com.query.dump.TypeManager
import com.query.utils.FileUtils.getFile
import com.query.utils.IndexType
import com.query.utils.Sprite
import com.query.utils.progress
import com.query.utils.revisionBefore
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO


class MapFunctions : TypeManager {

    override val requiredDefs = listOf(
        DefinitionsTypes.AREAS,
        DefinitionsTypes.SPRITES,
        DefinitionsTypes.OBJECTS
    )

    override fun load() {
        writeImage()
    }

    override fun onTest() {
        writeImage()
    }

    private fun writeImage() {

        if (revisionBefore(142)) {
            val functions = objects().filter { it.mapAreaId != -1 }
            val progress = progress("Writing Area Sprites",  functions.size.toLong())
            functions.filter { it.mapAreaId != -1 }.forEachIndexed { index, area ->

                val container: ByteArray = Constants.library.data(IndexType.SPRITES.number, 318)!!
                val sprite = SpriteData.decode(ByteBuffer.wrap(container))
                val img = sprite.getFrame(area.mapAreaId)
                val outputfile = getFile("mapFunctions/","${area.mapAreaId}.png")
                ImageIO.write(img, "png", outputfile)
                progress.step()
            }
            progress.close()
        } else {
            val functions = areas().filter { it.spriteId != -1 }
            val progress = progress("Writing Area Sprites",  functions.size.toLong())
            functions.filter { it.spriteId != -1 }.forEachIndexed { index, area ->
                val pink = getFile("sprites/pink/", "${area.spriteId}.png")
                Files.copy(pink.toPath(), getFile("mapFunctions/", "${index}.png").toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                progress.step()
            }
            progress.close()
        }


    }



    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            val parser = ArgParser("app")
            val rev by parser.option(ArgType.Int, description = "The revision you wish to dump").default(0)
            parser.parse(args)
            Application.revision = rev

            MapFunctions().test()
        }
    }

}